package com.osmartapps.yallanghanee.data

import com.osmartapps.yallanghanee.model.MaqamPack
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BundledMaqamPackTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val pack: MaqamPack by lazy {
        val file = assetFile("src/main/assets/content/maqams.json")
        assertTrue("missing ${file.absolutePath}", file.isFile)
        json.decodeFromString<MaqamPack>(file.readText())
    }

    private fun assetFile(path: String): File {
        val direct = File(path)
        return if (direct.exists()) direct else File("app/$path")
    }

    @Test
    fun `bundled pack is valid`() {
        val errors = MaqamPackValidator.validate(pack) { name ->
            assetFile("src/main/assets/content/audio/$name").isFile
        }
        assertEquals(emptyList<String>(), errors)
    }

    @Test
    fun `every audio file is bundled and non-empty`() {
        pack.maqams.forEach { maqam ->
            val audio = assetFile("src/main/assets/content/audio/${maqam.audio}")
            assertTrue("missing audio ${maqam.audio}", audio.isFile)
            assertTrue("empty audio ${maqam.audio}", audio.length() > 0)
            val droneName = requireNotNull(maqam.drone) { "missing drone for ${maqam.id}" }
            val drone = assetFile("src/main/assets/content/audio/$droneName")
            assertTrue("missing drone $droneName", drone.isFile)
            assertTrue("empty drone $droneName", drone.length() > 0)
        }
    }

    @Test
    fun `lessons offer distinct maqams and valid answers`() {
        pack.lessons.forEach { lesson ->
            assertTrue("lesson needs two options: ${lesson.id}", lesson.maqamIds.size >= 2)
            assertEquals(
                "duplicate option in ${lesson.id}",
                lesson.maqamIds.size,
                lesson.maqamIds.distinct().size,
            )
            lesson.items.forEach { item ->
                assertTrue(
                    "answer outside options in ${lesson.id}/${item.id}",
                    item.answer in lesson.maqamIds,
                )
            }
        }
    }

    @Test
    fun `starter pack has three approved maqams`() {
        assertEquals(3, pack.maqams.size)
        assertTrue(pack.maqams.all { it.status == "approved" })
        assertTrue(pack.maqams.all { it.signatureAr.isNotBlank() })
    }

    @Test
    fun `onboarding teaches five drills and a known first win`() {
        assertEquals(5, pack.onboarding.steps.size)
        val firstWin = pack.onboarding.firstWin
        assertTrue("first win answer must be one of its options", firstWin.answer in firstWin.maqamIds)
        assertTrue("first win answer must be a known maqam", pack.maqamById(firstWin.answer) != null)
    }
}
