package com.studybuddy.core.domain.usecase.avatar

import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.repository.AvatarRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAvatarConfigUseCase @Inject constructor(private val repository: AvatarRepository) {
    operator fun invoke(profileId: String): Flow<AvatarConfig?> =
        repository.getAvatarConfig(profileId)
}
