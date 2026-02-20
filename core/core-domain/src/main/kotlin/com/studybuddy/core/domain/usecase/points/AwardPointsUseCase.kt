package com.studybuddy.core.domain.usecase.points

import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.repository.PointsRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

class AwardPointsUseCase @Inject constructor(
    private val repository: PointsRepository,
) {
    suspend operator fun invoke(
        profileId: String,
        basePoints: Int,
        streak: Int,
        source: PointSource,
        reason: String,
    ) {
        val multiplier = PointValues.streakMultiplier(streak)
        val finalPoints = (basePoints * multiplier).toInt()
        val event = PointEvent(
            id = java.util.UUID.randomUUID().toString(),
            profileId = profileId,
            source = source,
            points = finalPoints,
            reason = reason,
            timestamp = Clock.System.now(),
        )
        repository.addPointEvent(event)
    }
}
