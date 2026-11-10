package com.osmartapps.yallanghanee.game

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.osmartapps.yallanghanee.ads.AdCounters
import com.osmartapps.yallanghanee.ads.AdPolicy
import com.osmartapps.yallanghanee.data.HttpRemotePackSource
import com.osmartapps.yallanghanee.data.MaqamRepository
import com.osmartapps.yallanghanee.data.PackCache
import com.osmartapps.yallanghanee.data.ProgressStore
import com.osmartapps.yallanghanee.data.SettingsStore
import com.osmartapps.yallanghanee.model.AppLanguage
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.MotionPreference
import com.osmartapps.yallanghanee.model.RecognitionLesson
import com.osmartapps.yallanghanee.telemetry.Telemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

data class LessonResult(
    val lessonId: String,
    val titleAr: String,
    val correct: Int,
    val total: Int,
) {
    val passed: Boolean get() = correct * 2 >= total
}

data class RecognitionUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val pack: MaqamPack? = null,
    val completedLessons: Set<String> = emptySet(),
    val onboardingStep: Int = 0,
    val onboardingComplete: Boolean? = null,
    val motionPreference: MotionPreference = MotionPreference.System,
    val language: AppLanguage? = null,
    val session: RecognitionSession? = null,
    val result: LessonResult? = null,
    val adCounters: AdCounters = AdCounters(),
    val hintUnavailable: Boolean = false,
) {
    val nextLesson: RecognitionLesson?
        get() = pack?.lessons?.firstOrNull { it.id !in completedLessons }
            ?: pack?.lessons?.firstOrNull()

    val completedCount: Int
        get() = pack?.lessons?.count { it.id in completedLessons } ?: 0
}

class RecognitionViewModel(
    private val repository: MaqamRepository,
    private val progressStore: ProgressStore,
    private val settingsStore: SettingsStore,
    private val random: Random = Random.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(RecognitionUiState())
    val state: StateFlow<RecognitionUiState> = _state.asStateFlow()

    private var lastCheckAt = 0L

    init {
        load()
        viewModelScope.launch {
            progressStore.completedLessons.collect { completed ->
                _state.update { it.copy(completedLessons = completed) }
            }
        }
        viewModelScope.launch {
            progressStore.adCounters.collect { counters ->
                _state.update { it.copy(adCounters = counters) }
            }
        }
        viewModelScope.launch {
            progressStore.onboarding.collect { progress ->
                _state.update {
                    it.copy(
                        onboardingStep = progress.step,
                        onboardingComplete = progress.complete,
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsStore.motion.collect { preference ->
                _state.update { it.copy(motionPreference = preference) }
            }
        }
        viewModelScope.launch {
            settingsStore.language.collect { language ->
                _state.update { it.copy(language = language) }
            }
        }
    }

    fun retry() = load()

    fun onHomeVisible() {
        if (System.currentTimeMillis() - lastCheckAt < CHECK_INTERVAL_MS) return
        refresh()
    }

    fun onboardingNext() {
        val steps = _state.value.pack?.onboarding?.steps ?: return
        val next = (_state.value.onboardingStep + 1).coerceAtMost(steps.size)
        _state.update { it.copy(onboardingStep = next) }
        viewModelScope.launch(Dispatchers.IO) { progressStore.setOnboardingStep(next) }
    }

    fun completeOnboarding() {
        _state.update { it.copy(onboardingComplete = true) }
        Telemetry.onboardingCompleted()
        viewModelScope.launch(Dispatchers.IO) { progressStore.completeOnboarding() }
    }

    fun setMotionPreference(preference: MotionPreference) {
        _state.update { it.copy(motionPreference = preference) }
        Telemetry.settingChanged("motion", preference.name)
        viewModelScope.launch(Dispatchers.IO) { settingsStore.setMotion(preference) }
    }

    fun setLanguage(language: AppLanguage) {
        _state.update { it.copy(language = language) }
        Telemetry.settingChanged("language", language.code)
        viewModelScope.launch(Dispatchers.IO) { settingsStore.setLanguage(language) }
    }

    fun startLesson(lessonId: String) {
        val pack = _state.value.pack ?: return
        val lesson = pack.lessons.firstOrNull { it.id == lessonId } ?: return
        _state.update {
            it.copy(
                session = RecognitionEngine.start(lesson, pack, random),
                result = null,
            )
        }
        Telemetry.lessonStarted(lesson.id, lesson.maqamIds.size)
    }

    fun startNextLesson() {
        val next = _state.value.nextLesson ?: return
        startLesson(next.id)
    }

    fun startLessonAfter(lessonId: String) {
        val pack = _state.value.pack ?: return
        val completed = _state.value.completedLessons + lessonId
        val next = pack.lessons.firstOrNull { it.id !in completed } ?: return
        startLesson(next.id)
    }

    fun answer(maqamId: String) {
        val session = _state.value.session ?: return
        if (session.answered) return
        _state.update {
            it.copy(
                session = RecognitionEngine.answer(session, maqamId),
                hintUnavailable = false,
            )
        }
        Telemetry.answerSubmitted(
            lessonId = session.lesson.id,
            itemId = session.question.item.id,
            maqamId = maqamId,
            correct = maqamId == session.question.answer.id,
            position = session.index,
        )
    }

    fun grantHint() {
        val session = _state.value.session ?: return
        if (!RecognitionEngine.hintAvailable(session)) return
        _state.update {
            it.copy(
                session = RecognitionEngine.eliminateWrongOption(session),
                hintUnavailable = false,
            )
        }
        Telemetry.rewardedHintGranted()
    }

    fun hintUnavailable() {
        _state.update { it.copy(hintUnavailable = true) }
    }

    fun markInterstitialShown() {
        val counters = AdPolicy.afterInterstitial(_state.value.adCounters)
        _state.update { it.copy(adCounters = counters) }
        viewModelScope.launch(Dispatchers.IO) { progressStore.setAdCounters(counters) }
    }

    fun markAppOpenShown(todayEpochDay: Long) {
        val counters = AdPolicy.afterAppOpen(_state.value.adCounters, todayEpochDay)
        _state.update { it.copy(adCounters = counters) }
        viewModelScope.launch(Dispatchers.IO) { progressStore.setAdCounters(counters) }
    }

    fun next() {
        val session = _state.value.session ?: return
        if (!session.answered) return
        advance(RecognitionEngine.next(session))
    }

    fun skip() {
        val session = _state.value.session ?: return
        if (session.answered || session.finished) return
        advance(RecognitionEngine.skip(session))
    }

    private fun advance(advanced: RecognitionSession) {
        if (!advanced.finished) {
            _state.update { it.copy(session = advanced, hintUnavailable = false) }
            return
        }
        val result = LessonResult(
            lessonId = advanced.lesson.id,
            titleAr = advanced.lesson.titleAr,
            correct = advanced.correctCount,
            total = advanced.total,
        )
        val firstCompletion = result.lessonId !in _state.value.completedLessons
        _state.update { it.copy(session = advanced, result = result, hintUnavailable = false) }
        Telemetry.lessonCompleted(result.lessonId, result.correct, result.total, result.passed)
        if (result.passed) {
            if (firstCompletion) {
                val counters = AdPolicy.afterLessonCompleted(_state.value.adCounters)
                _state.update { it.copy(adCounters = counters) }
                viewModelScope.launch(Dispatchers.IO) { progressStore.setAdCounters(counters) }
            }
            viewModelScope.launch(Dispatchers.IO) {
                progressStore.markLessonCompleted(result.lessonId)
            }
        }
    }

    fun closeLesson() {
        _state.update { it.copy(session = null, result = null) }
    }

    fun resetProgress() {
        _state.update { it.copy(completedLessons = emptySet(), session = null, result = null) }
        viewModelScope.launch(Dispatchers.IO) { progressStore.resetProgress() }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val pack = withContext(Dispatchers.IO) { repository.loadBaseline() }
                _state.update { it.copy(loading = false, pack = pack) }
            }.onFailure { error ->
                _state.update {
                    it.copy(loading = false, error = error.message ?: "load failed")
                }
            }
            refresh()
        }
    }

    private fun refresh() {
        lastCheckAt = System.currentTimeMillis()
        val current = _state.value.pack ?: return
        viewModelScope.launch {
            val updated = runCatching {
                withContext(Dispatchers.IO) { repository.refresh(current) }
            }.getOrNull() ?: return@launch
            _state.update { it.copy(pack = updated) }
        }
    }

    companion object {
        private const val CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L
        private const val PACK_BASE_URL = "https://yallanganee-73906702.web.app"

        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                RecognitionViewModel(
                    MaqamRepository(
                        assets = appContext.assets,
                        cache = PackCache(File(appContext.filesDir, "pack")),
                        remote = HttpRemotePackSource(PACK_BASE_URL),
                    ),
                    ProgressStore(appContext),
                    SettingsStore(appContext),
                )
            }
        }
    }
}
