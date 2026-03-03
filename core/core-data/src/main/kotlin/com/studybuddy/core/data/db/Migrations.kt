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
}
