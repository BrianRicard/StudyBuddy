package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studybuddy.core.data.db.entity.VoicePackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoicePackDao {
    @Query("SELECT * FROM voice_packs")
    fun getAll(): Flow<List<VoicePackEntity>>

    @Query("SELECT * FROM voice_packs WHERE id = :id")
    fun getById(id: String): Flow<VoicePackEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voicePack: VoicePackEntity)

    @Query("UPDATE voice_packs SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}
