package com.osmartapps.yallanghanee.game

import com.osmartapps.yallanghanee.model.Level

fun resumeIndex(levels: List<Level>, solvedIds: Set<Int>): Int =
    levels.indexOfFirst { it.id !in solvedIds }.takeIf { it >= 0 } ?: 0
