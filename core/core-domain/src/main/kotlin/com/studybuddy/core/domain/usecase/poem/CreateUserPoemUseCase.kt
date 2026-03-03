package com.studybuddy.core.domain.usecase.poem

import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.repository.PoemRepository
import javax.inject.Inject

class CreateUserPoemUseCase @Inject constructor(
    private val repository: PoemRepository,
) {
    suspend operator fun invoke(
        poem: Poem,
        profileId: String,
    ) {
        repository.createUserPoem(poem, profileId)
    }
}
