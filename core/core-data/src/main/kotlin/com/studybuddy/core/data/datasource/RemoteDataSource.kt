package com.studybuddy.core.data.datasource

/**
 * Stub interface for future cloud sync.
 * When cloud migration is needed, implement this interface
 * and inject it into Cloud*Repository implementations.
 */
interface RemoteDataSource {
    suspend fun syncProfiles()
    suspend fun syncDicteeLists()
    suspend fun syncMathSessions()
    suspend fun syncPoints()
    suspend fun syncRewards()
}
