package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studybuddy.core.data.db.entity.AvatarConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AvatarDao {
    @Query("SELECT * FROM avatar_configs WHERE profileId = :profileId")
    fun getAvatarConfig(profileId: String): Flow<AvatarConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: AvatarConfigEntity)
}
