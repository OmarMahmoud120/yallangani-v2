package com.osmartapps.yallanghanee

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

@RunWith(AndroidJUnit4::class)
class RecognitionFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun opensALessonAndShowsTwoAnswers() {
        openFirstLesson()

        waitForText(context.getString(R.string.lesson_prompt))
        val answers = compose
            .onAllNodes(hasContentDescription("خيار", substring = true))
            .fetchSemanticsNodes()
        assertTrue("expected two answer cards, found ${answers.size}", answers.size == 2)
    }

    @Test
    fun lessonTogglesTheDroneBed() {
        openFirstLesson()
        waitForText(context.getString(R.string.lesson_prompt))

        val off = context.getString(R.string.lesson_drone)
        val on = context.getString(R.string.lesson_drone_on)
        compose.onAllNodesWithText(off).onFirst().performClick()
        waitForText(on)
    }

    @Test
    fun opensSettingsAndReturnsHome() {
        skipOnboardingIfShown()
        waitForText(context.getString(R.string.home_intro_title))

        compose
            .onAllNodesWithContentDescription(context.getString(R.string.settings_title))
            .onFirst()
            .performClick()
        waitForText(context.getString(R.string.settings_motion))

        compose
            .onAllNodesWithText(context.getString(R.string.gallery_back))
            .onFirst()
            .performClick()
        waitForText(context.getString(R.string.home_intro_title))
    }

    private fun openFirstLesson() {
        skipOnboardingIfShown()
        waitForText(context.getString(R.string.home_intro_title))

        val startLabels = listOf(
            R.string.home_start,
            R.string.home_continue,
            R.string.results_retry,
        ).map(context::getString)
        val label = startLabels.first {
            compose.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithText(label).onFirst().performClick()
    }

    private fun skipOnboardingIfShown() {
        val skip = context.getString(R.string.onboarding_skip)
        val home = context.getString(R.string.home_intro_title)
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText(skip).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithText(home).fetchSemanticsNodes().isNotEmpty()
        }
        if (compose.onAllNodesWithText(skip).fetchSemanticsNodes().isNotEmpty()) {
            compose.onAllNodesWithText(skip).onFirst().performClick()
        }
    }

    private fun waitForText(text: String, timeoutMs: Long = 15_000) {
        compose.waitUntil(timeoutMs) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
