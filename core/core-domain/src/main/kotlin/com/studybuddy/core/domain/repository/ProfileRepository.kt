package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(id: String): Flow<Profile?>
    fun getActiveProfile(): Flow<Profile?>
    suspend fun createProfile(profile: Profile)
    suspend fun updateProfile(profile: Profile)
    suspend fun deleteProfile(id: String)
    suspend fun sync()
}
