package com.osmartapps.yallanghanee

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.osmartapps.yallanghanee.game.answerLetters
import com.osmartapps.yallanghanee.model.ContentPack
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun solvesCurrentLevelThroughTheUi() {
        compose.mainClock.autoAdvance = false

        val stage = waitForText("المرحلة")
        val levelIndex = Regex("\\d+").find(stage)!!.value.toInt() - 1

        compose.onAllNodesWithText(context.getString(R.string.home_play)).onFirst().performClick()
        waitForText(context.getString(R.string.game_listen))

        compose.onNodeWithContentDescription(context.getString(R.string.play_song_cd)).performClick()
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithContentDescription(context.getString(R.string.game_pause)).assertExists()

        val pack = json.decodeFromString<ContentPack>(
            context.assets.open("content/levels.json").bufferedReader().use { it.readText() },
        )
        val title = pack.levels[levelIndex].title
        val clicks = mutableMapOf<Char, Int>()

        answerLetters(title).forEach { letter ->
            val description = context.getString(R.string.letter_cd, letter.toString())
            val index = clicks.getOrDefault(letter, 0)
            clicks[letter] = index + 1
            compose.onAllNodes(hasContentDescription(description) and hasClickAction(), true)[index]
                .performClick()
            compose.mainClock.advanceTimeBy(50)
        }

        if (levelIndex < pack.levels.lastIndex) {
            waitForText(context.getString(R.string.game_correct))
            compose.onAllNodesWithText(context.getString(R.string.game_next)).onFirst().performClick()
            compose.mainClock.advanceTimeBy(500)

            val nextStage = waitForText("المرحلة")
            assertTrue(
                "expected stage ${levelIndex + 2}, was '$nextStage'",
                nextStage.contains("${levelIndex + 2}"),
            )
        } else {
            waitForText(context.getString(R.string.game_completed))
        }
    }

    private fun waitForText(substring: String, timeoutMs: Long = 15_000): String {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            compose.mainClock.advanceTimeBy(200)
            val node = compose.onAllNodesWithText(substring, substring = true)
                .fetchSemanticsNodes()
                .firstOrNull()
            if (node != null) {
                return node.config[SemanticsProperties.Text].joinToString("") { it.text }
            }
            Thread.sleep(50)
        }
        throw AssertionError("Timed out waiting for text containing '$substring'")
    }
}
