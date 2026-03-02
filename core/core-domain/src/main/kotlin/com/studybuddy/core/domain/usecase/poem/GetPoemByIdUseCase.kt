package com.studybuddy.core.domain.usecase.poem

import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.repository.PoemRepository
import javax.inject.Inject

class GetPoemByIdUseCase @Inject constructor(
    private val repository: PoemRepository,
) {
    suspend operator fun invoke(id: String): Poem? = repository.getPoemById(id)
}
