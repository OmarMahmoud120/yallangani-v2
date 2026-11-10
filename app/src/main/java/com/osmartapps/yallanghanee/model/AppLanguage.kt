package com.osmartapps.yallanghanee.model

enum class AppLanguage(val code: String, val isRtl: Boolean) {
    ARABIC("ar", true),
    ENGLISH("en", false),
    ;

    companion object {
        val default = ARABIC

        fun fromOrdinal(value: Int): AppLanguage = entries.getOrElse(value) { default }
    }
}
