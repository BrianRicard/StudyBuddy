package com.studybuddy.core.data.backup

import com.studybuddy.core.common.constants.AppConstants
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Guards the backup wire format — especially that adding Atelier reviews in
 * schema v2 did not break restoring older (v1) backups.
 */
class BackupDataSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun atelierReview(id: String) = AtelierReviewBackup(
        id = id,
        profileId = "p1",
        verbId = "etre",
        tense = "PRESENT",
        person = "JE",
        box = 3,
        dueAt = 1_750_000_000_000,
        lapses = 1,
        updatedAt = 1_750_000_000_000,
    )

    @Test
    fun `atelier reviews round-trip through json`() {
        val original = BackupData(
            profiles = listOf(
                ProfileBackup(
                    id = "p1",
                    name = "Kid",
                    locale = "fr",
                    totalPoints = 10,
                    bodyId = "fox",
                    hatId = "none",
                    faceId = "none",
                    outfitId = "default",
                    petId = "none",
                    createdAt = 0,
                    updatedAt = 0,
                ),
            ),
            atelierReviews = listOf(atelierReview("a1"), atelierReview("a2")),
        )

        val restored = json.decodeFromString<BackupData>(json.encodeToString(original))

        assertEquals(2, restored.atelierReviews.size)
        assertEquals(original.atelierReviews, restored.atelierReviews)
        assertEquals(AppConstants.BACKUP_SCHEMA_VERSION, restored.version)
    }

    @Test
    fun `a v1 backup without atelier reviews still restores`() {
        // A backup produced before schema v2 has no atelierReviews key at all.
        val v1Json = """
            {
              "version": 1,
              "profiles": [],
              "dicteeLists": [],
              "dicteeWords": [],
              "mathSessions": [],
              "pointEvents": [],
              "avatarConfigs": [],
              "ownedRewards": []
            }
        """.trimIndent()

        val restored = json.decodeFromString<BackupData>(v1Json)

        assertEquals(1, restored.version)
        assertTrue(restored.atelierReviews.isEmpty())
    }

    @Test
    fun `new backups declare the current schema version`() {
        assertEquals(AppConstants.BACKUP_SCHEMA_VERSION, BackupData().version)
    }
}
