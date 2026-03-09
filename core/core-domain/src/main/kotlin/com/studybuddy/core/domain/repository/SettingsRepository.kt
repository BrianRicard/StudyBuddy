package com.studybuddy.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAppLocale(): Flow<String>
    suspend fun setAppLocale(locale: String)
    fun isAccentStrict(): Flow<Boolean>
    suspend fun setAccentStrict(strict: Boolean)
    fun isSoundEnabled(): Flow<Boolean>
    suspend fun setSoundEnabled(enabled: Boolean)
    fun getDailyGoal(): Flow<Int>
    suspend fun setDailyGoal(goal: Int)
    fun getSelectedTheme(): Flow<String>
    suspend fun setSelectedTheme(themeId: String)
    fun isOnboardingComplete(): Flow<Boolean>
    suspend fun setOnboardingComplete(complete: Boolean)
    fun getParentPinHash(): Flow<Int?>
    suspend fun setParentPinHash(hash: Int?)
    fun isDicteeSeeded(): Flow<Boolean>
    suspend fun setDicteeSeeded(seeded: Boolean)
    fun getWhisperModel(): Flow<String>
    suspend fun setWhisperModel(modelFileName: String)
    suspend fun clearAll()
    suspend fun sync()
}
