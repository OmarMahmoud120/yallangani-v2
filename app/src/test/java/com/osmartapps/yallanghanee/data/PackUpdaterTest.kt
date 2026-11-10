package com.osmartapps.yallanghanee.data

import com.osmartapps.yallanghanee.model.FirstWin
import com.osmartapps.yallanghanee.model.Maqam
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.Onboarding
import com.osmartapps.yallanghanee.model.OnboardingOption
import com.osmartapps.yallanghanee.model.OnboardingStep
import com.osmartapps.yallanghanee.model.RecognitionItem
import com.osmartapps.yallanghanee.model.RecognitionLesson
import com.osmartapps.yallanghanee.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PackUpdaterTest {
    @get:Rule
    val temp = TemporaryFolder()

    private lateinit var cache: PackCache
    private lateinit var remote: FakeRemote

    @Before
    fun setUp() {
        cache = PackCache(temp.newFolder())
        remote = FakeRemote()
    }

    @Test
    fun `newer content version is adopted and cached`() {
        val current = pack(contentVersion = "v1")
        val newer = pack(contentVersion = "v2", publishedAt = "2026-11-11T00:00:00Z")
        remote.respond(REMOTE_PACK, encode(newer))
        val updater = updater(assetExists = { true })

        val result = updater.refresh(current)

        assertNotNull(result)
        assertEquals("v2", result?.contentVersion)
        assertEquals(newer, updater.loadCached())
    }

    @Test
    fun `equal content version is ignored`() {
        val current = pack(contentVersion = "v1")
        remote.respond(REMOTE_PACK, encode(current))

        assertNull(updater(assetExists = { true }).refresh(current))
        assertNull(cache.readPack())
    }

    @Test
    fun `pack without content version is rejected`() {
        val newer = pack(contentVersion = null)
        remote.respond(REMOTE_PACK, encode(newer))

        assertNull(updater(assetExists = { true }).refresh(pack(contentVersion = "v1")))
        assertNull(cache.readPack())
    }

    @Test
    fun `unsupported schema version is rejected`() {
        val newer = pack(contentVersion = "v2").copy(version = 2)
        remote.respond(REMOTE_PACK, encode(newer))

        assertNull(updater(assetExists = { true }).refresh(pack(contentVersion = "v1")))
        assertNull(cache.readPack())
    }

    @Test
    fun `malformed remote json is rejected`() {
        remote.respond(REMOTE_PACK, "{ not json".toByteArray())

        assertNull(updater(assetExists = { true }).refresh(pack(contentVersion = "v1")))
    }

    @Test
    fun `unreachable remote keeps the current pack`() {
        assertNull(updater(assetExists = { true }).refresh(pack(contentVersion = "v1")))
        assertNull(cache.readPack())
    }

    @Test
    fun `missing remote clip rejects the whole pack`() {
        val newer = packWithRemoteClip(contentVersion = "v2")
        remote.respond(REMOTE_PACK, encode(newer))
        val updater = updater(assetExists = { it != REMOTE_CLIP })

        assertNull(updater.refresh(pack(contentVersion = "v1")))
        assertFalse(cache.hasClip(REMOTE_CLIP))
    }

    @Test
    fun `clip with a mismatched digest is rejected`() {
        val newer = packWithRemoteClip(contentVersion = "v2")
        remote.respond(REMOTE_PACK, encode(newer))
        remote.respond("/data/app-audio/$REMOTE_CLIP", "tampered".toByteArray())

        assertNull(updater(assetExists = { it != REMOTE_CLIP }).refresh(pack(contentVersion = "v1")))
        assertFalse(cache.hasClip(REMOTE_CLIP))
        assertNull(cache.readPack())
    }

    @Test
    fun `clip without a digest entry is rejected`() {
        val newer = packWithRemoteClip(contentVersion = "v2").copy(audioSha256 = emptyMap())
        remote.respond(REMOTE_PACK, encode(newer))

        assertNull(updater(assetExists = { it != REMOTE_CLIP }).refresh(pack(contentVersion = "v1")))
        assertFalse(cache.hasClip(REMOTE_CLIP))
    }

    @Test
    fun `unsafe clip name is rejected before any download`() {
        val newer = packWithRemoteClip(contentVersion = "v2", clipName = "../evil.mp3")
        remote.respond(REMOTE_PACK, encode(newer))

        assertNull(updater(assetExists = { true }).refresh(pack(contentVersion = "v1")))
        assertFalse(remote.requests.contains("/data/app-audio/../evil.mp3"))
    }

    @Test
    fun `verified clip is downloaded and cached`() {
        val audio = "new".toByteArray()
        val newer = packWithRemoteClip(contentVersion = "v2")
        remote.respond(REMOTE_PACK, encode(newer))
        remote.respond("/data/app-audio/$REMOTE_CLIP", audio)
        val updater = updater(assetExists = { it != REMOTE_CLIP })

        assertNotNull(updater.refresh(pack(contentVersion = "v1")))
        assertTrue(cache.hasClip(REMOTE_CLIP))
        assertEquals(sha256(audio), sha256(cache.clipFile(REMOTE_CLIP)))
        assertEquals(PackUpdater.CLIP_MAX_BYTES, remote.limits.last())
    }

    @Test
    fun `clip already cached with the right digest is not fetched again`() {
        val audio = "cached".toByteArray()
        cache.writeClip(REMOTE_CLIP, audio)
        val newer = packWithRemoteClip(contentVersion = "v2")
        remote.respond(REMOTE_PACK, encode(newer))

        assertNotNull(updater(assetExists = { true }).refresh(pack(contentVersion = "v1")))
        assertFalse(remote.requests.contains("/data/app-audio/$REMOTE_CLIP"))
    }

    @Test
    fun `site track audio is not downloaded or cached`() {
        val newer = pack(contentVersion = "v2")
        remote.respond(REMOTE_PACK, encode(newer))

        assertNotNull(updater(assetExists = { true }).refresh(pack(contentVersion = "v1")))
        assertEquals(listOf(REMOTE_PACK), remote.requests)
    }

    @Test
    fun `bundled clip is not downloaded`() {
        val newer = packWithRemoteClip(contentVersion = "v2")
        remote.respond(REMOTE_PACK, encode(newer))

        assertNotNull(updater(assetExists = { true }).refresh(pack(contentVersion = "v1")))
        assertEquals(listOf(REMOTE_PACK), remote.requests)
        assertEquals(PackUpdater.PACK_MAX_BYTES, remote.limits.single())
    }

    @Test
    fun `invalid pack leaves the previous cache untouched`() {
        val current = pack(contentVersion = "v1")
        cache.writePack(encode(current).toString(Charsets.UTF_8))
        val broken = pack(contentVersion = "v2").copy(
            maqams = listOf(hijaz, hijaz),
            lessons = emptyList(),
        )
        remote.respond(REMOTE_PACK, encode(broken))
        val updater = updater(assetExists = { true })

        assertNull(updater.refresh(current))
        assertEquals(current, updater.loadCached())
    }

    @Test
    fun `prune removes clips the new pack no longer references`() {
        val stale = "stale.mp3".toByteArray()
        cache.writeClip(STALE_CLIP, stale)
        val newer = packWithRemoteClip(contentVersion = "v2")
        remote.respond(REMOTE_PACK, encode(newer))
        remote.respond("/data/app-audio/$REMOTE_CLIP", "new".toByteArray())

        assertNotNull(updater(assetExists = { true }).refresh(pack(contentVersion = "v1")))
        assertFalse(cache.hasClip(STALE_CLIP))
    }

    @Test
    fun `cached pack with broken json is ignored`() {
        temp.root.resolve("pack.json").writeText("{")

        assertNull(updater(assetExists = { true }).loadCached())
    }

    private fun updater(assetExists: (String) -> Boolean) = PackUpdater(cache, remote, assetExists)

    private fun pack(
        contentVersion: String?,
        publishedAt: String = "2026-11-10T00:00:00Z",
    ) = MaqamPack(
        version = PackUpdater.SCHEMA_VERSION,
        mediaBase = MEDIA_BASE,
        contentVersion = contentVersion,
        publishedAt = publishedAt,
        maqams = listOf(hijaz, nahawand),
        onboarding = onboarding(),
        lessons = listOf(lesson),
    )

    private fun packWithRemoteClip(
        contentVersion: String,
        clipName: String = REMOTE_CLIP,
    ): MaqamPack {
        val audio = "new".toByteArray()
        return MaqamPack(
            version = PackUpdater.SCHEMA_VERSION,
            mediaBase = MEDIA_BASE,
            contentVersion = contentVersion,
            publishedAt = "2026-11-11T00:00:00Z",
            maqams = listOf(hijaz.copy(audio = clipName), nahawand),
            onboarding = onboarding(),
            lessons = listOf(lesson),
            audioSha256 = mapOf(clipName to sha256(audio)),
        )
    }

    private fun onboarding() = Onboarding(
        steps = (1..5).map(::step),
        firstWin = FirstWin(
            promptAr = "أي مقام تسمع؟",
            promptEn = "Which maqam do you hear?",
            audio = "01-alhijaz.mp3",
            maqamIds = listOf("hijaz", "nahawand"),
            answer = "hijaz",
            explanationAr = "توقيع الحجاز",
            explanationEn = "Hijaz signature",
        ),
    )

    private fun step(index: Int) = OnboardingStep(
        id = "step_$index",
        promptAr = "سؤال $index",
        promptEn = "Question $index",
        audio = "on-$index.mp3",
        options = listOf(
            OnboardingOption(id = "first", labelAr = "الأولى", labelEn = "First"),
            OnboardingOption(id = "second", labelAr = "الثانية", labelEn = "Second"),
        ),
        answer = "second",
        explanationAr = "شرح $index",
        explanationEn = "Explanation $index",
    )

    private fun encode(pack: MaqamPack): ByteArray =
        PackUpdater.JSON.encodeToString(MaqamPack.serializer(), pack).toByteArray()

    private class FakeRemote : RemotePackSource {
        private val responses = mutableMapOf<String, ByteArray>()
        val requests = mutableListOf<String>()
        val limits = mutableListOf<Int>()

        fun respond(path: String, bytes: ByteArray) {
            responses[path] = bytes
        }

        override fun get(path: String, maxBytes: Int): ByteArray? {
            requests += path
            limits += maxBytes
            return responses[path]?.takeIf { it.size <= maxBytes }
        }
    }

    private companion object {
        const val MEDIA_BASE = "https://yallanganee-73906702.web.app/data/"
        const val REMOTE_PACK = "/data/pack.json"
        const val REMOTE_CLIP = "new-hijaz.mp3"
        const val STALE_CLIP = "stale.mp3"

        val hijazTrack = Track("لحن الحجاز", "فنان", "media/maqam/hijaz/a.mp3")
        val nahawandTrack = Track("لحن النهاوند", "فنان", "media/maqam/nahawand/a.mp3")

        val hijaz = Maqam(
            id = "hijaz",
            nameAr = "حجاز",
            nameEn = "Hijaz",
            color = "#C97B5A",
            jinsAr = "جنس حجاز",
            jinsEn = "Jins Hijaz",
            signatureAr = "توقيع الحجاز",
            signatureEn = "Hijaz signature",
            audio = "01-alhijaz.mp3",
            notation = "media/maqam/hijaz/hijaz.png",
            tracks = listOf(hijazTrack),
        )
        val nahawand = Maqam(
            id = "nahawand",
            nameAr = "نهاوند",
            nameEn = "Nahawand",
            color = "#C77E9B",
            jinsAr = "جنس نهاوند",
            jinsEn = "Jins Nahawand",
            signatureAr = "توقيع النهاوند",
            signatureEn = "Nahawand signature",
            audio = "02-alnahawand.mp3",
            notation = "media/maqam/nahawand/nahawand.png",
            tracks = listOf(nahawandTrack),
        )
        val lesson = RecognitionLesson(
            id = "hijaz_nahawand",
            titleAr = "حجاز أم نهاوند؟",
            titleEn = "Hijaz or Nahawand?",
            maqamIds = listOf("hijaz", "nahawand"),
            items = listOf(RecognitionItem(id = "1", answer = "hijaz")),
        )
    }
}
