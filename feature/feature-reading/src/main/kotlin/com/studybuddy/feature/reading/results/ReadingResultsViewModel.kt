package com.studybuddy.feature.reading.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.ReadingResult
import com.studybuddy.core.domain.repository.ReadingRepository
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.points.RewardCalculator
import com.studybuddy.shared.points.RewardInput
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReadingResultsState(
    val passageTitle: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val pointsEarned: Int = 0,
    val allCorrectFirstTry: Boolean = false,
    val isLoading: Boolean = true,
)

sealed interface ReadingResultsIntent {
    data object ReadAgain : ReadingResultsIntent
    data object GoHome : ReadingResultsIntent
}

sealed interface ReadingResultsEffect {
    data class NavigateToPassage(val passageId: String) : ReadingResultsEffect
    data object NavigateHome : ReadingResultsEffect
}

@HiltViewModel
class ReadingResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val readingRepository: ReadingRepository,
    private val rewardCalculator: RewardCalculator,
    private val awardPointsUseCase: AwardPointsUseCase,
) : ViewModel() {

    private val passageId: String = checkNotNull(savedStateHandle["passageId"])
    private val score: Int = savedStateHandle["score"] ?: 0
    private val totalQuestions: Int = savedStateHandle["totalQuestions"] ?: 0
    private val readingTimeMs: Long = savedStateHandle["readingTimeMs"] ?: 0L
    private val questionsTimeMs: Long = savedStateHandle["questionsTimeMs"] ?: 0L
    private val allCorrectFirstTry: Boolean = savedStateHandle["allCorrectFirstTry"] ?: false
    private val tier: Int = savedStateHandle["tier"] ?: 1

    private val _state = MutableStateFlow(ReadingResultsState())
    val state: StateFlow<ReadingResultsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ReadingResultsEffect>()
    val effects: SharedFlow<ReadingResultsEffect> = _effects.asSharedFlow()

    init {
        calculateAndSaveResults()
    }

    fun onIntent(intent: ReadingResultsIntent) {
        when (intent) {
            is ReadingResultsIntent.ReadAgain -> {
                viewModelScope.launch {
                    _effects.emit(ReadingResultsEffect.NavigateToPassage(passageId))
                }
            }
            is ReadingResultsIntent.GoHome -> {
                viewModelScope.launch {
                    _effects.emit(ReadingResultsEffect.NavigateHome)
                }
            }
        }
    }

    private fun calculateAndSaveResults() {
        viewModelScope.launch {
            val passage = readingRepository.getPassageById(passageId)
            val rewardResult = rewardCalculator.calculate(
                RewardInput.ReadingReward(
                    correctAnswers = score,
                    totalQuestions = totalQuestions,
                    tier = tier,
                    allCorrectFirstTry = allCorrectFirstTry,
                ),
            )

            val result = ReadingResult(
                passageId = passageId,
                score = score,
                totalQuestions = totalQuestions,
                pointsEarned = rewardResult.totalPoints,
                readingTimeMs = readingTimeMs,
                questionsTimeMs = questionsTimeMs,
                completedAt = System.currentTimeMillis(),
                allCorrectFirstTry = allCorrectFirstTry,
            )
            readingRepository.saveResult(result)

            awardPointsUseCase(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                basePoints = rewardResult.totalPoints,
                streak = 0,
                source = PointSource.READING,
                reason = "Reading: ${passage?.title ?: passageId}",
            )

            _state.update {
                it.copy(
                    passageTitle = passage?.title ?: "",
                    score = score,
                    totalQuestions = totalQuestions,
                    pointsEarned = rewardResult.totalPoints,
                    allCorrectFirstTry = allCorrectFirstTry,
                    isLoading = false,
                )
            }
        }
    }
}
