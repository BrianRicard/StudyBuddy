package com.studybuddy.core.domain.usecase.poem

import com.studybuddy.core.domain.model.ReadingSession
import com.studybuddy.core.domain.repository.PoemRepository
import javax.inject.Inject

class SaveReadingSessionUseCase @Inject constructor(
    private val repository: PoemRepository,
) {
    suspend operator fun invoke(session: ReadingSession) {
        repository.saveReadingSession(session)
    }
}
