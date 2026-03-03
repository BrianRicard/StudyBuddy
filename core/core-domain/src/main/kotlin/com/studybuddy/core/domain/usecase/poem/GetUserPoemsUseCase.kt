package com.studybuddy.core.domain.usecase.poem

import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.repository.PoemRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetUserPoemsUseCase @Inject constructor(
    private val repository: PoemRepository,
) {
    operator fun invoke(profileId: String): Flow<List<Poem>> = repository.getUserPoems(profileId)
}
