package com.studybuddy.feature.reading.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.domain.model.ReadingPassage
import com.studybuddy.core.domain.repository.ReadingRepository
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

data class ReadingHomeState(
    val passages: List<ReadingPassage> = emptyList(),
    val selectedLanguage: String = "EN",
    val isLoading: Boolean = true,
    val unlockedTiers: Set<Int> = setOf(1),
)

sealed interface ReadingHomeIntent {
    data class SelectLanguage(val language: String) : ReadingHomeIntent
    data class OpenPassage(val passageId: String) : ReadingHomeIntent
}

sealed interface ReadingHomeEffect {
    data class NavigateToPassage(val passageId: String) : ReadingHomeEffect
}

@HiltViewModel
class ReadingHomeViewModel @Inject constructor(
    private val readingRepository: ReadingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReadingHomeState())
    val state: StateFlow<ReadingHomeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ReadingHomeEffect>()
    val effects: SharedFlow<ReadingHomeEffect> = _effects.asSharedFlow()

    init {
        loadContent()
    }

    fun onIntent(intent: ReadingHomeIntent) {
        when (intent) {
            is ReadingHomeIntent.SelectLanguage -> selectLanguage(intent.language)
            is ReadingHomeIntent.OpenPassage -> {
                viewModelScope.launch {
                    _effects.emit(ReadingHomeEffect.NavigateToPassage(intent.passageId))
                }
            }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val language = _state.value.selectedLanguage
            readingRepository.loadContentIfNeeded(language)
            val unlockedTiers = calculateUnlockedTiers(language)
            _state.update { it.copy(unlockedTiers = unlockedTiers) }

            readingRepository.getPassagesByLanguage(language).collect { passages ->
                val withLocks = passages.map { passage ->
                    passage.copy(isLocked = passage.tier !in _state.value.unlockedTiers)
                }
                _state.update {
                    it.copy(passages = withLocks, isLoading = false)
                }
            }
        }
    }

    private fun selectLanguage(language: String) {
        _state.update { it.copy(selectedLanguage = language, isLoading = true) }
        viewModelScope.launch {
            readingRepository.loadContentIfNeeded(language)
            val unlockedTiers = calculateUnlockedTiers(language)
            _state.update { it.copy(unlockedTiers = unlockedTiers) }

            readingRepository.getPassagesByLanguage(language).collect { passages ->
                val withLocks = passages.map { passage ->
                    passage.copy(isLocked = passage.tier !in _state.value.unlockedTiers)
                }
                _state.update {
                    it.copy(passages = withLocks, isLoading = false)
                }
            }
        }
    }

    private suspend fun calculateUnlockedTiers(language: String): Set<Int> {
        val unlocked = mutableSetOf(1)
        for (tier in 2..4) {
            if (readingRepository.isNextTierUnlocked(tier, language)) {
                unlocked.add(tier)
            } else {
                break
            }
        }
        return unlocked
    }
}
