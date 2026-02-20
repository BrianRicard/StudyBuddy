package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.MathDao
import com.studybuddy.core.data.mapper.toDomain
import com.studybuddy.core.data.mapper.toEntity
import com.studybuddy.core.domain.model.MathSession
import com.studybuddy.core.domain.repository.MathRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMathRepository @Inject constructor(
    private val dao: MathDao,
) : MathRepository {

    override fun getSessionsForProfile(profileId: String): Flow<List<MathSession>> =
        dao.getSessionsForProfile(profileId).map { sessions -> sessions.map { it.toDomain() } }

    override fun getSession(sessionId: String): Flow<MathSession?> =
        dao.getSession(sessionId).map { it?.toDomain() }

    override suspend fun saveSession(session: MathSession) {
        dao.insert(session.toEntity())
    }

    override suspend fun deleteSession(sessionId: String) {
        dao.delete(sessionId)
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
