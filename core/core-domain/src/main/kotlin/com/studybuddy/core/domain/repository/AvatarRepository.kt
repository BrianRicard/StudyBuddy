package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.AvatarConfig
import kotlinx.coroutines.flow.Flow

interface AvatarRepository {
    fun getAvatarConfig(profileId: String): Flow<AvatarConfig?>
    suspend fun saveAvatarConfig(profileId: String, config: AvatarConfig)
    suspend fun sync()
}
