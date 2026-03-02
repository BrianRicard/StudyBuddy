package com.studybuddy.feature.poems

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.ReadingSession
import com.studybuddy.core.domain.usecase.poem.GetPoemByIdUseCase
import com.studybuddy.core.domain.usecase.poem.SaveReadingSessionUseCase
import com.studybuddy.core.domain.usecase.poem.ToggleFavouriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class PoemDetailState(
    val poem: Poem? = null,
    val isLoading: Boolean = true,
    val currentReadLine: Int = -1,
    val isReadingAloud: Boolean = false,
    val readingScore: Float? = null,
)

sealed interface PoemDetailIntent {
    data object ToggleFavourite : PoemDetailIntent
    data object StartReadAloud : PoemDetailIntent
    data object StopReadAloud : PoemDetailIntent
    data class AdvanceReadLine(val lineIndex: Int) : PoemDetailIntent
    data class FinishReading(val score: Float, val durationSeconds: Int) : PoemDetailIntent
}

sealed interface PoemDetailEffect {
    data class SpeakLine(val text: String, val language: String) : PoemDetailEffect
    data object StopSpeaking : PoemDetailEffect
}

@HiltViewModel
class PoemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPoemByIdUseCase: GetPoemByIdUseCase,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
    private val saveReadingSessionUseCase: SaveReadingSessionUseCase,
) : ViewModel() {

    private val poemId: String = checkNotNull(savedStateHandle["poemId"])
    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    private val _state = MutableStateFlow(PoemDetailState())
    val state: StateFlow<PoemDetailState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PoemDetailEffect>()
    val effects: SharedFlow<PoemDetailEffect> = _effects.asSharedFlow()

    init {
        loadPoem()
    }

    fun onIntent(intent: PoemDetailIntent) {
        when (intent) {
            is PoemDetailIntent.ToggleFavourite -> toggleFavourite()
            is PoemDetailIntent.StartReadAloud -> startReadAloud()
            is PoemDetailIntent.StopReadAloud -> stopReadAloud()
            is PoemDetailIntent.AdvanceReadLine -> advanceReadLine(intent.lineIndex)
            is PoemDetailIntent.FinishReading -> finishReading(intent.score, intent.durationSeconds)
        }
    }

    private fun loadPoem() {
        viewModelScope.launch {
            val poem = getPoemByIdUseCase(poemId)
            _state.update { it.copy(poem = poem, isLoading = false) }
        }
    }

    private fun toggleFavourite() {
        val poem = _state.value.poem ?: return
        viewModelScope.launch {
            toggleFavouriteUseCase(poem.id, poem.source.name, profileId)
            _state.update {
                it.copy(poem = poem.copy(isFavourite = !poem.isFavourite))
            }
        }
    }

    private fun startReadAloud() {
        val poem = _state.value.poem ?: return
        _state.update { it.copy(isReadingAloud = true, currentReadLine = 0) }
        viewModelScope.launch {
            _effects.emit(PoemDetailEffect.SpeakLine(poem.lines.first(), poem.language))
        }
    }

    private fun stopReadAloud() {
        _state.update { it.copy(isReadingAloud = false, currentReadLine = -1) }
        viewModelScope.launch {
            _effects.emit(PoemDetailEffect.StopSpeaking)
        }
    }

    private fun advanceReadLine(lineIndex: Int) {
        val poem = _state.value.poem ?: return
        val nextLine = lineIndex + 1
        if (nextLine < poem.lines.size) {
            _state.update { it.copy(currentReadLine = nextLine) }
            viewModelScope.launch {
                _effects.emit(PoemDetailEffect.SpeakLine(poem.lines[nextLine], poem.language))
            }
        } else {
            _state.update { it.copy(isReadingAloud = false, currentReadLine = -1) }
        }
    }

    private fun finishReading(
        score: Float,
        durationSeconds: Int,
    ) {
        val poem = _state.value.poem ?: return
        _state.update { it.copy(readingScore = score, isReadingAloud = false) }
        viewModelScope.launch {
            val session = ReadingSession(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                poemId = poem.id,
                score = score,
                accuracyPct = score * 100f,
                durationSeconds = durationSeconds,
                language = poem.language,
                createdAt = Clock.System.now(),
            )
            saveReadingSessionUseCase(session)
        }
    }
}
