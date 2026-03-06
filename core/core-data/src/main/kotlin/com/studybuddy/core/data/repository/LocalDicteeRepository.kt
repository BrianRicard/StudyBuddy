package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.DicteeDao
import com.studybuddy.core.data.mapper.toDomain
import com.studybuddy.core.data.mapper.toEntity
import com.studybuddy.core.data.network.BundledDicteeListLoader
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.repository.DicteeRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

@Singleton
class LocalDicteeRepository @Inject constructor(
    private val dao: DicteeDao,
    private val bundledDicteeListLoader: BundledDicteeListLoader,
) : DicteeRepository {

    override fun getListsForProfile(profileId: String): Flow<List<DicteeList>> =
        dao.getListsForProfile(profileId).flatMapLatest { lists ->
            if (lists.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    lists.map { entity ->
                        combine(
                            dao.getWordCount(entity.id),
                            dao.getMasteredCount(entity.id),
                        ) { wc, mc -> entity.toDomain(wordCount = wc, masteredCount = mc) }
                    },
                ) { it.toList() }
            }
        }

    override fun getList(listId: String): Flow<DicteeList?> = dao.getList(listId).flatMapLatest { entity ->
        if (entity == null) {
            flowOf(null)
        } else {
            combine(
                dao.getWordCount(entity.id),
                dao.getMasteredCount(entity.id),
            ) { wc, mc -> entity.toDomain(wordCount = wc, masteredCount = mc) }
        }
    }

    override fun getWordsForList(listId: String): Flow<List<DicteeWord>> =
        dao.getWordsForList(listId).map { words -> words.map { it.toDomain() } }

    override fun getWordsForLists(listIds: List<String>): Flow<List<DicteeWord>> =
        dao.getWordsForLists(listIds).map { words -> words.map { it.toDomain() } }

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

    override suspend fun seedDefaultLists(profileId: String) {
        val now = Clock.System.now()
        bundledDicteeListLoader.loadFrenchLists().forEach { bundled ->
            val list = DicteeList(
                id = bundled.id,
                profileId = profileId,
                title = "Unit ${bundled.unit}: ${bundled.title}",
                language = bundled.language,
                createdAt = now,
                updatedAt = now,
            )
            dao.insertList(list.toEntity())
            bundled.words.forEach { word ->
                val dicteeWord = DicteeWord(
                    id = UUID.randomUUID().toString(),
                    listId = bundled.id,
                    word = word,
                )
                dao.insertWord(dicteeWord.toEntity())
            }
        }
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
