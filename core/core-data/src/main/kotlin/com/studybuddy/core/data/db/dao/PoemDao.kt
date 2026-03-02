package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studybuddy.core.data.db.entity.CachedPoemEntity
import com.studybuddy.core.data.db.entity.FavouritePoemEntity
import com.studybuddy.core.data.db.entity.ReadingSessionEntity
import com.studybuddy.core.data.db.entity.UserPoemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoemDao {

    @Query("SELECT * FROM cached_poems WHERE language = :language ORDER BY title ASC")
    fun getCachedPoemsByLanguage(language: String): Flow<List<CachedPoemEntity>>

    @Query("SELECT * FROM cached_poems WHERE id = :id")
    suspend fun getCachedPoemById(id: String): CachedPoemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedPoems(poems: List<CachedPoemEntity>)

    @Query("DELETE FROM cached_poems WHERE source = 'API' AND cachedAt < :expiryMillis")
    suspend fun deleteExpiredCache(expiryMillis: Long)

    @Query(
        "SELECT COUNT(*) FROM cached_poems " +
            "WHERE source = 'API' AND language = :language AND cachedAt > :sinceMillis",
    )
    suspend fun getCachedApiPoemCount(
        language: String,
        sinceMillis: Long,
    ): Int

    @Query("SELECT COUNT(*) FROM cached_poems WHERE source = 'BUNDLED' AND language = :language")
    suspend fun getCachedBundledPoemCount(language: String): Int

    @Query("SELECT * FROM user_poems WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getUserPoems(profileId: String): Flow<List<UserPoemEntity>>

    @Query("SELECT * FROM user_poems WHERE id = :id")
    suspend fun getUserPoemById(id: String): UserPoemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPoem(poem: UserPoemEntity)

    @Query("DELETE FROM user_poems WHERE id = :id")
    suspend fun deleteUserPoem(id: String)

    @Query("SELECT * FROM favourite_poems WHERE profileId = :profileId")
    fun getFavourites(profileId: String): Flow<List<FavouritePoemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(favourite: FavouritePoemEntity)

    @Query("DELETE FROM favourite_poems WHERE poemId = :poemId AND profileId = :profileId")
    suspend fun removeFavourite(
        poemId: String,
        profileId: String,
    )

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_poems WHERE poemId = :poemId AND profileId = :profileId)")
    fun isFavourite(
        poemId: String,
        profileId: String,
    ): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingSession(session: ReadingSessionEntity)

    @Query("SELECT * FROM reading_sessions WHERE poemId = :poemId AND profileId = :profileId ORDER BY createdAt DESC")
    fun getSessionsForPoem(
        poemId: String,
        profileId: String,
    ): Flow<List<ReadingSessionEntity>>

    @Query(
        "SELECT * FROM reading_sessions WHERE poemId = :poemId AND profileId = :profileId ORDER BY score DESC LIMIT 1",
    )
    suspend fun getBestSession(
        poemId: String,
        profileId: String,
    ): ReadingSessionEntity?
}
