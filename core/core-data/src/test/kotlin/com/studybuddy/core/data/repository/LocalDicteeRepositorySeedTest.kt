package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.DicteeDao
import com.studybuddy.core.data.db.entity.DicteeListEntity
import com.studybuddy.core.data.network.BundledDicteeList
import com.studybuddy.core.data.network.BundledDicteeListLoader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Regression tests for the restore-then-seed data-loss bug: seeding bundled
 * lists must never touch list ids that already exist. The DAO inserts use
 * ON CONFLICT REPLACE with stable bundled ids, and the words' CASCADE meant a
 * re-seed over restored data silently wiped every restored word.
 */
class LocalDicteeRepositorySeedTest {

    private val dao: DicteeDao = mockk(relaxed = true)
    private val loader: BundledDicteeListLoader = mockk()
    private val repository = LocalDicteeRepository(dao, loader)

    private fun bundled(
        id: String,
        vararg words: String,
    ) = BundledDicteeList(id = id, unit = 1, title = "Test", language = "fr", words = words.toList())

    private fun existingList(id: String) = DicteeListEntity(
        id = id,
        profileId = "default",
        title = "Restored",
        language = "fr",
        createdAt = 0,
        updatedAt = 0,
    )

    @Test
    fun `seeding skips bundled lists that already exist`() = runTest {
        every { loader.loadFrenchLists() } returns listOf(
            bundled("fr_dictee_01", "chat", "chien"),
            bundled("fr_dictee_02", "maison"),
        )
        // fr_dictee_01 was restored from a backup and must survive untouched.
        coEvery { dao.getAllLists() } returns listOf(existingList("fr_dictee_01"))

        val insertedLists = mutableListOf<DicteeListEntity>()
        val listSlot = slot<DicteeListEntity>()
        coEvery { dao.insertList(capture(listSlot)) } answers { insertedLists += listSlot.captured }

        repository.seedDefaultLists("default")

        assertEquals(listOf("fr_dictee_02"), insertedLists.map { it.id })
        // Only the new list's single word is inserted — nothing for fr_dictee_01.
        coVerify(exactly = 1) { dao.insertWord(any()) }
    }

    @Test
    fun `seeding an empty database inserts everything`() = runTest {
        every { loader.loadFrenchLists() } returns listOf(
            bundled("fr_dictee_01", "chat", "chien"),
            bundled("fr_dictee_02", "maison"),
        )
        coEvery { dao.getAllLists() } returns emptyList()

        repository.seedDefaultLists("default")

        coVerify(exactly = 2) { dao.insertList(any()) }
        coVerify(exactly = 3) { dao.insertWord(any()) }
    }

    @Test
    fun `re-running the seed is a no-op once everything exists`() = runTest {
        every { loader.loadFrenchLists() } returns listOf(bundled("fr_dictee_01", "chat"))
        coEvery { dao.getAllLists() } returns listOf(existingList("fr_dictee_01"))

        repository.seedDefaultLists("default")

        coVerify(exactly = 0) { dao.insertList(any()) }
        coVerify(exactly = 0) { dao.insertWord(any()) }
        assertTrue(true)
    }
}
