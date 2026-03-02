package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.ReadingSession
import kotlinx.coroutines.flow.Flow

interface PoemRepository {
    fun getPoemsByLanguage(language: String): Flow<List<Poem>>
    fun getUserPoems(profileId: String): Flow<List<Poem>>
    fun getFavourites(profileId: String): Flow<List<Poem>>
    fun isFavourite(
        poemId: String,
        profileId: String,
    ): Flow<Boolean>
    fun getSessionsForPoem(
        poemId: String,
        profileId: String,
    ): Flow<List<ReadingSession>>
    suspend fun getPoemById(id: String): Poem?
    suspend fun refreshPoems(language: String)
    suspend fun toggleFavourite(
        poemId: String,
        poemSource: String,
        profileId: String,
    )
    suspend fun createUserPoem(
        poem: Poem,
        profileId: String,
    )
    suspend fun deleteUserPoem(id: String)
    suspend fun saveReadingSession(session: ReadingSession)
    suspend fun getBestSession(
        poemId: String,
        profileId: String,
    ): ReadingSession?
    suspend fun sync()
}
