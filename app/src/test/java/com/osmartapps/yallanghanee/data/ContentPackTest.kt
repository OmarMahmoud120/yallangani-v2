package com.osmartapps.yallanghanee.data

import com.osmartapps.yallanghanee.game.answerLetters
import com.osmartapps.yallanghanee.model.ContentPack
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ContentPackTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val pack: ContentPack by lazy {
        val file = assetFile("src/main/assets/content/levels.json")
        assertTrue("missing ${file.absolutePath}", file.isFile)
        json.decodeFromString<ContentPack>(file.readText())
    }

    private fun assetFile(path: String): File {
        val direct = File(path)
        return if (direct.exists()) direct else File("app/$path")
    }

    @Test
    fun `content pack is valid`() {
        assertEquals(emptyList<String>(), pack.validationErrors())
    }

    @Test
    fun `every level has a bundled audio file`() {
        pack.levels.forEach { level ->
            val audio = assetFile("src/main/assets/content/audio/${level.audio}")
            assertTrue("missing audio ${level.audio}", audio.isFile)
            assertTrue("empty audio ${level.audio}", audio.length() > 0)
        }
    }

    @Test
    fun `ids are unique and titles fit the board`() {
        assertEquals(pack.levels.size, pack.levels.map { it.id }.distinct().size)
        pack.levels.forEach { level ->
            assertTrue("title too long: ${level.title}", answerLetters(level.title).length <= 16)
        }
    }

    @Test
    fun `question is present`() {
        assertTrue(pack.question.isNotBlank())
        assertTrue(pack.levels.isNotEmpty())
    }
}
