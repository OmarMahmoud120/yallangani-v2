package com.osmartapps.yallanghanee

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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

        waitForText(context.getString(R.string.lesson_prompt))
        val answers = compose
            .onAllNodes(hasContentDescription("خيار", substring = true))
            .fetchSemanticsNodes()
        assertTrue("expected two answer cards, found ${answers.size}", answers.size == 2)
    }

    private fun waitForText(text: String, timeoutMs: Long = 15_000) {
        compose.waitUntil(timeoutMs) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
