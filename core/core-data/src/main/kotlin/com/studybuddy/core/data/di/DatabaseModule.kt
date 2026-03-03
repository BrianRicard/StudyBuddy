package com.studybuddy.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.WorkManager
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.data.db.Migrations
import com.studybuddy.core.data.db.StudyBuddyDatabase
import com.studybuddy.core.domain.model.RewardCatalog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudyBuddyDatabase = Room.databaseBuilder(
        context,
        StudyBuddyDatabase::class.java,
        "studybuddy.db",
    )
        .addCallback(
            object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val now = System.currentTimeMillis()
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO profiles (id, name, locale, totalPoints,
                            bodyId, hatId, faceId, outfitId, petId, equippedTitle,
                            createdAt, updatedAt)
                        VALUES ('${AppConstants.DEFAULT_PROFILE_ID}', 'StudyBuddy',
                            'en', 0, 'fox', 'none', 'none', 'default', 'none',
                            NULL, $now, $now)
                        """.trimIndent(),
                    )
                    // Seed default avatar config so Avatar Closet can load immediately
                    val avatarId = java.util.UUID.randomUUID().toString()
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO avatar_configs (id, profileId, bodyId,
                            hatId, faceId, outfitId, petId, equippedTitle, updatedAt)
                        VALUES ('$avatarId', '${AppConstants.DEFAULT_PROFILE_ID}',
                            'fox', 'hat_none', 'face_none', 'outfit_none', 'pet_none',
                            NULL, $now)
                        """.trimIndent(),
                    )
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Backfill starter items for the default profile on every open.
                    // The unique index on (profileId, rewardId) prevents duplicates.
                    val profileId = AppConstants.DEFAULT_PROFILE_ID
                    val now = System.currentTimeMillis()
                    RewardCatalog.starterItemIds.forEach { rewardId ->
                        val item = RewardCatalog.getItemById(rewardId) ?: return@forEach
                        db.execSQL(
                            """
                            INSERT OR IGNORE INTO owned_rewards
                                (id, profileId, rewardId, category, purchasedAt)
                            VALUES (
                                '${java.util.UUID.randomUUID()}',
                                '$profileId', '$rewardId',
                                '${item.category.name}', $now
                            )
                            """.trimIndent(),
                        )
                    }
                }
            },
        )
        .addMigrations(Migrations.MIGRATION_1_2)
        .build()

    @Provides
    fun provideProfileDao(db: StudyBuddyDatabase) = db.profileDao()

    @Provides
    fun provideDicteeDao(db: StudyBuddyDatabase) = db.dicteeDao()

    @Provides
    fun provideMathDao(db: StudyBuddyDatabase) = db.mathDao()

    @Provides
    fun providePointsDao(db: StudyBuddyDatabase) = db.pointsDao()

    @Provides
    fun provideAvatarDao(db: StudyBuddyDatabase) = db.avatarDao()

    @Provides
    fun provideRewardsDao(db: StudyBuddyDatabase) = db.rewardsDao()

    @Provides
    fun provideVoicePackDao(db: StudyBuddyDatabase) = db.voicePackDao()

    @Provides
    fun providePoemDao(db: StudyBuddyDatabase) = db.poemDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
