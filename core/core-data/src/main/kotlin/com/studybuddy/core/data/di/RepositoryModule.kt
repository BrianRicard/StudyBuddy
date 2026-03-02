package com.studybuddy.core.data.di

import com.studybuddy.core.data.repository.DataStoreSettingsRepository
import com.studybuddy.core.data.repository.LocalAvatarRepository
import com.studybuddy.core.data.repository.LocalBackupRepository
import com.studybuddy.core.data.repository.LocalDicteeRepository
import com.studybuddy.core.data.repository.LocalMathRepository
import com.studybuddy.core.data.repository.LocalPoemRepository
import com.studybuddy.core.data.repository.LocalPointsRepository
import com.studybuddy.core.data.repository.LocalProfileRepository
import com.studybuddy.core.data.repository.LocalRewardsRepository
import com.studybuddy.core.data.repository.LocalVoicePackRepository
import com.studybuddy.core.domain.repository.AvatarRepository
import com.studybuddy.core.domain.repository.BackupRepository
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.repository.MathRepository
import com.studybuddy.core.domain.repository.PoemRepository
import com.studybuddy.core.domain.repository.PointsRepository
import com.studybuddy.core.domain.repository.ProfileRepository
import com.studybuddy.core.domain.repository.RewardsRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.repository.VoicePackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindProfileRepo(impl: LocalProfileRepository): ProfileRepository

    @Binds
    abstract fun bindDicteeRepo(impl: LocalDicteeRepository): DicteeRepository

    @Binds
    abstract fun bindMathRepo(impl: LocalMathRepository): MathRepository

    @Binds
    abstract fun bindPointsRepo(impl: LocalPointsRepository): PointsRepository

    @Binds
    abstract fun bindAvatarRepo(impl: LocalAvatarRepository): AvatarRepository

    @Binds
    abstract fun bindRewardsRepo(impl: LocalRewardsRepository): RewardsRepository

    @Binds
    abstract fun bindBackupRepo(impl: LocalBackupRepository): BackupRepository

    @Binds
    abstract fun bindSettingsRepo(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindVoicePackRepo(impl: LocalVoicePackRepository): VoicePackRepository

    @Binds
    abstract fun bindPoemRepo(impl: LocalPoemRepository): PoemRepository

    // CLOUD MIGRATION: When ready, create CloudXxxRepository implementations and
    // swap the @Binds here. No other code changes needed.
    // Example:
    // @Binds abstract fun bindDicteeRepo(impl: CloudDicteeRepository): DicteeRepository
}
