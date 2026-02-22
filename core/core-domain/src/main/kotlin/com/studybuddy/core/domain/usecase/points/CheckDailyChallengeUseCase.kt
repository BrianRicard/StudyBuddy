package com.studybuddy.core.domain.usecase.points

import com.studybuddy.core.domain.repository.PointsRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CheckDailyChallengeUseCase @Inject constructor(
    private val pointsRepository: PointsRepository,
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(profileId: String): Flow<DailyChallengeStatus> =
        combine(
            pointsRepository.getPointsToday(profileId),
            settingsRepository.getDailyGoal(),
        ) { pointsToday, goal ->
            DailyChallengeStatus(
                activitiesCompleted = pointsToday,
                goal = goal,
                isComplete = pointsToday >= goal,
            )
        }
}

data class DailyChallengeStatus(val activitiesCompleted: Int, val goal: Int, val isComplete: Boolean)
