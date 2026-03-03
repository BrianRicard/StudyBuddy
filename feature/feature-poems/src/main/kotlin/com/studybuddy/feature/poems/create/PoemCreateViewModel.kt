package com.studybuddy.feature.poems.create

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.PoemSource
import com.studybuddy.core.domain.usecase.poem.CreateUserPoemUseCase
import com.studybuddy.core.ui.R as CoreUiR
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

data class PoemCreateState(
    val title: String = "",
    val author: String = "",
    val body: String = "",
    val language: String = "en",
    val isSaving: Boolean = false,
) {
    val isValid: Boolean
        get() = title.isNotBlank() && body.isNotBlank()
}

sealed interface PoemCreateIntent {
    data class UpdateTitle(val title: String) : PoemCreateIntent
    data class UpdateAuthor(val author: String) : PoemCreateIntent
    data class UpdateBody(val body: String) : PoemCreateIntent
    data class UpdateLanguage(val language: String) : PoemCreateIntent
    data object Save : PoemCreateIntent
    data class ImportFile(val content: String, val mimeType: String) : PoemCreateIntent
}

sealed interface PoemCreateEffect {
    data object NavigateBack : PoemCreateEffect
    data class ShowSnackbar(@StringRes val messageResId: Int, val formatArg: Int = 0) :
        PoemCreateEffect
}

@HiltViewModel
class PoemCreateViewModel @Inject constructor(
    private val createUserPoemUseCase: CreateUserPoemUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PoemCreateState())
    val state: StateFlow<PoemCreateState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PoemCreateEffect>()
    val effects: SharedFlow<PoemCreateEffect> = _effects.asSharedFlow()

    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    fun onIntent(intent: PoemCreateIntent) {
        when (intent) {
            is PoemCreateIntent.UpdateTitle -> _state.update { it.copy(title = intent.title) }
            is PoemCreateIntent.UpdateAuthor -> _state.update { it.copy(author = intent.author) }
            is PoemCreateIntent.UpdateBody -> _state.update { it.copy(body = intent.body) }
            is PoemCreateIntent.UpdateLanguage ->
                _state.update { it.copy(language = intent.language) }
            is PoemCreateIntent.Save -> save()
            is PoemCreateIntent.ImportFile -> importFile(intent.content, intent.mimeType)
        }
    }

    private fun save() {
        val current = _state.value
        if (!current.isValid || current.isSaving) return

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val poem = Poem(
                id = UUID.randomUUID().toString(),
                title = current.title.trim(),
                author = current.author.trim().ifEmpty { "Unknown" },
                lines = current.body.lines().filter { it.isNotBlank() }.map { it.trim() },
                language = current.language,
                source = PoemSource.USER,
            )
            createUserPoemUseCase(poem, profileId)
            _effects.emit(PoemCreateEffect.ShowSnackbar(CoreUiR.string.poems_saved))
            _effects.emit(PoemCreateEffect.NavigateBack)
        }
    }

    private fun importFile(
        content: String,
        mimeType: String,
    ) {
        val language = _state.value.language
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val result = when {
                mimeType.contains("json") -> PoemFileParser.parseJsonFile(content, language)
                else -> PoemFileParser.parseTextFile(content, language)
            }

            result.fold(
                onSuccess = { poems ->
                    poems.forEach { createUserPoemUseCase(it, profileId) }
                    _effects.emit(
                        PoemCreateEffect.ShowSnackbar(
                            CoreUiR.string.poems_import_count,
                            poems.size,
                        ),
                    )
                    _effects.emit(PoemCreateEffect.NavigateBack)
                },
                onFailure = {
                    _state.update { it.copy(isSaving = false) }
                    _effects.emit(
                        PoemCreateEffect.ShowSnackbar(CoreUiR.string.poems_import_failed),
                    )
                },
            )
        }
    }
}
