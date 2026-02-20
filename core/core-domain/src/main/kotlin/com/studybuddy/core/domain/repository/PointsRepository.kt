package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.PointEvent
import kotlinx.coroutines.flow.Flow

interface PointsRepository {
    fun getPointsForProfile(profileId: String): Flow<List<PointEvent>>
    fun getTotalPoints(profileId: String): Flow<Long>
    fun getPointsToday(profileId: String): Flow<Int>
    suspend fun addPointEvent(event: PointEvent)
    suspend fun deductPoints(profileId: String, amount: Int, reason: String)
    suspend fun sync()
}
