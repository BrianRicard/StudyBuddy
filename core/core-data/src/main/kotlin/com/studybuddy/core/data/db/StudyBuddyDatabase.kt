package com.studybuddy.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.studybuddy.core.data.db.dao.AvatarDao
import com.studybuddy.core.data.db.dao.DicteeDao
import com.studybuddy.core.data.db.dao.MathDao
import com.studybuddy.core.data.db.dao.PointsDao
import com.studybuddy.core.data.db.dao.ProfileDao
import com.studybuddy.core.data.db.dao.RewardsDao
import com.studybuddy.core.data.db.dao.VoicePackDao
import com.studybuddy.core.data.db.entity.AvatarConfigEntity
import com.studybuddy.core.data.db.entity.DicteeListEntity
import com.studybuddy.core.data.db.entity.DicteeWordEntity
import com.studybuddy.core.data.db.entity.MathSessionEntity
import com.studybuddy.core.data.db.entity.OwnedRewardEntity
import com.studybuddy.core.data.db.entity.PointEventEntity
import com.studybuddy.core.data.db.entity.ProfileEntity
import com.studybuddy.core.data.db.entity.VoicePackEntity

@Database(
    entities = [
        ProfileEntity::class,
        DicteeListEntity::class,
        DicteeWordEntity::class,
        MathSessionEntity::class,
        PointEventEntity::class,
        AvatarConfigEntity::class,
        OwnedRewardEntity::class,
        VoicePackEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class StudyBuddyDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun dicteeDao(): DicteeDao
    abstract fun mathDao(): MathDao
    abstract fun pointsDao(): PointsDao
    abstract fun avatarDao(): AvatarDao
    abstract fun rewardsDao(): RewardsDao
    abstract fun voicePackDao(): VoicePackDao
}
