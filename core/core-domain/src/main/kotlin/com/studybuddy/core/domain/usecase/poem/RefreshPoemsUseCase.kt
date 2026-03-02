package com.studybuddy.core.domain.usecase.poem

import com.studybuddy.core.domain.repository.PoemRepository
import javax.inject.Inject

class RefreshPoemsUseCase @Inject constructor(
    private val repository: PoemRepository,
) {
    suspend operator fun invoke(language: String) {
        repository.refreshPoems(language)
    }
}
