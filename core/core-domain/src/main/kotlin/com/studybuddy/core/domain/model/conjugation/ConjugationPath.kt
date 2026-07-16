package com.studybuddy.core.domain.model.conjugation

/**
 * A stage on the quest path together with the child's progress through it.
 */
data class ConjugationPathStage(
    val stage: ConjugationStage,
    val stepProgress: Map<ConjugationStep, ConjugationProgress>,
    val isUnlocked: Boolean,
) {
    val completedStepCount: Int
        get() = ConjugationStep.entries.count { stepProgress[it]?.isCompleted == true }

    val isCompleted: Boolean
        get() = completedStepCount == ConjugationStep.entries.size

    /** The next step to play, or null when the stage is fully completed. */
    val nextStep: ConjugationStep?
        get() = ConjugationStep.entries.firstOrNull { stepProgress[it]?.isCompleted != true }

    /** A step is playable once every step before it is completed. */
    fun isStepUnlocked(step: ConjugationStep): Boolean =
        isUnlocked && ConjugationStep.entries
            .takeWhile { it != step }
            .all { stepProgress[it]?.isCompleted == true }
}
