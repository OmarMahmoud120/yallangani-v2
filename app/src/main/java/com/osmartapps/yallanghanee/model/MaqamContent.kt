package com.osmartapps.yallanghanee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaqamPack(
    val version: Int,
    val notice: String? = null,
    val maqams: List<Maqam>,
    val lessons: List<RecognitionLesson>,
) {
    fun maqamById(id: String): Maqam? = maqams.firstOrNull { it.id == id }
}

@Serializable
data class Maqam(
    val id: String,
    @SerialName("name_ar") val nameAr: String,
    val color: String,
    @SerialName("jins_ar") val jinsAr: String,
    @SerialName("signature_ar") val signatureAr: String,
    val audio: String,
    val status: String = APPROVED,
) {
    companion object {
        const val APPROVED = "approved"
    }
}

@Serializable
data class RecognitionLesson(
    val id: String,
    @SerialName("title_ar") val titleAr: String,
    @SerialName("maqam_ids") val maqamIds: List<String>,
    val items: List<RecognitionItem>,
)

@Serializable
data class RecognitionItem(
    val id: String,
    val answer: String,
    @SerialName("explanation_ar") val explanationAr: String = "",
    val audio: String? = null,
)
