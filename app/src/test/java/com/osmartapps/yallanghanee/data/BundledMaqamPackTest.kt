package com.osmartapps.yallanghanee.data

import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.ui.components.sigilLetter
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

    private fun assertBundled(name: String) {
        val file = assetFile("src/main/assets/content/audio/$name")
        assertTrue("missing audio $name", file.isFile)
        assertTrue("empty audio $name", file.length() > 0)
    }

    @Test
    fun `bundled pack is valid`() {
        val errors = MaqamPackValidator.validate(pack) { name ->
            assetFile("src/main/assets/content/audio/$name").isFile
        }
        assertEquals(emptyList<String>(), errors)
    }

    @Test
    fun `pack is schema four with a site media base`() {
        assertEquals(4, pack.version)
        assertEquals("https://yallanganee-73906702.web.app/data/", pack.mediaBase)
    }

    @Test
    fun `every bundled clip is present and non-empty`() {
        pack.maqams.forEach { maqam ->
            maqam.audio?.let(::assertBundled)
            maqam.drone?.let(::assertBundled)
        }
    }

    @Test
    fun `every approved maqam has notation and site tracks`() {
        assertEquals(35, pack.maqams.size)
        assertTrue(pack.maqams.all { it.status == "approved" })
        pack.maqams.forEach { maqam ->
            assertTrue("blank signature for ${maqam.id}", maqam.signatureAr.isNotBlank())
            assertTrue("blank English name for ${maqam.id}", maqam.nameEn.isNotBlank())
            assertTrue("blank English jins for ${maqam.id}", maqam.jinsEn.isNotBlank())
            assertTrue("blank English signature for ${maqam.id}", maqam.signatureEn.isNotBlank())
            assertTrue("missing notation for ${maqam.id}", !maqam.notation.isNullOrBlank())
            assertTrue("no tracks for ${maqam.id}", maqam.tracks.isNotEmpty())
            assertNotNull("no media url for ${maqam.id}", pack.mediaUrl(maqam.notation))
        }
    }

    @Test
    fun `every maqam sigil starts on a letter in both languages`() {
        pack.maqams.forEach { maqam ->
            listOf("ar" to maqam.nameAr, "en" to maqam.nameEn).forEach { (language, name) ->
                val sigil = sigilLetter(name)
                assertEquals(
                    "sigil for ${maqam.id} ($language) reads ${name.firstOrNull()} in $name",
                    1,
                    sigil.length,
                )
                assertTrue(
                    "sigil for ${maqam.id} ($language) is not a letter in $name",
                    sigil.single().isLetterOrDigit(),
                )
            }
        }
    }

    @Test
    fun `lessons offer distinct maqams and valid answers`() {
        pack.lessons.forEach { lesson ->
            assertTrue("blank English title: ${lesson.id}", lesson.titleEn.isNotBlank())
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
    fun `every maqam is covered by a lesson`() {
        val covered = pack.lessons.flatMap { it.maqamIds }.toSet()
        val uncovered = pack.maqams.map { it.id }.filter { it !in covered }
        assertEquals(emptyList<String>(), uncovered)
    }

    @Test
    fun `onboarding teaches five drills and a known first win`() {
        assertEquals(5, pack.onboarding.steps.size)
        pack.onboarding.steps.forEach { step ->
            assertTrue("blank English prompt: ${step.id}", step.promptEn.isNotBlank())
            assertTrue("blank English explanation: ${step.id}", step.explanationEn.isNotBlank())
            assertTrue("blank English option label: ${step.id}", step.options.all { it.labelEn.isNotBlank() })
        }
        val firstWin = pack.onboarding.firstWin
        assertTrue("blank English first win prompt", firstWin.promptEn.isNotBlank())
        assertTrue("blank English first win explanation", firstWin.explanationEn.isNotBlank())
        assertTrue("first win answer must be one of its options", firstWin.answer in firstWin.maqamIds)
        assertTrue("first win answer must be a known maqam", pack.maqamById(firstWin.answer) != null)
    }
}
