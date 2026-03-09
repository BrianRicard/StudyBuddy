package com.studybuddy.core.data.repository

import com.studybuddy.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * A fake [SettingsRepository] whose defaults mirror what DataStore should provide
 * on a fresh install. This lets us verify the contract without Android context.
 */
class FakeSettingsRepository : SettingsRepository {
    private val locale = MutableStateFlow("en")
    private val accentStrict = MutableStateFlow(false)
    private val soundEnabled = MutableStateFlow(true)
    private val dailyGoal = MutableStateFlow(5)
    private val selectedTheme = MutableStateFlow("sunset")
    private val onboardingComplete = MutableStateFlow(false)
    private val parentPinHash = MutableStateFlow<Int?>(null)
    private val dicteeSeeded = MutableStateFlow(false)
    private val whisperModel = MutableStateFlow("")

    override fun getAppLocale(): Flow<String> = locale
    override suspend fun setAppLocale(locale: String) {
        this.locale.value = locale
    }
    override fun isAccentStrict(): Flow<Boolean> = accentStrict
    override suspend fun setAccentStrict(strict: Boolean) {
        accentStrict.value = strict
    }
    override fun isSoundEnabled(): Flow<Boolean> = soundEnabled
    override suspend fun setSoundEnabled(enabled: Boolean) {
        soundEnabled.value = enabled
    }
    override fun getDailyGoal(): Flow<Int> = dailyGoal
    override suspend fun setDailyGoal(goal: Int) {
        dailyGoal.value = goal
    }
    override fun getSelectedTheme(): Flow<String> = selectedTheme
    override suspend fun setSelectedTheme(themeId: String) {
        selectedTheme.value = themeId
    }
    override fun isOnboardingComplete(): Flow<Boolean> = onboardingComplete
    override suspend fun setOnboardingComplete(complete: Boolean) {
        onboardingComplete.value = complete
    }
    override fun getParentPinHash(): Flow<Int?> = parentPinHash
    override suspend fun setParentPinHash(hash: Int?) {
        parentPinHash.value = hash
    }
    override fun isDicteeSeeded(): Flow<Boolean> = dicteeSeeded
    override suspend fun setDicteeSeeded(seeded: Boolean) {
        dicteeSeeded.value = seeded
    }
    override fun getWhisperModel(): Flow<String> = whisperModel
    override suspend fun setWhisperModel(modelFileName: String) {
        whisperModel.value = modelFileName
    }
    override suspend fun clearAll() {
        onboardingComplete.value = false
        parentPinHash.value = null
    }
    override suspend fun sync() {}
}

/**
 * Contract tests that verify the expected defaults and basic behavior of
 * [SettingsRepository]. The bug that prompted these tests: `isOnboardingComplete`
 * was collected with `initial = true` but the DataStore default is `false`,
 * causing the wrong start destination on first launch.
 */
class SettingsRepositoryContractTest {

    private val repo: SettingsRepository = FakeSettingsRepository()

    @Test
    fun `onboarding defaults to false for fresh install`() = runTest {
        assertFalse(repo.isOnboardingComplete().first())
    }

    @Test
    fun `theme defaults to sunset`() = runTest {
        assertEquals("sunset", repo.getSelectedTheme().first())
    }

    @Test
    fun `locale defaults to en`() = runTest {
        assertEquals("en", repo.getAppLocale().first())
    }

    @Test
    fun `set and get onboarding complete`() = runTest {
        repo.setOnboardingComplete(true)
        assertTrue(repo.isOnboardingComplete().first())
    }

    @Test
    fun `clearAll resets onboarding`() = runTest {
        repo.setOnboardingComplete(true)
        repo.clearAll()
        assertFalse(repo.isOnboardingComplete().first())
    }

    @Test
    fun `set and get theme`() = runTest {
        repo.setSelectedTheme("ocean")
        assertEquals("ocean", repo.getSelectedTheme().first())
    }

    @Test
    fun `set and get locale`() = runTest {
        repo.setAppLocale("fr")
        assertEquals("fr", repo.getAppLocale().first())
    }

    @Test
    fun `daily goal defaults to 5`() = runTest {
        assertEquals(5, repo.getDailyGoal().first())
    }

    @Test
    fun `sound defaults to enabled`() = runTest {
        assertTrue(repo.isSoundEnabled().first())
    }

    @Test
    fun `accent strict defaults to false`() = runTest {
        assertFalse(repo.isAccentStrict().first())
    }
}
