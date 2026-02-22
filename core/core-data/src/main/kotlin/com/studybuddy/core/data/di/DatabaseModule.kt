package com.studybuddy.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.studybuddy.core.data.db.StudyBuddyDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): StudyBuddyDatabase =
        Room.databaseBuilder(
            context,
            StudyBuddyDatabase::class.java,
            "studybuddy.db",
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideProfileDao(db: StudyBuddyDatabase) =
        db.profileDao()

    @Provides
    fun provideDicteeDao(db: StudyBuddyDatabase) =
        db.dicteeDao()

    @Provides
    fun provideMathDao(db: StudyBuddyDatabase) =
        db.mathDao()

    @Provides
    fun providePointsDao(db: StudyBuddyDatabase) =
        db.pointsDao()

    @Provides
    fun provideAvatarDao(db: StudyBuddyDatabase) =
        db.avatarDao()

    @Provides
    fun provideRewardsDao(db: StudyBuddyDatabase) =
        db.rewardsDao()

    @Provides
    fun provideVoicePackDao(db: StudyBuddyDatabase) =
        db.voicePackDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
