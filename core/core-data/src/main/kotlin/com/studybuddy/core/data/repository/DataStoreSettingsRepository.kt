package com.studybuddy.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(@ApplicationContext private val context: Context) :
    SettingsRepository {

    private object Keys {
        val APP_LOCALE = stringPreferencesKey("app_locale")
        val ACCENT_STRICT = booleanPreferencesKey("accent_strict")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val PARENT_PIN_HASH = intPreferencesKey("parent_pin_hash")
        val DICTEE_SEEDED = booleanPreferencesKey("dictee_seeded")
        val WHISPER_MODEL = stringPreferencesKey("whisper_model")
    }

    override fun getAppLocale(): Flow<String> = context.dataStore.data.map { it[Keys.APP_LOCALE] ?: "en" }

    override suspend fun setAppLocale(locale: String) {
        context.dataStore.edit { it[Keys.APP_LOCALE] = locale }
    }

    override fun isAccentStrict(): Flow<Boolean> = context.dataStore.data.map { it[Keys.ACCENT_STRICT] ?: false }

    override suspend fun setAccentStrict(strict: Boolean) {
        context.dataStore.edit { it[Keys.ACCENT_STRICT] = strict }
    }

    override fun isSoundEnabled(): Flow<Boolean> = context.dataStore.data.map { it[Keys.SOUND_ENABLED] ?: true }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    override fun getDailyGoal(): Flow<Int> =
        context.dataStore.data.map { it[Keys.DAILY_GOAL] ?: AppConstants.DEFAULT_DAILY_GOAL }

    override suspend fun setDailyGoal(goal: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL] = goal }
    }

    override fun getSelectedTheme(): Flow<String> = context.dataStore.data.map { it[Keys.SELECTED_THEME] ?: "sunset" }

    override suspend fun setSelectedTheme(themeId: String) {
        context.dataStore.edit { it[Keys.SELECTED_THEME] = themeId }
    }

    override fun isOnboardingComplete(): Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }

    override suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    override fun getParentPinHash(): Flow<Int?> = context.dataStore.data.map { it[Keys.PARENT_PIN_HASH] }

    override suspend fun setParentPinHash(hash: Int?) {
        context.dataStore.edit {
            if (hash != null) {
                it[Keys.PARENT_PIN_HASH] = hash
            } else {
                it.remove(Keys.PARENT_PIN_HASH)
            }
        }
    }

    override fun isDicteeSeeded(): Flow<Boolean> = context.dataStore.data.map { it[Keys.DICTEE_SEEDED] ?: false }

    override suspend fun setDicteeSeeded(seeded: Boolean) {
        context.dataStore.edit { it[Keys.DICTEE_SEEDED] = seeded }
    }

    override fun getWhisperModel(): Flow<String> = context.dataStore.data.map { it[Keys.WHISPER_MODEL] ?: "" }

    override suspend fun setWhisperModel(modelFileName: String) {
        context.dataStore.edit { it[Keys.WHISPER_MODEL] = modelFileName }
    }

    override suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
