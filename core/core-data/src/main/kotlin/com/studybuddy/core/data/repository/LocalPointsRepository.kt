package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.PointsDao
import com.studybuddy.core.data.db.entity.PointEventEntity
import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.repository.PointsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@Singleton
class LocalPointsRepository @Inject constructor(private val dao: PointsDao) : PointsRepository {

    override fun getPointsForProfile(profileId: String): Flow<List<PointEvent>> =
        dao.getPointsForProfile(profileId).map { events ->
            events.map { it.toDomain() }
        }

    override fun getTotalPoints(profileId: String): Flow<Long> = dao.getTotalPoints(profileId)

    override fun getPointsToday(profileId: String): Flow<Int> {
        val tz = TimeZone.currentSystemDefault()
        val startOfDay = Clock.System.now()
            .toLocalDateTime(tz).date
            .atStartOfDayIn(tz)
            .toEpochMilliseconds()
        return dao.getPointsToday(profileId, startOfDay)
    }

    override fun getSessionsToday(profileId: String): Flow<Int> {
        val tz = TimeZone.currentSystemDefault()
        val startOfDay = Clock.System.now()
            .toLocalDateTime(tz).date
            .atStartOfDayIn(tz)
            .toEpochMilliseconds()
        return dao.getSessionsToday(profileId, startOfDay)
    }

    override suspend fun addPointEvent(event: PointEvent) {
        dao.insert(event.toEntity())
    }

    override suspend fun deductPoints(
        profileId: String,
        amount: Int,
        reason: String,
    ) {
        val deduction = PointEventEntity(
            id = java.util.UUID.randomUUID().toString(),
            profileId = profileId,
            source = PointSource.PURCHASE.name,
            points = -amount,
            reason = reason,
            timestamp = Clock.System.now().toEpochMilliseconds(),
        )
        dao.insert(deduction)
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }

    private fun PointEventEntity.toDomain() = PointEvent(
        id = id,
        profileId = profileId,
        source = runCatching { PointSource.valueOf(source) }.getOrDefault(PointSource.MATH),
        points = points,
        reason = reason,
        timestamp = Instant.fromEpochMilliseconds(timestamp),
    )

    private fun PointEvent.toEntity() = PointEventEntity(
        id = id,
        profileId = profileId,
        source = source.name,
        points = points,
        reason = reason,
        timestamp = timestamp.toEpochMilliseconds(),
    )
}
