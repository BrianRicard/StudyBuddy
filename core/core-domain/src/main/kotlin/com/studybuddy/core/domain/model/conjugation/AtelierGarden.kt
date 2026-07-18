package com.studybuddy.core.domain.model.conjugation

/**
 * Growth stage of one garden cell (a verb × tense), derived from the Leitner
 * boxes of its six person-cards. Memory made visible: cards the child has
 * planted and watered grow from seed to tree.
 */
enum class AtelierGrowth {
    SEED,
    SPROUT,
    FLOWER,
    TREE,
    ;

    companion object {
        private const val SPROUT_THRESHOLD = 2.0
        private const val FLOWER_THRESHOLD = 4.0

        /**
         * Maps the six per-person boxes (0 for never-seen persons) to a stage:
         * untouched → SEED, some progress → SPROUT, average box ≥ 2 → FLOWER,
         * every person at [AtelierSchedule.MAX_BOX] → TREE.
         */
        fun fromBoxes(boxes: List<Int>): AtelierGrowth {
            val average = if (boxes.isEmpty()) 0.0 else boxes.average()
            return when {
                average == 0.0 -> SEED
                average < SPROUT_THRESHOLD -> SPROUT
                average < FLOWER_THRESHOLD -> FLOWER
                else -> TREE
            }
        }
    }
}

/** One verb's row in the garden: a growth stage per tense. */
data class AtelierVerbGarden(
    val verb: ConjugationVerb,
    val growth: Map<ConjugationTense, AtelierGrowth>,
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
