package com.studybuddy.shared.tts

sealed interface TtsState {
    data object Initializing : TtsState
    data object Ready : TtsState
    data class Speaking(val text: String) : TtsState
    data class Error(val message: String) : TtsState
}
