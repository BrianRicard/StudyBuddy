package com.studybuddy.feature.poems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.usecase.poem.GetFavouritePoemsUseCase
import com.studybuddy.core.domain.usecase.poem.GetPoemsUseCase
import com.studybuddy.core.domain.usecase.poem.GetUserPoemsUseCase
import com.studybuddy.core.domain.usecase.poem.RefreshPoemsUseCase
import com.studybuddy.core.domain.usecase.poem.ToggleFavouriteUseCase
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

data class PoemsState(
    val poems: List<Poem> = emptyList(),
    val favourites: List<Poem> = emptyList(),
    val userPoems: List<Poem> = emptyList(),
    val isLoading: Boolean = true,
    val selectedLanguage: String = "en",
    val selectedTab: PoemsTab = PoemsTab.BROWSE,
) {
    val displayPoems: List<Poem>
        get() = when (selectedTab) {
            PoemsTab.BROWSE -> poems
            PoemsTab.FAVOURITES -> favourites
            PoemsTab.MY_POEMS -> userPoems
        }
}

enum class PoemsTab {
    BROWSE,
    FAVOURITES,
    MY_POEMS,
}

sealed interface PoemsIntent {
    data object LoadPoems : PoemsIntent
    data class SelectLanguage(val language: String) : PoemsIntent
    data class SelectTab(val tab: PoemsTab) : PoemsIntent
    data class OpenPoem(val poemId: String) : PoemsIntent
    data class ToggleFavourite(val poem: Poem) : PoemsIntent
}

sealed interface PoemsEffect {
    data class NavigateToDetail(val poemId: String) : PoemsEffect
}

@HiltViewModel
class PoemsViewModel @Inject constructor(
    private val getPoemsUseCase: GetPoemsUseCase,
    private val getFavouritePoemsUseCase: GetFavouritePoemsUseCase,
    private val getUserPoemsUseCase: GetUserPoemsUseCase,
    private val refreshPoemsUseCase: RefreshPoemsUseCase,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PoemsState())
    val state: StateFlow<PoemsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PoemsEffect>()
    val effects: SharedFlow<PoemsEffect> = _effects.asSharedFlow()

    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    init {
        onIntent(PoemsIntent.LoadPoems)
    }

    fun onIntent(intent: PoemsIntent) {
        when (intent) {
            is PoemsIntent.LoadPoems -> loadPoems()
            is PoemsIntent.SelectLanguage -> selectLanguage(intent.language)
            is PoemsIntent.SelectTab -> _state.update { it.copy(selectedTab = intent.tab) }
            is PoemsIntent.OpenPoem -> {
                viewModelScope.launch {
                    _effects.emit(PoemsEffect.NavigateToDetail(intent.poemId))
                }
            }
            is PoemsIntent.ToggleFavourite -> toggleFavourite(intent.poem)
        }
    }

    private fun loadPoems() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                refreshPoemsUseCase(state.value.selectedLanguage)
            } catch (_: Exception) {
                // Offline — use cached poems
            }

            getPoemsUseCase(state.value.selectedLanguage).collect { poems ->
                _state.update { it.copy(poems = poems, isLoading = false) }
            }
        }

        viewModelScope.launch {
            getFavouritePoemsUseCase(profileId).collect { favs ->
                _state.update { it.copy(favourites = favs) }
            }
        }

        viewModelScope.launch {
            getUserPoemsUseCase(profileId).collect { userPoems ->
                _state.update { it.copy(userPoems = userPoems) }
            }
        }
    }

    private fun selectLanguage(language: String) {
        _state.update { it.copy(selectedLanguage = language, isLoading = true) }
        viewModelScope.launch {
            try {
                refreshPoemsUseCase(language)
            } catch (_: Exception) {
                // Offline
            }

            getPoemsUseCase(language).collect { poems ->
                _state.update { it.copy(poems = poems, isLoading = false) }
            }
        }
    }

    private fun toggleFavourite(poem: Poem) {
        viewModelScope.launch {
            toggleFavouriteUseCase(poem.id, poem.source.name, profileId)
        }
    }
}
