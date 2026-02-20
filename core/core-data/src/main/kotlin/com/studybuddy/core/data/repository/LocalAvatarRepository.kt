package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.AvatarDao
import com.studybuddy.core.data.db.entity.AvatarConfigEntity
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.repository.AvatarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAvatarRepository @Inject constructor(
    private val dao: AvatarDao,
) : AvatarRepository {

    override fun getAvatarConfig(profileId: String): Flow<AvatarConfig?> =
        dao.getAvatarConfig(profileId).map { entity ->
            entity?.let {
                AvatarConfig(
                    bodyId = it.bodyId,
                    hatId = it.hatId,
                    faceId = it.faceId,
                    outfitId = it.outfitId,
                    petId = it.petId,
                    equippedTitle = it.equippedTitle,
                )
            }
        }

    override suspend fun saveAvatarConfig(profileId: String, config: AvatarConfig) {
        dao.insert(
            AvatarConfigEntity(
                id = java.util.UUID.randomUUID().toString(),
                profileId = profileId,
                bodyId = config.bodyId,
                hatId = config.hatId,
                faceId = config.faceId,
                outfitId = config.outfitId,
                petId = config.petId,
                equippedTitle = config.equippedTitle,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
