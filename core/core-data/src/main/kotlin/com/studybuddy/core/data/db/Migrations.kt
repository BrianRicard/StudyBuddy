package com.studybuddy.core.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database migrations.
 *
 * Each migration must match the exact schema that Room generates for that version.
 * Schema JSON files live in `core/core-data/schemas/`.
 */
object Migrations {

    /**
     * v1 → v2: Add the four poem-related tables introduced by the Poems feature.
     *
     * Tables added:
     * - `cached_poems`      — poems fetched from PoetryDB API
     * - `favourite_poems`    — user's favourite poem bookmarks
     * - `reading_sessions`   — pronunciation scoring results
     * - `user_poems`         — poems created/imported by the user
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `cached_poems` (
                    `id` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `author` TEXT NOT NULL,
                    `lines` TEXT NOT NULL,
                    `language` TEXT NOT NULL,
                    `source` TEXT NOT NULL,
                    `tags` TEXT NOT NULL,
                    `cachedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `favourite_poems` (
                    `id` TEXT NOT NULL,
                    `poemId` TEXT NOT NULL,
                    `poemSource` TEXT NOT NULL,
                    `profileId` TEXT NOT NULL,
                    `favouritedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reading_sessions` (
                    `id` TEXT NOT NULL,
                    `profileId` TEXT NOT NULL,
                    `poemId` TEXT NOT NULL,
                    `score` REAL NOT NULL,
                    `accuracyPct` REAL NOT NULL,
                    `durationSeconds` INTEGER NOT NULL,
                    `language` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `user_poems` (
                    `id` TEXT NOT NULL,
                    `profileId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `author` TEXT NOT NULL,
                    `lines` TEXT NOT NULL,
                    `language` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_user_poems_profileId` ON `user_poems` (`profileId`)",
            )
        }
    }

    /**
     * v2 -> v3: Add reading comprehension tables.
     *
     * Tables added:
     * - `reading_passages`  - bundled reading passages
     * - `reading_questions`  - questions for each passage
     * - `reading_results`   - user's completed reading sessions
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reading_passages` (
                    `id` TEXT NOT NULL,
                    `language` TEXT NOT NULL,
                    `tier` INTEGER NOT NULL,
                    `theme` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `passage` TEXT NOT NULL,
                    `wordCount` INTEGER NOT NULL,
                    `source` TEXT NOT NULL,
                    `sourceAttribution` TEXT,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reading_questions` (
                    `id` TEXT NOT NULL,
                    `passageId` TEXT NOT NULL,
                    `questionIndex` INTEGER NOT NULL,
                    `type` TEXT NOT NULL,
                    `questionText` TEXT NOT NULL,
                    `options` TEXT,
                    `correctAnswer` TEXT NOT NULL,
                    `explanation` TEXT NOT NULL,
                    `evidenceSentenceIndex` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`passageId`) REFERENCES `reading_passages`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_reading_questions_passageId` ON `reading_questions` (`passageId`)",
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reading_results` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `passageId` TEXT NOT NULL,
                    `score` INTEGER NOT NULL,
                    `totalQuestions` INTEGER NOT NULL,
                    `pointsEarned` INTEGER NOT NULL,
                    `readingTimeMs` INTEGER NOT NULL,
                    `questionsTimeMs` INTEGER NOT NULL,
                    `completedAt` INTEGER NOT NULL,
                    `allCorrectFirstTry` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

    /**
     * v3 -> v4: Add the conjugation quest progress table.
     * One row per (profile, stage, step) storing the child's best result.
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `conjugation_progress` (
                    `id` TEXT NOT NULL,
                    `profileId` TEXT NOT NULL,
                    `stageId` TEXT NOT NULL,
                    `step` TEXT NOT NULL,
                    `bestCorrect` INTEGER NOT NULL,
                    `bestTotal` INTEGER NOT NULL,
                    `completedAt` INTEGER,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_conjugation_progress_profileId_stageId_step` " +
                    "ON `conjugation_progress` (`profileId`, `stageId`, `step`)",
            )
        }
    }

    /**
     * v4 -> v5: Add the Atelier des Verbes review table.
     * One Leitner-scheduled row per (profile, verb, tense, person) card,
     * created the first time the card is answered in a drill.
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `atelier_review` (
                    `id` TEXT NOT NULL,
                    `profileId` TEXT NOT NULL,
                    `verbId` TEXT NOT NULL,
                    `tense` TEXT NOT NULL,
                    `person` TEXT NOT NULL,
                    `box` INTEGER NOT NULL,
                    `dueAt` INTEGER NOT NULL,
                    `lapses` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )

            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_atelier_review_profileId_verbId_tense_person` " +
                    "ON `atelier_review` (`profileId`, `verbId`, `tense`, `person`)",
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_atelier_review_profileId_dueAt` " +
                    "ON `atelier_review` (`profileId`, `dueAt`)",
            )
        }
    }
}
