package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.studybuddy.core.data.db.entity.ReadingPassageEntity
import com.studybuddy.core.data.db.entity.ReadingQuestionEntity
import com.studybuddy.core.data.db.entity.ReadingResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {

    @Query("SELECT * FROM reading_passages WHERE language = :language ORDER BY tier, title")
    fun getPassagesByLanguage(language: String): Flow<List<ReadingPassageEntity>>

    @Query("SELECT * FROM reading_passages WHERE id = :id")
    suspend fun getPassageById(id: String): ReadingPassageEntity?

    @Query("SELECT * FROM reading_questions WHERE passageId = :passageId ORDER BY questionIndex")
    suspend fun getQuestionsForPassage(passageId: String): List<ReadingQuestionEntity>

    @Query("SELECT * FROM reading_questions WHERE passageId = :passageId ORDER BY questionIndex")
    fun observeQuestionsForPassage(passageId: String): Flow<List<ReadingQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassages(passages: List<ReadingPassageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<ReadingQuestionEntity>)

    @Insert
    suspend fun insertResult(result: ReadingResultEntity): Long

    @Query("SELECT * FROM reading_results WHERE passageId = :passageId ORDER BY completedAt DESC")
    fun getResultsForPassage(passageId: String): Flow<List<ReadingResultEntity>>

    @Query(
        "SELECT * FROM reading_results WHERE passageId = :passageId ORDER BY score DESC LIMIT 1",
    )
    suspend fun getBestResultForPassage(passageId: String): ReadingResultEntity?

    @Query("SELECT * FROM reading_results ORDER BY completedAt DESC")
    fun getAllResults(): Flow<List<ReadingResultEntity>>

    @Query("SELECT COUNT(*) FROM reading_passages WHERE language = :language")
    suspend fun getPassageCount(language: String): Int

    @Query(
        """
        SELECT DISTINCT passageId FROM reading_results rr
        INNER JOIN reading_passages rp ON rr.passageId = rp.id
        WHERE rp.tier = :tier AND rp.language = :language
        AND CAST(rr.score AS REAL) / rr.totalQuestions >= :minAccuracy
        """,
    )
    suspend fun getPassedPassageIds(
        tier: Int,
        language: String,
        minAccuracy: Float,
    ): List<String>

    @Transaction
    suspend fun insertPassagesWithQuestions(
        passages: List<ReadingPassageEntity>,
        questions: List<ReadingQuestionEntity>,
    ) {
        insertPassages(passages)
        insertQuestions(questions)
    }
}
