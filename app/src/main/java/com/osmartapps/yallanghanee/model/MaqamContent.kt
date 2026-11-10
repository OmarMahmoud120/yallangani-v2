package com.osmartapps.yallanghanee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaqamPack(
    val version: Int,
    @SerialName("content_version") val contentVersion: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("media_base") val mediaBase: String? = null,
    val notice: String? = null,
    @SerialName("notice_en") val noticeEn: String? = null,
    val maqams: List<Maqam>,
    val onboarding: Onboarding,
    val lessons: List<RecognitionLesson>,
    @SerialName("audio_sha256") val audioSha256: Map<String, String> = emptyMap(),
) {
    fun maqamById(id: String): Maqam? = maqams.firstOrNull { it.id == id }

    fun mediaUrl(path: String?): String? {
        if (path.isNullOrBlank() || mediaBase.isNullOrBlank()) return null
        return if (mediaBase.endsWith("/")) mediaBase + path else "$mediaBase/$path"
    }
}

@Serializable
data class Onboarding(
    val steps: List<OnboardingStep>,
    @SerialName("first_win") val firstWin: FirstWin,
)

@Serializable
data class OnboardingStep(
    val id: String,
    @SerialName("prompt_ar") val promptAr: String,
    @SerialName("prompt_en") val promptEn: String = "",
    val audio: String,
    val options: List<OnboardingOption>,
    val answer: String,
    @SerialName("explanation_ar") val explanationAr: String,
    @SerialName("explanation_en") val explanationEn: String = "",
)

@Serializable
data class OnboardingOption(
    val id: String,
    @SerialName("label_ar") val labelAr: String,
    @SerialName("label_en") val labelEn: String = "",
)

@Serializable
data class FirstWin(
    @SerialName("prompt_ar") val promptAr: String,
    @SerialName("prompt_en") val promptEn: String = "",
    val audio: String,
    @SerialName("maqam_ids") val maqamIds: List<String>,
    val answer: String,
    @SerialName("explanation_ar") val explanationAr: String,
    @SerialName("explanation_en") val explanationEn: String = "",
)

@Serializable
data class Maqam(
    val id: String,
    @SerialName("name_ar") val nameAr: String,
    @SerialName("name_en") val nameEn: String = "",
    val color: String,
    @SerialName("jins_ar") val jinsAr: String,
    @SerialName("jins_en") val jinsEn: String = "",
    @SerialName("signature_ar") val signatureAr: String,
    @SerialName("signature_en") val signatureEn: String = "",
    val audio: String? = null,
    val drone: String? = null,
    val status: String = APPROVED,
    val notation: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
    val tracks: List<Track> = emptyList(),
) {
    companion object {
        const val APPROVED = "approved"
    }
}

@Serializable
data class Track(
    val title: String,
    val artist: String,
    val audio: String,
    val credits: List<String> = emptyList(),
)

@Serializable
data class RecognitionLesson(
    val id: String,
    @SerialName("title_ar") val titleAr: String,
    @SerialName("title_en") val titleEn: String = "",
    @SerialName("maqam_ids") val maqamIds: List<String>,
    val items: List<RecognitionItem>,
)

@Serializable
data class RecognitionItem(
    val id: String,
    val answer: String,
    @SerialName("explanation_ar") val explanationAr: String = "",
    @SerialName("explanation_en") val explanationEn: String = "",
    val audio: String? = null,
)

fun Maqam.name(language: AppLanguage): String = localized(language, nameEn, nameAr)

fun Maqam.jins(language: AppLanguage): String = localized(language, jinsEn, jinsAr)

fun Maqam.signature(language: AppLanguage): String = localized(language, signatureEn, signatureAr)

fun RecognitionLesson.title(language: AppLanguage): String = localized(language, titleEn, titleAr)

fun RecognitionItem.explanation(language: AppLanguage): String =
    if (language == AppLanguage.ENGLISH) explanationEn else explanationAr

fun MaqamPack.notice(language: AppLanguage): String? =
    if (language == AppLanguage.ENGLISH) noticeEn?.ifBlank { notice } ?: notice else notice

fun OnboardingStep.prompt(language: AppLanguage): String = localized(language, promptEn, promptAr)

fun OnboardingStep.explanation(language: AppLanguage): String =
    localized(language, explanationEn, explanationAr)

fun OnboardingOption.label(language: AppLanguage): String = localized(language, labelEn, labelAr)

fun FirstWin.prompt(language: AppLanguage): String = localized(language, promptEn, promptAr)

fun FirstWin.explanation(language: AppLanguage): String =
    localized(language, explanationEn, explanationAr)

private fun localized(language: AppLanguage, english: String, arabic: String): String =
    if (language == AppLanguage.ENGLISH) english.ifBlank { arabic } else arabic
