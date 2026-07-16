package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationPathStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import com.studybuddy.core.domain.repository.ConjugationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Emits the quest path: every stage with its progress and lock state.
 * Stage 1 starts unlocked; each later stage unlocks when the previous
 * one is fully completed.
 */
class GetConjugationPathUseCase @Inject constructor(
    private val repository: ConjugationRepository,
) {
    operator fun invoke(profileId: String): Flow<List<ConjugationPathStage>> =
        repository.getProgressForProfile(profileId).map { progress ->
            val byStage = progress.groupBy { it.stageId }
            var previousCompleted = true
            ConjugationStages.all.map { stage ->
                val stepProgress = byStage[stage.id].orEmpty().associateBy { it.step }
                val pathStage = ConjugationPathStage(
                    stage = stage,
                    stepProgress = stepProgress,
                    isUnlocked = previousCompleted,
                )
                previousCompleted = pathStage.isCompleted
                pathStage
            }
        }
}
