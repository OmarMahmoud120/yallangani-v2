package com.osmartapps.yallanghanee.game

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.osmartapps.yallanghanee.data.MaqamRepository
import com.osmartapps.yallanghanee.data.ProgressStore
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.RecognitionLesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val session: RecognitionSession? = null,
    val result: LessonResult? = null,
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
    private val random: Random = Random.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(RecognitionUiState())
    val state: StateFlow<RecognitionUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            progressStore.completedLessons.collect { completed ->
                _state.update { it.copy(completedLessons = completed) }
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
    }

    fun retry() = load()

    fun onboardingNext() {
        val steps = _state.value.pack?.onboarding?.steps ?: return
        val next = (_state.value.onboardingStep + 1).coerceAtMost(steps.size)
        _state.update { it.copy(onboardingStep = next) }
        viewModelScope.launch(Dispatchers.IO) { progressStore.setOnboardingStep(next) }
    }

    fun completeOnboarding() {
        _state.update { it.copy(onboardingComplete = true) }
        viewModelScope.launch(Dispatchers.IO) { progressStore.completeOnboarding() }
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
        _state.update { it.copy(session = RecognitionEngine.answer(session, maqamId)) }
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
            _state.update { it.copy(session = advanced) }
            return
        }
        val result = LessonResult(
            lessonId = advanced.lesson.id,
            titleAr = advanced.lesson.titleAr,
            correct = advanced.correctCount,
            total = advanced.total,
        )
        _state.update { it.copy(session = advanced, result = result) }
        if (result.passed) {
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
                val pack = withContext(Dispatchers.IO) { repository.load() }
                _state.update { it.copy(loading = false, pack = pack) }
            }.onFailure { error ->
                _state.update {
                    it.copy(loading = false, error = error.message ?: "load failed")
                }
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                RecognitionViewModel(
                    MaqamRepository(appContext.assets),
                    ProgressStore(appContext),
                )
            }
        }
    }
}
