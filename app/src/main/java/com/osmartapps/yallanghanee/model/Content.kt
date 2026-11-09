package com.osmartapps.yallanghanee.model

import kotlinx.serialization.Serializable

@Serializable
data class Level(
    val id: Int,
    val title: String,
    val audio: String,
)

@Serializable
data class ContentPack(
    val version: Int,
    val question: String,
    val notice: String = "",
    val levels: List<Level>,
)
