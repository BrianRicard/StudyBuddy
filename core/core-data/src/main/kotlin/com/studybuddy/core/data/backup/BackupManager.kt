package com.studybuddy.core.data.backup

import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.data.db.StudyBuddyDatabase
import com.studybuddy.core.data.db.entity.AvatarConfigEntity
import com.studybuddy.core.data.db.entity.DicteeListEntity
import com.studybuddy.core.data.db.entity.DicteeWordEntity
import com.studybuddy.core.data.db.entity.MathSessionEntity
import com.studybuddy.core.data.db.entity.OwnedRewardEntity
import com.studybuddy.core.data.db.entity.PointEventEntity
import com.studybuddy.core.data.db.entity.ProfileEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BackupData(
    val version: Int = AppConstants.BACKUP_SCHEMA_VERSION,
    val profiles: List<ProfileBackup> = emptyList(),
    val dicteeLists: List<DicteeListBackup> = emptyList(),
    val dicteeWords: List<DicteeWordBackup> = emptyList(),
    val mathSessions: List<MathSessionBackup> = emptyList(),
    val pointEvents: List<PointEventBackup> = emptyList(),
    val avatarConfigs: List<AvatarConfigBackup> = emptyList(),
    val ownedRewards: List<OwnedRewardBackup> = emptyList(),
)

@Serializable
data class ProfileBackup(
    val id: String,
    val name: String,
    val locale: String,
    val totalPoints: Long,
    val bodyId: String,
    val hatId: String,
    val faceId: String,
    val outfitId: String,
    val petId: String,
    val equippedTitle: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class DicteeListBackup(
    val id: String,
    val profileId: String,
    val title: String,
    val language: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class DicteeWordBackup(
    val id: String,
    val listId: String,
    val word: String,
    val mastered: Boolean,
    val attempts: Int,
    val correctCount: Int,
    val lastAttemptAt: Long? = null,
)

@Serializable
data class MathSessionBackup(
    val id: String,
    val profileId: String,
    val operators: String,
    val rangeMin: Int,
    val rangeMax: Int,
    val totalProblems: Int,
    val correctCount: Int,
    val bestStreak: Int,
    val avgResponseMs: Long,
    val difficulty: String,
    val completedAt: Long,
)

@Serializable
data class PointEventBackup(
    val id: String,
    val profileId: String,
    val source: String,
    val points: Int,
    val reason: String,
    val timestamp: Long,
)

@Serializable
data class AvatarConfigBackup(
    val id: String,
    val profileId: String,
    val bodyId: String,
    val hatId: String,
    val faceId: String,
    val outfitId: String,
    val petId: String,
    val equippedTitle: String? = null,
    val updatedAt: Long,
)

@Serializable
data class OwnedRewardBackup(
    val id: String,
    val profileId: String,
    val rewardId: String,
    val category: String,
    val purchasedAt: Long,
)

@Singleton
class BackupManager @Inject constructor(private val database: StudyBuddyDatabase) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun createFullBackup(): String {
        val profiles = database.profileDao().getAllProfiles().map { entity ->
            ProfileBackup(
                id = entity.id,
                name = entity.name,
                locale = entity.locale,
                totalPoints = entity.totalPoints,
                bodyId = entity.bodyId,
                hatId = entity.hatId,
                faceId = entity.faceId,
                outfitId = entity.outfitId,
                petId = entity.petId,
                equippedTitle = entity.equippedTitle,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
        }

        val dicteeLists = database.dicteeDao().getAllLists().map { entity ->
            DicteeListBackup(
                id = entity.id,
                profileId = entity.profileId,
                title = entity.title,
                language = entity.language,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
        }

        val dicteeWords = database.dicteeDao().getAllWords().map { entity ->
            DicteeWordBackup(
                id = entity.id,
                listId = entity.listId,
                word = entity.word,
                mastered = entity.mastered,
                attempts = entity.attempts,
                correctCount = entity.correctCount,
                lastAttemptAt = entity.lastAttemptAt,
            )
        }

        val mathSessions = database.mathDao().getAllSessions().map { entity ->
            MathSessionBackup(
                id = entity.id,
                profileId = entity.profileId,
                operators = entity.operators,
                rangeMin = entity.rangeMin,
                rangeMax = entity.rangeMax,
                totalProblems = entity.totalProblems,
                correctCount = entity.correctCount,
                bestStreak = entity.bestStreak,
                avgResponseMs = entity.avgResponseMs,
                difficulty = entity.difficulty,
                completedAt = entity.completedAt,
            )
        }

        val pointEvents = database.pointsDao().getAllPoints().map { entity ->
            PointEventBackup(
                id = entity.id,
                profileId = entity.profileId,
                source = entity.source,
                points = entity.points,
                reason = entity.reason,
                timestamp = entity.timestamp,
            )
        }

        val avatarConfigs = database.avatarDao().getAllConfigs().map { entity ->
            AvatarConfigBackup(
                id = entity.id,
                profileId = entity.profileId,
                bodyId = entity.bodyId,
                hatId = entity.hatId,
                faceId = entity.faceId,
                outfitId = entity.outfitId,
                petId = entity.petId,
                equippedTitle = entity.equippedTitle,
                updatedAt = entity.updatedAt,
            )
        }

        val ownedRewards = database.rewardsDao().getAllOwnedRewards().map { entity ->
            OwnedRewardBackup(
                id = entity.id,
                profileId = entity.profileId,
                rewardId = entity.rewardId,
                category = entity.category,
                purchasedAt = entity.purchasedAt,
            )
        }

        val backup = BackupData(
            profiles = profiles,
            dicteeLists = dicteeLists,
            dicteeWords = dicteeWords,
            mathSessions = mathSessions,
            pointEvents = pointEvents,
            avatarConfigs = avatarConfigs,
            ownedRewards = ownedRewards,
        )

        return json.encodeToString(backup)
    }

    suspend fun restoreFromBackup(jsonString: String) {
        val backup = json.decodeFromString<BackupData>(jsonString)

        withContext(Dispatchers.IO) { database.clearAllTables() }

        backup.profiles.forEach { profile ->
            database.profileDao().insert(
                ProfileEntity(
                    id = profile.id,
                    name = profile.name,
                    locale = profile.locale,
                    totalPoints = profile.totalPoints,
                    bodyId = profile.bodyId,
                    hatId = profile.hatId,
                    faceId = profile.faceId,
                    outfitId = profile.outfitId,
                    petId = profile.petId,
                    equippedTitle = profile.equippedTitle,
                    createdAt = profile.createdAt,
                    updatedAt = profile.updatedAt,
                ),
            )
        }

        backup.dicteeLists.forEach { list ->
            database.dicteeDao().insertList(
                DicteeListEntity(
                    id = list.id,
                    profileId = list.profileId,
                    title = list.title,
                    language = list.language,
                    createdAt = list.createdAt,
                    updatedAt = list.updatedAt,
                ),
            )
        }

        backup.dicteeWords.forEach { word ->
            database.dicteeDao().insertWord(
                DicteeWordEntity(
                    id = word.id,
                    listId = word.listId,
                    word = word.word,
                    mastered = word.mastered,
                    attempts = word.attempts,
                    correctCount = word.correctCount,
                    lastAttemptAt = word.lastAttemptAt,
                ),
            )
        }

        backup.mathSessions.forEach { session ->
            database.mathDao().insert(
                MathSessionEntity(
                    id = session.id,
                    profileId = session.profileId,
                    operators = session.operators,
                    rangeMin = session.rangeMin,
                    rangeMax = session.rangeMax,
                    totalProblems = session.totalProblems,
                    correctCount = session.correctCount,
                    bestStreak = session.bestStreak,
                    avgResponseMs = session.avgResponseMs,
                    difficulty = session.difficulty,
                    completedAt = session.completedAt,
                ),
            )
        }

        backup.pointEvents.forEach { event ->
            database.pointsDao().insert(
                PointEventEntity(
                    id = event.id,
                    profileId = event.profileId,
                    source = event.source,
                    points = event.points,
                    reason = event.reason,
                    timestamp = event.timestamp,
                ),
            )
        }

        backup.avatarConfigs.forEach { config ->
            database.avatarDao().insert(
                AvatarConfigEntity(
                    id = config.id,
                    profileId = config.profileId,
                    bodyId = config.bodyId,
                    hatId = config.hatId,
                    faceId = config.faceId,
                    outfitId = config.outfitId,
                    petId = config.petId,
                    equippedTitle = config.equippedTitle,
                    updatedAt = config.updatedAt,
                ),
            )
        }

        backup.ownedRewards.forEach { reward ->
            database.rewardsDao().insert(
                OwnedRewardEntity(
                    id = reward.id,
                    profileId = reward.profileId,
                    rewardId = reward.rewardId,
                    category = reward.category,
                    purchasedAt = reward.purchasedAt,
                ),
            )
        }
    }
}
