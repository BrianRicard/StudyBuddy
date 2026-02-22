package com.studybuddy.core.data.mapper

import com.studybuddy.core.data.db.entity.ProfileEntity
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.Profile
import kotlinx.datetime.Instant

fun ProfileEntity.toDomain() =
    Profile(
        id = id,
        name = name,
        avatarConfig = AvatarConfig(
            bodyId = bodyId,
            hatId = hatId,
            faceId = faceId,
            outfitId = outfitId,
            petId = petId,
            equippedTitle = equippedTitle,
        ),
        locale = locale,
        totalPoints = totalPoints,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
    )

fun Profile.toEntity() =
    ProfileEntity(
        id = id,
        name = name,
        locale = locale,
        totalPoints = totalPoints,
        bodyId = avatarConfig.bodyId,
        hatId = avatarConfig.hatId,
        faceId = avatarConfig.faceId,
        outfitId = avatarConfig.outfitId,
        petId = avatarConfig.petId,
        equippedTitle = avatarConfig.equippedTitle,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt.toEpochMilliseconds(),
    )
