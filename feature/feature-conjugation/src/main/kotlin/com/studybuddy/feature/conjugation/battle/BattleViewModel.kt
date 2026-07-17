package com.studybuddy.feature.conjugation.battle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.conjugation.BattleRound
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.usecase.avatar.GrantCharacterUseCase
import com.studybuddy.core.domain.usecase.conjugation.BuildBattleRoundsUseCase
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.tts.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BattlePhase {
    QUESTION,
    CORRECT,
    ENCOURAGE,
    GIFT,
    WON,
}

data class BattleState(
    val stage: ConjugationStage? = null,
    val queue: List<BattleRound> = emptyList(),
    val totalRounds: Int = 0,
    val cheeredCount: Int = 0,
    val phase: BattlePhase = BattlePhase.QUESTION,
    val selectedOption: String? = null,
    val firstTryCorrect: Int = 0,
    /** Rounds missed at least once, keyed by (verb, person) — a retry is not a first try. */
    val missedRoundKeys: Set<Pair<String, ConjugationPerson>> = emptySet(),
    /** True once the lucky ladybug has been unlocked into the child's closet. */
    val ladybugUnlocked: Boolean = false,
    val isSaving: Boolean = false,
) {
    val currentRound: BattleRound? get() = queue.firstOrNull()

    /** 0..1 fill of the friend's cheer meter. */
    val cheerProgress: Float
        get() = if (totalRounds == 0) 0f else cheeredCount.toFloat() / totalRounds
}

sealed interface BattleIntent {
    data class SelectOption(val option: String) : BattleIntent
    data object Continue : BattleIntent
    data object Finish : BattleIntent
}

sealed interface BattleEffect {
    data object Completed : BattleEffect
}

@HiltViewModel
class BattleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    buildBattleRounds: BuildBattleRoundsUseCase,
    private val conjugationRepository: ConjugationRepository,
    private val awardPointsUseCase: AwardPointsUseCase,
    private val grantCharacterUseCase: GrantCharacterUseCase,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val stageId: String = checkNotNull(savedStateHandle["stageId"])

    private val _state = MutableStateFlow(
        ConjugationStages.byId(stageId)?.let { stage ->
            val rounds = buildBattleRounds(stage)
            BattleState(stage = stage, queue = rounds, totalRounds = rounds.size)
        } ?: BattleState(),
    )
    val state: StateFlow<BattleState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<BattleEffect>()
    val effects: SharedFlow<BattleEffect> = _effects.asSharedFlow()

    fun onIntent(intent: BattleIntent) {
        when (intent) {
            is BattleIntent.SelectOption -> selectOption(intent.option)
            is BattleIntent.Continue -> continueBattle()
            is BattleIntent.Finish -> finish()
        }
    }

    private fun selectOption(option: String) {
        val current = _state.value
        val round = current.currentRound ?: return
        if (current.phase != BattlePhase.QUESTION) return

        val roundKey = round.verb.id to round.person
        if (option == round.correctForm) {
            ttsManager.speak(round.verb.display(round.person), Locale.FRENCH)
            _state.update {
                it.copy(
                    phase = BattlePhase.CORRECT,
                    selectedOption = option,
                    cheeredCount = it.cheeredCount + 1,
                    firstTryCorrect = it.firstTryCorrect + if (roundKey in it.missedRoundKeys) 0 else 1,
                )
            }
        } else {
            // Never punishing: the round goes to the back of the queue and the
            // battle can always be won.
            _state.update {
                it.copy(
                    phase = BattlePhase.ENCOURAGE,
                    selectedOption = option,
                    missedRoundKeys = it.missedRoundKeys + roundKey,
                )
            }
        }
    }

    private fun continueBattle() {
        val current = _state.value
        when (current.phase) {
            BattlePhase.CORRECT -> {
                val remaining = current.queue.drop(1)
                if (remaining.isEmpty()) {
                    winBattle()
                } else {
                    _state.update {
                        it.copy(
                            queue = remaining,
                            phase = BattlePhase.QUESTION,
                            selectedOption = null,
                        )
                    }
                }
            }

            BattlePhase.ENCOURAGE -> {
                val round = current.currentRound ?: return
                _state.update {
                    it.copy(
                        queue = it.queue.drop(1) + round,
                        phase = BattlePhase.QUESTION,
                        selectedOption = null,
                    )
                }
            }

            // The gift moment can be skipped — no forced waiting.
            BattlePhase.GIFT -> _state.update { it.copy(phase = BattlePhase.WON) }

            else -> Unit
        }
    }

    private fun winBattle() {
        _state.update { it.copy(phase = BattlePhase.GIFT) }
        viewModelScope.launch {
            // The lucky ladybug from the gift also joins the child's closet.
            val newlyUnlocked = grantCharacterUseCase(AppConstants.DEFAULT_PROFILE_ID, LADYBUG_BODY_ID)
            _state.update { it.copy(ladybugUnlocked = newlyUnlocked) }
            delay(GIFT_ANIMATION_MS)
            _state.update { it.copy(phase = BattlePhase.WON) }
        }
    }

    private fun finish() {
        val current = _state.value
        if (current.phase != BattlePhase.WON || current.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            conjugationRepository.recordStepResult(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                stageId = stageId,
                step = ConjugationStep.BATTLE,
                correct = current.firstTryCorrect,
                total = current.totalRounds,
            )
            awardPointsUseCase(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                basePoints = PointValues.CONJUGATION_BATTLE_WIN,
                streak = 0,
                source = PointSource.CONJUGATION,
                reason = "Conjugation battle won: $stageId",
            )
            _effects.emit(BattleEffect.Completed)
        }
    }

    override fun onCleared() {
        ttsManager.stop()
        super.onCleared()
    }

    private companion object {
        const val GIFT_ANIMATION_MS = 2_200L
        const val LADYBUG_BODY_ID = "ladybug"
    }
}
