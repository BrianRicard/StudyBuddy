package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studybuddy.core.data.db.entity.DicteeListEntity
import com.studybuddy.core.data.db.entity.DicteeWordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DicteeDao {
    @Query("SELECT * FROM dictee_lists WHERE profileId = :profileId ORDER BY updatedAt DESC")
    fun getListsForProfile(profileId: String): Flow<List<DicteeListEntity>>

    @Query("SELECT * FROM dictee_lists WHERE id = :listId")
    fun getList(listId: String): Flow<DicteeListEntity?>

    @Query("SELECT * FROM dictee_words WHERE listId = :listId ORDER BY word ASC")
    fun getWordsForList(listId: String): Flow<List<DicteeWordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: DicteeListEntity)

    @Update
    suspend fun updateList(list: DicteeListEntity)

    @Query("DELETE FROM dictee_lists WHERE id = :listId")
    suspend fun deleteList(listId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: DicteeWordEntity)

    @Update
    suspend fun updateWord(word: DicteeWordEntity)

    @Query("DELETE FROM dictee_words WHERE id = :wordId")
    suspend fun deleteWord(wordId: String)

    @Query("SELECT COUNT(*) FROM dictee_words WHERE listId = :listId")
    fun getWordCount(listId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM dictee_words WHERE listId = :listId AND mastered = 1")
    fun getMasteredCount(listId: String): Flow<Int>

    @Query("SELECT * FROM dictee_lists")
    suspend fun getAllLists(): List<DicteeListEntity>

    @Query("SELECT * FROM dictee_words")
    suspend fun getAllWords(): List<DicteeWordEntity>
}
