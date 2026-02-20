package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.model.DicteeWord
import kotlinx.coroutines.flow.Flow

interface DicteeRepository {
    fun getListsForProfile(profileId: String): Flow<List<DicteeList>>
    fun getList(listId: String): Flow<DicteeList?>
    fun getWordsForList(listId: String): Flow<List<DicteeWord>>
    suspend fun createList(list: DicteeList)
    suspend fun updateList(list: DicteeList)
    suspend fun deleteList(listId: String)
    suspend fun addWord(word: DicteeWord)
    suspend fun updateWord(word: DicteeWord)
    suspend fun deleteWord(wordId: String)
    suspend fun sync()
}
