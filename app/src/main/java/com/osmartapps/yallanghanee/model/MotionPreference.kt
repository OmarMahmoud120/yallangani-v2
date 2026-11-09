package com.osmartapps.yallanghanee.model

enum class MotionPreference {
    System,
    Reduced,
    Full,
    ;

    fun enabled(systemAnimationsEnabled: Boolean): Boolean = when (this) {
        System -> systemAnimationsEnabled
        Reduced -> false
        Full -> true
    }
}
