package com.osmartapps.yallanghanee.game

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.osmartapps.yallanghanee.data.ContentRepository
import com.osmartapps.yallanghanee.data.ProgressStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

sealed interface GameUiState {
    data object Loading : GameUiState

    data class Error(val message: String?) : GameUiState

    data class Ready(
        val game: GameState,
        val question: String,
        val notice: String,
        val roundsCompleted: Int,
        val solvedCount: Int = 0,
        val packComplete: Boolean = false,
        val showOnboarding: Boolean = false,
    ) : GameUiState
}

class GameViewModel(
    private val contentRepository: ContentRepository,
    private val progressStore: ProgressStore,
    private val random: Random = Random.Default,
) : ViewModel() {
    private val _state = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var engine: GameEngine? = null
    private var wrongJob: Job? = null
    private var roundsCompleted = 0
    private var countedLevelId: Int? = null
    private var solvedIds: Set<Int> = emptySet()
    private var levelIds: Set<Int> = emptySet()

    init {
        load()
    }

    fun retry() = load()

    fun dismissOnboarding() {
        viewModelScope.launch(Dispatchers.IO) { progressStore.setOnboardingSeen() }
        _state.update { existing ->
            if (existing is GameUiState.Ready) existing.copy(showOnboarding = false) else existing
        }
    }

    fun onTrayLetter(index: Int) {
        engine?.onTrayLetter(index)
        publish()
    }

    fun onSlotClick(wordIndex: Int, slotIndex: Int) {
        engine?.onSlotClick(wordIndex, slotIndex)
        publish()
    }

    fun revealHint() {
        engine?.revealHint()
        publish()
    }

    fun onNext() {
        engine?.next()
        publish()
    }

    fun resetProgress() {
        wrongJob?.cancel()
        engine?.restart()
        roundsCompleted = 0
        countedLevelId = null
        solvedIds = emptySet()
        viewModelScope.launch(Dispatchers.IO) {
            progressStore.resetProgress()
        }
        publish()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = GameUiState.Loading
            runCatching {
                val pack = withContext(Dispatchers.IO) { contentRepository.load() }
                val solved = withContext(Dispatchers.IO) { progressStore.solvedLevelIds.first() }
                val seenOnboarding =
                    withContext(Dispatchers.IO) { progressStore.onboardingSeen.first() }
                roundsCompleted = withContext(Dispatchers.IO) { progressStore.roundsCompleted.first() }
                solvedIds = solved
                levelIds = pack.levels.map { it.id }.toSet()
                val start = resumeIndex(pack.levels, solved)
                val newEngine = GameEngine(pack.levels, start, random)
                engine = newEngine
                _state.value = GameUiState.Ready(
                    newEngine.state,
                    pack.question,
                    pack.notice,
                    roundsCompleted,
                    solvedCount = solvedIds.intersect(levelIds).size,
                    packComplete = levelIds.isNotEmpty() && solvedIds.containsAll(levelIds),
                    showOnboarding = !seenOnboarding,
                )
            }.onFailure { error ->
                _state.value = GameUiState.Error(error.message)
            }
        }
    }

    private fun publish() {
        val currentEngine = engine ?: return
        val game = currentEngine.state
        if (game.solved && countedLevelId != game.levelId) {
            countedLevelId = game.levelId
            roundsCompleted++
            solvedIds = solvedIds + game.levelId
            viewModelScope.launch(Dispatchers.IO) {
                progressStore.setRoundsCompleted(roundsCompleted)
                progressStore.markSolved(game.levelId)
            }
        }
        val solvedCount = solvedIds.intersect(levelIds).size
        val packComplete = levelIds.isNotEmpty() && solvedIds.containsAll(levelIds)
        _state.update { existing ->
            if (existing is GameUiState.Ready) {
                existing.copy(
                    game = game,
                    roundsCompleted = roundsCompleted,
                    solvedCount = solvedCount,
                    packComplete = packComplete,
                )
            } else {
                existing
            }
        }

        if (game.wrong) {
            wrongJob?.cancel()
            wrongJob = viewModelScope.launch {
                delay(WRONG_FEEDBACK_MS)
                currentEngine.clearWrong()
                publish()
            }
        }
    }

    companion object {
        private const val WRONG_FEEDBACK_MS = 800L

        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GameViewModel(
                    ContentRepository(context.applicationContext),
                    ProgressStore(context.applicationContext),
                )
            }
        }
    }
}
