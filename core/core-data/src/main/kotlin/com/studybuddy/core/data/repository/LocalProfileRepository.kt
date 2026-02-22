package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.ProfileDao
import com.studybuddy.core.data.mapper.toDomain
import com.studybuddy.core.data.mapper.toEntity
import com.studybuddy.core.domain.model.Profile
import com.studybuddy.core.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LocalProfileRepository @Inject constructor(private val dao: ProfileDao) : ProfileRepository {

    override fun getProfile(id: String): Flow<Profile?> = dao.getProfile(id).map { it?.toDomain() }

    override fun getActiveProfile(): Flow<Profile?> = dao.getActiveProfile().map { it?.toDomain() }

    override suspend fun createProfile(profile: Profile) {
        dao.insert(profile.toEntity())
    }

    override suspend fun updateProfile(profile: Profile) {
        dao.update(profile.toEntity())
    }

    override suspend fun deleteProfile(id: String) {
        dao.delete(id)
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
