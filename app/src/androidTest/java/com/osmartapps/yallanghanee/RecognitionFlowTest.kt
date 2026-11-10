package com.osmartapps.yallanghanee

import android.content.res.Configuration
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class RecognitionFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val arabic by lazy { localized("ar") }
    private val english by lazy { localized("en") }

    private fun localized(code: String) = context.createConfigurationContext(
        Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(code)) },
    )

    private fun text(id: Int) = arabic.getString(id)
    private fun englishText(id: Int) = english.getString(id)

    @Test
    fun opensALessonAndShowsTwoAnswers() {
        val english = openFirstLesson()

        waitForText(if (english) englishText(R.string.lesson_prompt) else text(R.string.lesson_prompt))
        val marker = listOf("، خيار", ", option").first { candidate ->
            compose
                .onAllNodes(hasContentDescription(candidate, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        val answers = compose
            .onAllNodes(hasContentDescription(marker, substring = true))
            .fetchSemanticsNodes()
        assertTrue("expected two answer cards, found ${answers.size}", answers.size == 2)
    }

    @Test
    fun lessonTogglesTheDroneBed() {
        val english = openFirstLesson()
        waitForText(if (english) englishText(R.string.lesson_prompt) else text(R.string.lesson_prompt))

        val off = if (english) englishText(R.string.lesson_drone) else text(R.string.lesson_drone)
        val on = if (english) englishText(R.string.lesson_drone_on) else text(R.string.lesson_drone_on)
        compose.onAllNodesWithText(off).onFirst().performClick()
        waitForText(on)
    }

    @Test
    fun opensSettingsAndReturnsHome() {
        skipOnboardingIfShown()
        waitForText(text(R.string.home_intro_title))

        compose
            .onAllNodesWithContentDescription(text(R.string.settings_title))
            .onFirst()
            .performClick()
        waitForText(text(R.string.settings_motion))

        compose
            .onAllNodesWithText(text(R.string.gallery_back))
            .onFirst()
            .performClick()
        waitForText(text(R.string.home_intro_title))
    }

    @Test
    fun settingsSwitchLanguageAndPersistAcrossRestart() {
        skipOnboardingIfShown()
        waitForText(text(R.string.home_intro_title))

        compose
            .onAllNodesWithContentDescription(text(R.string.settings_title))
            .onFirst()
            .performClick()
        waitForText(text(R.string.settings_language))
        compose.onAllNodesWithText(text(R.string.settings_language_english)).onFirst().performClick()
        waitForText(englishText(R.string.settings_motion))

        compose.onAllNodesWithText(englishText(R.string.gallery_back)).onFirst().performClick()
        waitForText(englishText(R.string.home_intro_title))

        compose.activityRule.scenario.recreate()
        waitForText(englishText(R.string.home_intro_title))

        compose
            .onAllNodesWithContentDescription(englishText(R.string.settings_title))
            .onFirst()
            .performClick()
        compose.onAllNodesWithText(text(R.string.settings_language_arabic)).onFirst().performClick()
        waitForText(text(R.string.settings_motion))
    }

    private fun openFirstLesson(): Boolean {
        skipOnboardingIfShown()
        waitForHome()
        val english = isEnglishUi()
        resetProgress()

        val startLabels = listOf(
            R.string.home_start,
            R.string.home_continue,
            R.string.results_retry,
        ).map { if (english) englishText(it) else text(it) }
        val label = startLabels.first {
            compose.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithText(label).onFirst().performClick()
        return english
    }

    private fun isEnglishUi() = compose
        .onAllNodesWithText(englishText(R.string.home_intro_title))
        .fetchSemanticsNodes()
        .isNotEmpty()

    private fun waitForHome() {
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText(text(R.string.home_intro_title)).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithText(englishText(R.string.home_intro_title)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun resetProgress() {
        val english = isEnglishUi()
        fun label(id: Int) = if (english) englishText(id) else text(id)

        compose
            .onAllNodesWithContentDescription(label(R.string.settings_title))
            .onFirst()
            .performClick()
        waitForText(label(R.string.settings_motion))
        compose.onAllNodesWithText(label(R.string.settings_reset)).onFirst().performClick()
        waitForText(label(R.string.reset_title))
        compose.onAllNodesWithText(label(R.string.reset_confirm)).onFirst().performClick()
        waitForHome()
    }

    private fun skipOnboardingIfShown() {
        val skip = text(R.string.onboarding_skip)
        val home = text(R.string.home_intro_title)
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText(skip).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithText(home).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithText(englishText(R.string.onboarding_skip)).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithText(englishText(R.string.home_intro_title)).fetchSemanticsNodes().isNotEmpty()
        }
        val skipLabel = listOf(skip, englishText(R.string.onboarding_skip))
            .firstOrNull { compose.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty() }
        if (skipLabel != null) {
            compose.onAllNodesWithText(skipLabel).onFirst().performClick()
        }
    }

    private fun waitForText(text: String, timeoutMs: Long = 15_000) {
        compose.waitUntil(timeoutMs) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
