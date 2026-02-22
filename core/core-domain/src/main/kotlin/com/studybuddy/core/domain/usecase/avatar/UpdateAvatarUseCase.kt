package com.studybuddy.core.domain.usecase.avatar

import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.repository.AvatarRepository
import javax.inject.Inject

class UpdateAvatarUseCase @Inject constructor(private val repository: AvatarRepository) {
    suspend operator fun invoke(
        profileId: String,
        config: AvatarConfig,
    ) {
        repository.saveAvatarConfig(profileId, config)
    }
}
