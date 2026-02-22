package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.DicteeDao
import com.studybuddy.core.data.mapper.toDomain
import com.studybuddy.core.data.mapper.toEntity
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.repository.DicteeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LocalDicteeRepository @Inject constructor(private val dao: DicteeDao) : DicteeRepository {

    override fun getListsForProfile(profileId: String): Flow<List<DicteeList>> =
        dao.getListsForProfile(profileId).map { lists -> lists.map { it.toDomain() } }

    override fun getList(listId: String): Flow<DicteeList?> =
        dao.getList(listId).map { it?.toDomain() }

    override fun getWordsForList(listId: String): Flow<List<DicteeWord>> =
        dao.getWordsForList(listId).map { words -> words.map { it.toDomain() } }

    override suspend fun createList(list: DicteeList) {
        dao.insertList(list.toEntity())
    }

    override suspend fun updateList(list: DicteeList) {
        dao.updateList(list.toEntity())
    }

    override suspend fun deleteList(listId: String) {
        dao.deleteList(listId)
    }

    override suspend fun addWord(word: DicteeWord) {
        dao.insertWord(word.toEntity())
    }

    override suspend fun updateWord(word: DicteeWord) {
        dao.updateWord(word.toEntity())
    }

    override suspend fun deleteWord(wordId: String) {
        dao.deleteWord(wordId)
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
