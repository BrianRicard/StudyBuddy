package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.ReadingDictionaryEntry
import com.studybuddy.core.domain.model.ReadingPassage
import com.studybuddy.core.domain.model.ReadingResult
import kotlinx.coroutines.flow.Flow

interface ReadingRepository {

    fun getPassagesByLanguage(language: String): Flow<List<ReadingPassage>>

    suspend fun getPassageById(id: String): ReadingPassage?

    suspend fun saveResult(result: ReadingResult)

    suspend fun getBestResult(passageId: String): ReadingResult?

    fun getAllResults(): Flow<List<ReadingResult>>

    suspend fun isNextTierUnlocked(
        currentTier: Int,
        language: String,
    ): Boolean

    suspend fun loadContentIfNeeded(language: String)

    fun getDictionaryEntries(language: String): List<ReadingDictionaryEntry>

    suspend fun sync() // Cloud migration hook
}
