package com.studybuddy.feature.conjugation.boss

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.conjugation.ConjugationStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.tts.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A tappable word chip in the boss word bank. */
data class WordChip(
    val id: Int,
    val text: String,
    val isUsed: Boolean = false,
)

enum class BossPhase {
    INTRO,
    BUILD,
    SENTENCE_DONE,
    WON,
}

data class BossState(
    val stage: ConjugationStage? = null,
    val sentenceIndex: Int = 0,
    val bank: List<WordChip> = emptyList(),
    val builtWords: List<String> = emptyList(),
    val phase: BossPhase = BossPhase.INTRO,
    val mistakes: Int = 0,
    val shakeChipId: Int? = null,
    /** Bumped on every wrong tap so re-tapping the same chip re-triggers the shake. */
    val shakeEvent: Int = 0,
    val isSaving: Boolean = false,
) {
    val sentences: List<String> get() = stage?.verb?.bossSentences.orEmpty()
    val currentSentence: String? get() = sentences.getOrNull(sentenceIndex)
    val targetWords: List<String> get() = currentSentence?.split(" ").orEmpty()
    val totalWords: Int get() = sentences.sumOf { it.split(" ").size }
    val isFinalStage: Boolean get() = stage?.order == ConjugationStages.all.size
}

sealed interface BossIntent {
    data object PlayRap : BossIntent
    data object StartBuilding : BossIntent
    data class TapChip(val chipId: Int) : BossIntent
    data object NextSentence : BossIntent
    data object Finish : BossIntent
}

sealed interface BossEffect {
    data object Completed : BossEffect
}

@HiltViewModel
class BossViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val conjugationRepository: ConjugationRepository,
    private val awardPointsUseCase: AwardPointsUseCase,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val stageId: String = checkNotNull(savedStateHandle["stageId"])
    private val random = Random(System.currentTimeMillis())

    private val _state = MutableStateFlow(BossState(stage = ConjugationStages.byId(stageId)))
    val state: StateFlow<BossState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<BossEffect>()
    val effects: SharedFlow<BossEffect> = _effects.asSharedFlow()

    fun onIntent(intent: BossIntent) {
        when (intent) {
            is BossIntent.PlayRap -> playRap()
            is BossIntent.StartBuilding -> startBuilding()
            is BossIntent.TapChip -> tapChip(intent.chipId)
            is BossIntent.NextSentence -> nextSentence()
            is BossIntent.Finish -> finish()
        }
    }

    private fun playRap() {
        val sentence = _state.value.currentSentence ?: return
        ttsManager.speak(sentence, Locale.FRENCH)
    }

    private fun startBuilding() {
        val current = _state.value
        if (current.phase != BossPhase.INTRO) return
        playRap()
        _state.update { it.copy(phase = BossPhase.BUILD, bank = shuffledBank(it.targetWords)) }
    }

    private fun shuffledBank(words: List<String>): List<WordChip> =
        words.mapIndexed { index, word -> WordChip(id = index, text = word) }.shuffled(random)

    private fun tapChip(chipId: Int) {
        val current = _state.value
        if (current.phase != BossPhase.BUILD) return
        val chip = current.bank.firstOrNull { it.id == chipId && !it.isUsed } ?: return
        val expected = current.targetWords.getOrNull(current.builtWords.size) ?: return

        if (chip.text == expected) {
            val built = current.builtWords + chip.text
            val bank = current.bank.map { if (it.id == chipId) it.copy(isUsed = true) else it }
            val sentenceDone = built.size == current.targetWords.size
            _state.update {
                it.copy(
                    builtWords = built,
                    bank = bank,
                    shakeChipId = null,
                    phase = if (sentenceDone) BossPhase.SENTENCE_DONE else BossPhase.BUILD,
                )
            }
            if (sentenceDone) {
                // Replay the full line as a little victory echo.
                current.currentSentence?.let { ttsManager.speak(it, Locale.FRENCH) }
            }
        } else {
            // Gentle nudge only: the chip shakes, nothing is lost.
            _state.update {
                it.copy(
                    mistakes = it.mistakes + 1,
                    shakeChipId = chipId,
                    shakeEvent = it.shakeEvent + 1,
                )
            }
        }
    }

    private fun nextSentence() {
        val current = _state.value
        if (current.phase != BossPhase.SENTENCE_DONE) return
        val nextIndex = current.sentenceIndex + 1
        if (nextIndex < current.sentences.size) {
            _state.update {
                it.copy(
                    sentenceIndex = nextIndex,
                    builtWords = emptyList(),
                    phase = BossPhase.BUILD,
                    shakeChipId = null,
                )
            }
            // Recompute the bank for the new sentence, then rap it.
            _state.update { it.copy(bank = shuffledBank(it.targetWords)) }
            playRap()
        } else {
            _state.update { it.copy(phase = BossPhase.WON) }
        }
    }

    private fun finish() {
        val current = _state.value
        if (current.phase != BossPhase.WON || current.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val total = current.totalWords
            val correct = (total - current.mistakes).coerceAtLeast(0)
            val outcome = conjugationRepository.recordStepResult(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                stageId = stageId,
                step = ConjugationStep.BOSS,
                correct = correct,
                total = total,
            )
            awardPointsUseCase(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                basePoints = PointValues.CONJUGATION_BOSS_WIN,
                streak = 0,
                source = PointSource.CONJUGATION,
                reason = "Conjugation boss beaten: $stageId",
            )
            // The boss is the last step, so its first completion completes the stage.
            if (outcome.firstCompletion) {
                awardPointsUseCase(
                    profileId = AppConstants.DEFAULT_PROFILE_ID,
                    basePoints = PointValues.CONJUGATION_STAGE_COMPLETE,
                    streak = 0,
                    source = PointSource.CONJUGATION,
                    reason = "Conjugation stage complete: $stageId",
                )
            }
            _effects.emit(BossEffect.Completed)
        }
    }

    override fun onCleared() {
        ttsManager.stop()
        super.onCleared()
    }
}
