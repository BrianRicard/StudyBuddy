package com.studybuddy.core.domain.model.srs

/**
 * Growth stage of one garden cell, derived from the Leitner boxes of its
 * cards. Memory made visible: cards the child has planted and watered grow
 * from seed to tree. Shared by every spaced-repetition garden in the app.
 */
enum class LeitnerGrowth {
    SEED,
    SPROUT,
    FLOWER,
    TREE,
    ;

    companion object {
        private const val SPROUT_THRESHOLD = 2.0
        private const val FLOWER_THRESHOLD = 4.0

        /**
         * Maps a cell's per-card boxes (0 for never-seen cards) to a stage:
         * untouched → SEED, some progress → SPROUT, average box ≥ 2 → FLOWER,
         * every card at [LeitnerSchedule.MAX_BOX] → TREE.
         */
        fun fromBoxes(boxes: List<Int>): LeitnerGrowth {
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
