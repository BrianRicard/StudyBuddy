package com.studybuddy.core.domain.usecase.poem

import com.studybuddy.core.domain.repository.PoemRepository
import javax.inject.Inject

class DeleteUserPoemUseCase @Inject constructor(
    private val repository: PoemRepository,
) {
    suspend operator fun invoke(poemId: String) {
        repository.deleteUserPoem(poemId)
    }
}
