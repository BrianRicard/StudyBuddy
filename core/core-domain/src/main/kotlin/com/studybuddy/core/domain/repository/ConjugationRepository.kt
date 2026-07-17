package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.conjugation.ConjugationProgress
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import kotlinx.coroutines.flow.Flow

/**
 * Outcome of recording a step run, so callers can award one-time bonuses
 * (stage completion, new best) without re-deriving state from the flow.
 */
data class StepResultOutcome(
    val firstCompletion: Boolean,
    val newBest: Boolean,
)

interface ConjugationRepository {

    fun getProgressForProfile(profileId: String): Flow<List<ConjugationProgress>>

    /**
     * Records a finished run of [step].
     *
     * Contract (never-punishing by design):
     * - Any finished run completes the step, whatever the score.
     * - The best score is kept: higher correct/total ratio wins, ties broken
     *   by the larger total. Worse runs leave the stored record untouched.
     * - [correct] must be within 0..[total].
     */
    suspend fun recordStepResult(
        profileId: String,
        stageId: String,
        step: ConjugationStep,
        correct: Int,
        total: Int,
    ): StepResultOutcome

    suspend fun sync() // Cloud migration hook
}
