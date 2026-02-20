package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.VoicePack
import com.studybuddy.core.domain.model.VoicePackStatus
import kotlinx.coroutines.flow.Flow

interface VoicePackRepository {
    fun getVoicePacks(): Flow<List<VoicePack>>
    fun getVoicePack(id: String): Flow<VoicePack?>
    suspend fun updateVoicePackStatus(id: String, status: VoicePackStatus)
    suspend fun sync()
}
