package com.studybuddy.core.domain.usecase.avatar

import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.repository.AvatarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvatarConfigUseCase @Inject constructor(
    private val repository: AvatarRepository,
) {
    operator fun invoke(profileId: String): Flow<AvatarConfig?> =
        repository.getAvatarConfig(profileId)
}
