package com.studybuddy.shared.points

import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.repository.PointsRepository
import javax.inject.Inject
import kotlinx.datetime.Clock

class AwardPointsUseCase @Inject constructor(private val repository: PointsRepository) {
    suspend operator fun invoke(
        profileId: String,
        basePoints: Int,
        streak: Int,
        source: PointSource,
        reason: String,
    ): Int {
        val finalPoints = PointsCalculator.applyMultiplier(basePoints, streak)
        val event = PointEvent(
            id = java.util.UUID.randomUUID().toString(),
            profileId = profileId,
            source = source,
            points = finalPoints,
            reason = reason,
            timestamp = Clock.System.now(),
        )
        repository.addPointEvent(event)
        return finalPoints
    }
}
