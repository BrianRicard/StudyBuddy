package com.studybuddy.core.ui.components

import androidx.annotation.StringRes
import com.studybuddy.core.ui.R

/**
 * Rotating praise for a correct answer, shared by every practice mode so a
 * child hears the same warm vocabulary everywhere.
 *
 * @param seed Usually a running count of correct answers, so the same word
 * never lands twice in a row.
 */
@StringRes
fun praiseRes(seed: Int): Int = PRAISE[Math.floorMod(seed, PRAISE.size)]

private val PRAISE = listOf(
    R.string.praise_1,
    R.string.praise_2,
    R.string.praise_3,
)
