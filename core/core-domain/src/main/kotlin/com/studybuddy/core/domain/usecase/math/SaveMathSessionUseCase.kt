package com.studybuddy.core.domain.usecase.math

import com.studybuddy.core.domain.model.MathSession
import com.studybuddy.core.domain.repository.MathRepository
import javax.inject.Inject

class SaveMathSessionUseCase @Inject constructor(private val repository: MathRepository) {
    suspend operator fun invoke(session: MathSession) {
        repository.saveSession(session)
    }
}
