package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.MathSession
import kotlinx.coroutines.flow.Flow

interface MathRepository {
    fun getSessionsForProfile(profileId: String): Flow<List<MathSession>>
    fun getSession(sessionId: String): Flow<MathSession?>
    suspend fun saveSession(session: MathSession)
    suspend fun deleteSession(sessionId: String)
    suspend fun sync()
}
