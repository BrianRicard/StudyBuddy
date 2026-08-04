package com.studybuddy.core.domain.usecase.plan

import com.studybuddy.core.domain.model.LearningMode
import com.studybuddy.core.domain.repository.ParentPlanRepository
import javax.inject.Inject
import kotlinx.datetime.Clock

/**
 * Records that the child finished one session of [LearningMode].
 *
 * Call this once per *session*, at the point the results screen is reached — never
 * per question or per card, which is what makes this record trustworthy where the
 * points ledger is not.
 */
class RecordSessionUseCase @Inject constructor(
    private val repository: ParentPlanRepository,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(
        profileId: String,
        mode: LearningMode,
    ) {
        repository.recordSession(profileId = profileId, mode = mode, completedAt = clock.now())
    }
}
