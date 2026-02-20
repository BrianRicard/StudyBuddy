package com.studybuddy.core.domain.usecase.points

import com.studybuddy.core.domain.repository.PointsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTotalPointsUseCase @Inject constructor(
    private val repository: PointsRepository,
) {
    operator fun invoke(profileId: String): Flow<Long> =
        repository.getTotalPoints(profileId)
}
