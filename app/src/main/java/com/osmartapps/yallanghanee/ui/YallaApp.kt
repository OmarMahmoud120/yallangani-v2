package com.osmartapps.yallanghanee.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.ads.AdsController
import com.osmartapps.yallanghanee.game.GameUiState
import com.osmartapps.yallanghanee.game.GameViewModel
import com.osmartapps.yallanghanee.ui.components.ErrorState
import com.osmartapps.yallanghanee.ui.components.LoadingState
import com.osmartapps.yallanghanee.ui.theme.BrandRed

private enum class Screen { Home, Game }

private const val ROUNDS_PER_INTERSTITIAL = 5

@Composable
fun YallaApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val ads = remember { AdsController() }
    val viewModel: GameViewModel = viewModel(factory = GameViewModel.factory(context))
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }

    LaunchedEffect(Unit) { ads.preload(context) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BrandRed,
    ) {
        when (val state = uiState) {
            GameUiState.Loading -> LoadingState()

            is GameUiState.Error -> ErrorState(onRetry = viewModel::retry)

            is GameUiState.Ready -> Crossfade(
                targetState = screen,
                label = "screen",
            ) { target ->
                when (target) {
                    Screen.Home -> HomeScreen(
                        game = state.game,
                        solvedCount = state.solvedCount,
                        packComplete = state.packComplete,
                        showOnboarding = state.showOnboarding,
                        onPlay = { screen = Screen.Game },
                        onResetProgress = viewModel::resetProgress,
                        onDismissOnboarding = viewModel::dismissOnboarding,
                    )

                    Screen.Game -> GameScreen(
                        game = state.game,
                        question = state.question,
                        packComplete = state.packComplete,
                        onBack = { screen = Screen.Home },
                        onTrayLetter = viewModel::onTrayLetter,
                        onSlotClick = viewModel::onSlotClick,
                        onNext = {
                            val adDue = state.roundsCompleted > 0 &&
                                state.roundsCompleted % ROUNDS_PER_INTERSTITIAL == 0
                            if (adDue && activity != null) {
                                ads.showInterstitial(activity) { viewModel.onNext() }
                            } else {
                                viewModel.onNext()
                            }
                        },
                        onResetProgress = {
                            viewModel.resetProgress()
                            screen = Screen.Home
                        },
                        onHint = {
                            if (activity == null) {
                                viewModel.revealHint()
                            } else {
                                ads.showRewarded(
                                    activity = activity,
                                    onReward = viewModel::revealHint,
                                    onUnavailable = {
                                        Toast.makeText(
                                            context,
                                            R.string.ad_unavailable,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
