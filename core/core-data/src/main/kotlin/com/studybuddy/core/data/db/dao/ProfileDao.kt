package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studybuddy.core.data.db.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getProfile(id: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles LIMIT 1")
    fun getActiveProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity)

    @Update
    suspend fun update(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun delete(id: String)
}
