package com.osmartapps.yallanghanee.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.osmartapps.yallanghanee.BuildConfig
import com.osmartapps.yallanghanee.game.RecognitionViewModel
import com.osmartapps.yallanghanee.ui.components.ErrorState
import com.osmartapps.yallanghanee.ui.components.LoadingState
import com.osmartapps.yallanghanee.ui.gallery.GalleryScreen
import com.osmartapps.yallanghanee.ui.theme.NightBg

@Composable
fun YallaApp() {
    val context = LocalContext.current
    val viewModel: RecognitionViewModel = viewModel(
        factory = RecognitionViewModel.factory(context),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pack = state.pack
    val session = state.session
    val result = state.result
    var showGallery by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NightBg,
    ) {
        when {
            pack == null && state.error != null -> ErrorState(onRetry = viewModel::retry)

            pack == null -> LoadingState()

            showGallery -> GalleryScreen(
                pack = pack,
                onBack = { showGallery = false },
            )

            state.onboardingComplete == null -> LoadingState()

            state.onboardingComplete == false -> OnboardingScreen(
                pack = pack,
                step = state.onboardingStep,
                onNext = viewModel::onboardingNext,
                onFinish = viewModel::completeOnboarding,
            )

            result != null -> ResultsScreen(
                result = result,
                pack = pack,
                hasNextLesson = pack.lessons.any {
                    it.id != result.lessonId && it.id !in state.completedLessons
                },
                onNext = { viewModel.startLessonAfter(result.lessonId) },
                onRetry = { viewModel.startLesson(result.lessonId) },
                onHome = viewModel::closeLesson,
            )

            session != null -> LessonScreen(
                session = session,
                onAnswer = viewModel::answer,
                onNext = viewModel::next,
                onSkip = viewModel::skip,
                onExit = viewModel::closeLesson,
            )

            else -> HomeScreen(
                pack = pack,
                completedLessons = state.completedLessons,
                onStartLesson = viewModel::startLesson,
                onResetProgress = viewModel::resetProgress,
                onOpenGallery = if (BuildConfig.DEBUG) {
                    { showGallery = true }
                } else {
                    null
                },
            )
        }
    }
}
