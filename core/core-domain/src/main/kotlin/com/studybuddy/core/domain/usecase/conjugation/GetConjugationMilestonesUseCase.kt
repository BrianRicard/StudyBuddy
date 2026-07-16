package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationMilestone
import com.studybuddy.core.domain.model.conjugation.ConjugationPathStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.model.conjugation.MilestoneStatus
import javax.inject.Inject
import kotlinx.datetime.Instant

/**
 * Derives milestone statuses from path progress. Achievement timestamps are
 * taken from the completion time of the step that clinched the milestone,
 * so parents can see exactly when each goal was reached.
 */
class GetConjugationMilestonesUseCase @Inject constructor() {

    operator fun invoke(pathStages: List<ConjugationPathStage>): List<MilestoneStatus> {
        val allProgress = pathStages.flatMap { it.stepProgress.values }
        val completedStages = pathStages.filter { it.isCompleted }
        val stageCount = ConjugationStages.all.size

        val firstStepAt = allProgress.mapNotNull { it.completedAt }.minOrNull()
        val stageCompletionTimes = completedStages
            .mapNotNull { stage -> stage.stepProgress.values.mapNotNull { it.completedAt }.maxOrNull() }
            .sorted()

        val perfectStages = completedStages.filter { stage ->
            SCORED_STEPS.all { stage.stepProgress[it]?.isPerfect == true }
        }
        val isPerfectQuest = completedStages.size == stageCount && perfectStages.size == stageCount

        // A stage can become perfect on a replay long after it was completed,
        // so the timestamp comes from when the winning scores were achieved.
        val perfectAt = perfectStages
            .flatMap { stage -> SCORED_STEPS.mapNotNull { stage.stepProgress[it]?.updatedAt } }
            .maxOrNull()

        return listOf(
            MilestoneStatus(
                milestone = ConjugationMilestone.FIRST_STEP,
                current = if (firstStepAt != null) 1 else 0,
                target = 1,
                achievedAt = firstStepAt,
            ),
            stageMilestone(
                milestone = ConjugationMilestone.FIRST_VERB,
                target = 1,
                completionTimes = stageCompletionTimes,
            ),
            stageMilestone(
                milestone = ConjugationMilestone.THREE_VERBS,
                target = 3,
                completionTimes = stageCompletionTimes,
            ),
            stageMilestone(
                milestone = ConjugationMilestone.ALL_VERBS,
                target = stageCount,
                completionTimes = stageCompletionTimes,
            ),
            MilestoneStatus(
                milestone = ConjugationMilestone.PERFECT_QUEST,
                current = perfectStages.size,
                target = stageCount,
                achievedAt = if (isPerfectQuest) perfectAt else null,
            ),
        )
    }

    private fun stageMilestone(
        milestone: ConjugationMilestone,
        target: Int,
        completionTimes: List<Instant>,
    ) = MilestoneStatus(
        milestone = milestone,
        current = completionTimes.size.coerceAtMost(target),
        target = target,
        achievedAt = completionTimes.getOrNull(target - 1),
    )

    private companion object {
        /** Steps whose scores count toward a perfect quest. */
        val SCORED_STEPS = setOf(ConjugationStep.WRITE, ConjugationStep.BATTLE, ConjugationStep.BOSS)
    }
}
