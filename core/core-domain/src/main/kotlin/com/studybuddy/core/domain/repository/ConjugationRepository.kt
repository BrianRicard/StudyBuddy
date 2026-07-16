package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.conjugation.ConjugationProgress
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import kotlinx.coroutines.flow.Flow

interface ConjugationRepository {

    fun getProgressForProfile(profileId: String): Flow<List<ConjugationProgress>>

    /**
     * Records a finished run of [step]. Keeps the best score across runs and
     * the earliest completion timestamp.
     */
    suspend fun recordStepResult(
        profileId: String,
        stageId: String,
        step: ConjugationStep,
        correct: Int,
        total: Int,
    )

    suspend fun sync() // Cloud migration hook
}
