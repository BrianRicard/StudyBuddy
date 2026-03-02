package com.studybuddy.core.domain.usecase.poem

import com.studybuddy.core.domain.repository.PoemRepository
import javax.inject.Inject

class ToggleFavouriteUseCase @Inject constructor(
    private val repository: PoemRepository,
) {
    suspend operator fun invoke(
        poemId: String,
        poemSource: String,
        profileId: String,
    ) {
        repository.toggleFavourite(poemId, poemSource, profileId)
    }
}
