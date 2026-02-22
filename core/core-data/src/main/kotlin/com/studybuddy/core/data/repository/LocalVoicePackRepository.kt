package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.VoicePackDao
import com.studybuddy.core.domain.model.VoicePack
import com.studybuddy.core.domain.model.VoicePackStatus
import com.studybuddy.core.domain.repository.VoicePackRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LocalVoicePackRepository @Inject constructor(private val dao: VoicePackDao) : VoicePackRepository {

    override fun getVoicePacks(): Flow<List<VoicePack>> = dao.getAll().map { packs ->
        packs.map { entity ->
            VoicePack(
                id = entity.id,
                locale = entity.locale,
                displayName = entity.displayName,
                sizeBytes = entity.sizeBytes,
                status = runCatching {
                    VoicePackStatus.valueOf(entity.status)
                }.getOrDefault(VoicePackStatus.NOT_INSTALLED),
            )
        }
    }

    override fun getVoicePack(id: String): Flow<VoicePack?> = dao.getById(id).map { entity ->
        entity?.let {
            VoicePack(
                id = it.id,
                locale = it.locale,
                displayName = it.displayName,
                sizeBytes = it.sizeBytes,
                status = runCatching {
                    VoicePackStatus.valueOf(it.status)
                }.getOrDefault(VoicePackStatus.NOT_INSTALLED),
            )
        }
    }

    override suspend fun updateVoicePackStatus(
        id: String,
        status: VoicePackStatus,
    ) {
        dao.updateStatus(id, status.name)
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
