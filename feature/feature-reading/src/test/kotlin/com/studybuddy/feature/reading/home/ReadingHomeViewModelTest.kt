package com.studybuddy.feature.reading.home

import app.cash.turbine.test
import com.studybuddy.core.domain.model.ReadingPassage
import com.studybuddy.core.domain.model.ReadingTheme
import com.studybuddy.core.domain.repository.ReadingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingHomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val readingRepository: ReadingRepository = mockk()

    private val testPassages = listOf(
        ReadingPassage(
            id = "p1",
            language = "EN",
            tier = 1,
            theme = ReadingTheme.ANIMALS,
            title = "Test Passage 1",
            passage = "Test content",
            wordCount = 50,
            source = "test",
            sourceAttribution = null,
            questions = emptyList(),
        ),
        ReadingPassage(
            id = "p2",
            language = "EN",
            tier = 2,
            theme = ReadingTheme.ADVENTURE,
            title = "Test Passage 2",
            passage = "Test content 2",
            wordCount = 80,
            source = "test",
            sourceAttribution = null,
            questions = emptyList(),
        ),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { readingRepository.loadContentIfNeeded(any()) } returns Unit
        every { readingRepository.getPassagesByLanguage("EN") } returns flowOf(testPassages)
        coEvery { readingRepository.isNextTierUnlocked(2, "EN") } returns false
        coEvery { readingRepository.isNextTierUnlocked(3, "EN") } returns false
        coEvery { readingRepository.isNextTierUnlocked(4, "EN") } returns false
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ReadingHomeViewModel(readingRepository)

    @Test
    fun `init loads content and sets passages`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.passages.size)
        coVerify { readingRepository.loadContentIfNeeded("EN") }
    }

    @Test
    fun `tier 1 passages are unlocked by default`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val tier1 = vm.state.value.passages.first { it.tier == 1 }
        assertFalse(tier1.isLocked)
    }

    @Test
    fun `tier 2 passages are locked when tier not unlocked`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val tier2 = vm.state.value.passages.first { it.tier == 2 }
        assertTrue(tier2.isLocked)
    }

    @Test
    fun `tier 2 passages are unlocked when criteria met`() = runTest {
        coEvery { readingRepository.isNextTierUnlocked(2, "EN") } returns true

        val vm = createViewModel()
        advanceUntilIdle()

        val tier2 = vm.state.value.passages.first { it.tier == 2 }
        assertFalse(tier2.isLocked)
    }

    @Test
    fun `selecting language reloads content`() = runTest {
        val frPassages = listOf(
            testPassages[0].copy(id = "fr1", language = "FR", title = "Passage FR"),
        )
        every { readingRepository.getPassagesByLanguage("FR") } returns flowOf(frPassages)
        coEvery { readingRepository.isNextTierUnlocked(any(), "FR") } returns false

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onIntent(ReadingHomeIntent.SelectLanguage("FR"))
        advanceUntilIdle()

        assertEquals("FR", vm.state.value.selectedLanguage)
        assertEquals(1, vm.state.value.passages.size)
        assertEquals("Passage FR", vm.state.value.passages[0].title)
    }

    @Test
    fun `open passage emits navigation effect`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.effects.test {
            vm.onIntent(ReadingHomeIntent.OpenPassage("p1"))
            val effect = awaitItem()
            assertTrue(effect is ReadingHomeEffect.NavigateToPassage)
            assertEquals("p1", (effect as ReadingHomeEffect.NavigateToPassage).passageId)
        }
    }
}
