package com.studybuddy.core.domain.model

/**
 * A thing the child can be asked to practise — one per card on the Home screen.
 *
 * Deliberately separate from [PointSource], which is coarser: `MATH` covers Speed
 * Math, the tables garden and the arcade challenge alike, and a parent setting
 * "two Speed Math sessions" does not mean "two rounds of falling equations".
 */
enum class LearningMode {
    DICTEE,
    SPEED_MATH,
    VERB_QUEST,
    POEMS,
    READING,
    MATH_CHALLENGE,
}
