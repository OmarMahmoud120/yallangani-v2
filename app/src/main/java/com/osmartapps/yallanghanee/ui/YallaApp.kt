package com.osmartapps.yallanghanee.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.osmartapps.yallanghanee.BuildConfig
import com.osmartapps.yallanghanee.ads.AdPolicy
import com.osmartapps.yallanghanee.ads.AdsController
import com.osmartapps.yallanghanee.ads.ConsentManager
import com.osmartapps.yallanghanee.game.RecognitionViewModel
import com.osmartapps.yallanghanee.telemetry.Telemetry
import com.osmartapps.yallanghanee.ui.components.AdBanner
import com.osmartapps.yallanghanee.ui.components.ErrorState
import com.osmartapps.yallanghanee.ui.components.LoadingState
import com.osmartapps.yallanghanee.ui.components.LocalMotionPreference
import com.osmartapps.yallanghanee.ui.gallery.GalleryScreen
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightMajlisTheme

@Composable
fun YallaApp() {
    val context = LocalContext.current
    val viewModel: RecognitionViewModel = viewModel(
        factory = RecognitionViewModel.factory(context),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = state.language ?: return
    val pack = state.pack
    val session = state.session
    val result = state.result
    var showGallery by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val activity = remember(context) { context.findActivity() }
    val consentManager = remember(context) { ConsentManager(context) }
    val consent by consentManager.state.collectAsStateWithLifecycle()
    val ads = remember { AdsController() }
    var appOpenAttempted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(activity) {
        activity?.let(consentManager::refresh)
    }

    LaunchedEffect(consent.canRequestAds) {
        if (consent.canRequestAds) {
            ads.initialize(context)
            ads.preload(context)
        }
    }

    val homeVisible = pack != null &&
        state.onboardingComplete == true &&
        !showSettings &&
        !showGallery &&
        session == null &&
        result == null

    val screenName = when {
        pack == null && state.error != null -> "error"
        pack == null -> "loading"
        state.onboardingComplete == null -> "loading"
        showGallery -> "gallery"
        state.onboardingComplete == false -> "onboarding"
        showSettings -> "settings"
        result != null -> "results"
        session != null -> "lesson"
        else -> "home"
    }
    LaunchedEffect(screenName) { Telemetry.screen(screenName) }

    LaunchedEffect(homeVisible, consent.canRequestAds, state.adCounters) {
        val current = activity ?: return@LaunchedEffect
        val today = AdPolicy.epochDay(System.currentTimeMillis())
        if (appOpenAttempted ||
            !homeVisible ||
            !AdPolicy.shouldShowAppOpen(
                state.adCounters,
                today,
                onboardingComplete = state.onboardingComplete == true,
                canRequestAds = consent.canRequestAds,
            )
        ) {
            return@LaunchedEffect
        }
        appOpenAttempted = true
        ads.showAppOpen(current) { viewModel.markAppOpenShown(today) }
    }

    fun leaveResults(action: () -> Unit) {
        val current = activity
        if (current != null &&
            consent.canRequestAds &&
            AdPolicy.shouldShowInterstitial(state.adCounters) &&
            ads.hasInterstitial()
        ) {
            viewModel.markInterstitialShown()
            ads.showInterstitial(current) { action() }
        } else {
            action()
        }
    }

    LocalizedContent(language) {
        NightMajlisTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = NightBg,
            ) {
                CompositionLocalProvider(LocalMotionPreference provides state.motionPreference) {
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

                        showSettings -> SettingsScreen(
                            pack = pack,
                            language = language,
                            motionPreference = state.motionPreference,
                            onLanguageChange = viewModel::setLanguage,
                            onMotionChange = viewModel::setMotionPreference,
                            onResetProgress = {
                                viewModel.resetProgress()
                                showSettings = false
                            },
                            onBack = { showSettings = false },
                            privacyOptionsRequired = consent.privacyOptionsRequired,
                            onPrivacyOptions = {
                                activity?.let(consentManager::showPrivacyOptions)
                            },
                        )

                        result != null -> ResultsScreen(
                            result = result,
                            pack = pack,
                            hasNextLesson = pack.lessons.any {
                                it.id != result.lessonId && it.id !in state.completedLessons
                            },
                            onNext = {
                                leaveResults { viewModel.startLessonAfter(result.lessonId) }
                            },
                            onRetry = { leaveResults { viewModel.startLesson(result.lessonId) } },
                            onHome = { leaveResults(viewModel::closeLesson) },
                        )

                        session != null -> LessonScreen(
                            session = session,
                            onAnswer = viewModel::answer,
                            onNext = viewModel::next,
                            onSkip = viewModel::skip,
                            onExit = viewModel::closeLesson,
                            onHint = {
                                val current = activity
                                if (current == null || !consent.canRequestAds) {
                                    viewModel.hintUnavailable()
                                } else {
                                    ads.showRewarded(
                                        current,
                                        onReward = viewModel::grantHint,
                                        onUnavailable = viewModel::hintUnavailable,
                                    )
                                }
                            },
                            hintUnavailable = state.hintUnavailable,
                        )

                        else -> {
                            LaunchedEffect(Unit) { viewModel.onHomeVisible() }
                            HomeScreen(
                                pack = pack,
                                completedLessons = state.completedLessons,
                                onStartLesson = viewModel::startLesson,
                                onOpenSettings = { showSettings = true },
                                onOpenGallery = if (BuildConfig.DEBUG) {
                                    { showGallery = true }
                                } else {
                                    null
                                },
                                footer = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        AdBanner(enabled = consent.canRequestAds)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
