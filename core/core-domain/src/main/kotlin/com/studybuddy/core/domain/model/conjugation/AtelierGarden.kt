package com.studybuddy.core.domain.model.conjugation

import com.studybuddy.core.domain.model.srs.LeitnerGrowth

/** One verb's row in the garden: a growth stage per tense. */
data class AtelierVerbGarden(
    val verb: ConjugationVerb,
    val growth: Map<ConjugationTense, LeitnerGrowth>,
)

/**
 * The whole garden for one profile.
 *
 * @property dueCardCount Cards (verb, tense, person) due for review now.
 * @property dueVerbCount Distinct verbs with at least one due card — the
 * number shown in the "verbes à arroser" nudge.
 * @property verbs All Atelier verbs in roster order (grouped by verb group).
 */
data class AtelierGarden(
    val dueCardCount: Int,
    val dueVerbCount: Int,
    val verbs: List<AtelierVerbGarden>,
)
