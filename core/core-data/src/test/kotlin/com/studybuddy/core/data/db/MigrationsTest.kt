package com.studybuddy.core.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Validates that [Migrations] SQL statements match the expected Room schema.
 *
 * These are pure JUnit tests that verify the migration SQL strings contain the
 * correct table definitions. Full integration testing with MigrationTestHelper
 * requires an Android instrumented test environment.
 */
class MigrationsTest {

    @Test
    fun `MIGRATION_1_2 creates cached_poems table`() {
        val sql = getMigrationSql()
        assertTrue(
            sql.any { it.contains("CREATE TABLE IF NOT EXISTS `cached_poems`") },
            "MIGRATION_1_2 should create cached_poems table",
        )
        val cachedPoemsSql = sql.first { it.contains("cached_poems") }
        listOf("id", "title", "author", "lines", "language", "source", "tags", "cachedAt").forEach { col ->
            assertTrue(
                cachedPoemsSql.contains("`$col`"),
                "cached_poems should have column '$col'",
            )
        }
    }

    @Test
    fun `MIGRATION_1_2 creates favourite_poems table`() {
        val sql = getMigrationSql()
        assertTrue(
            sql.any { it.contains("CREATE TABLE IF NOT EXISTS `favourite_poems`") },
            "MIGRATION_1_2 should create favourite_poems table",
        )
        val favSql = sql.first { it.contains("favourite_poems") }
        listOf("id", "poemId", "poemSource", "profileId", "favouritedAt").forEach { col ->
            assertTrue(
                favSql.contains("`$col`"),
                "favourite_poems should have column '$col'",
            )
        }
    }

    @Test
    fun `MIGRATION_1_2 creates reading_sessions table`() {
        val sql = getMigrationSql()
        assertTrue(
            sql.any { it.contains("CREATE TABLE IF NOT EXISTS `reading_sessions`") },
            "MIGRATION_1_2 should create reading_sessions table",
        )
        val readingSql = sql.first { it.contains("reading_sessions") }
        listOf("id", "profileId", "poemId", "score", "accuracyPct", "durationSeconds", "language", "createdAt")
            .forEach { col ->
                assertTrue(readingSql.contains("`$col`"), "reading_sessions should have column '$col'")
            }
        assertTrue(readingSql.contains("`score` REAL"), "score should be REAL type")
        assertTrue(readingSql.contains("`accuracyPct` REAL"), "accuracyPct should be REAL type")
    }

    @Test
    fun `MIGRATION_1_2 creates user_poems table with foreign key`() {
        val sql = getMigrationSql()
        assertTrue(
            sql.any { it.contains("CREATE TABLE IF NOT EXISTS `user_poems`") },
            "MIGRATION_1_2 should create user_poems table",
        )
        val userPoemsSql = sql.first { it.contains("`user_poems`") && it.contains("CREATE TABLE") }
        assertTrue(
            userPoemsSql.contains("FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`)"),
            "user_poems should have foreign key referencing profiles",
        )
        assertTrue(
            userPoemsSql.contains("ON DELETE CASCADE"),
            "user_poems foreign key should cascade on delete",
        )
    }

    @Test
    fun `MIGRATION_1_2 creates index on user_poems profileId`() {
        val sql = getMigrationSql()
        assertTrue(
            sql.any { it.contains("CREATE INDEX") && it.contains("user_poems") && it.contains("profileId") },
            "MIGRATION_1_2 should create index on user_poems.profileId",
        )
    }

    @Test
    fun `MIGRATION_1_2 produces exactly 5 SQL statements`() {
        val sql = getMigrationSql()
        // 4 CREATE TABLE + 1 CREATE INDEX
        assertTrue(
            sql.size == 5,
            "MIGRATION_1_2 should produce 5 SQL statements (4 tables + 1 index), got ${sql.size}",
        )
    }

    @Test
    fun `MIGRATION_1_2 does not touch existing v1 tables`() {
        val sql = getMigrationSql()
        val v1Tables = listOf(
            "profiles",
            "dictee_lists",
            "dictee_words",
            "math_sessions",
            "point_events",
            "avatar_configs",
            "owned_rewards",
            "voice_packs",
        )
        v1Tables.forEach { table ->
            assertTrue(
                sql.none { it.contains("ALTER TABLE") && it.contains(table) },
                "MIGRATION_1_2 should not ALTER existing table '$table'",
            )
            assertTrue(
                sql.none { it.contains("DROP TABLE") && it.contains(table) },
                "MIGRATION_1_2 should not DROP existing table '$table'",
            )
        }
    }

    @Test
    fun `MIGRATION_3_4 creates conjugation_progress table with unique index`() {
        val sql = getMigration34Sql()
        assertTrue(
            sql.any { it.contains("CREATE TABLE IF NOT EXISTS `conjugation_progress`") },
            "MIGRATION_3_4 should create conjugation_progress table",
        )
        val tableSql = sql.first { it.contains("CREATE TABLE IF NOT EXISTS `conjugation_progress`") }
        listOf("id", "profileId", "stageId", "step", "bestCorrect", "bestTotal", "completedAt", "updatedAt")
            .forEach { col ->
                assertTrue(
                    tableSql.contains("`$col`"),
                    "conjugation_progress should have column '$col'",
                )
            }
        val expectedIndex = "CREATE UNIQUE INDEX IF NOT EXISTS " +
            "`index_conjugation_progress_profileId_stageId_step`"
        assertTrue(
            sql.any { it.contains(expectedIndex) },
            "MIGRATION_3_4 should create the unique (profileId, stageId, step) index",
        )
    }

    @Test
    fun `MIGRATION_3_4 does not touch existing tables`() {
        val sql = getMigration34Sql()
        assertTrue(
            sql.none { it.contains("ALTER TABLE") || it.contains("DROP TABLE") },
            "MIGRATION_3_4 should only create new objects",
        )
    }

    @Test
    fun `MIGRATION_4_5 creates atelier_review table with all columns`() {
        val sql = getMigration45Sql()
        assertTrue(
            sql.any { it.contains("CREATE TABLE IF NOT EXISTS `atelier_review`") },
            "MIGRATION_4_5 should create atelier_review table",
        )
        val tableSql = sql.first { it.contains("`atelier_review`") && it.contains("CREATE TABLE") }
        listOf("id", "profileId", "verbId", "tense", "person", "box", "dueAt", "lapses", "updatedAt")
            .forEach { col ->
                assertTrue(
                    tableSql.contains("`$col`"),
                    "atelier_review should have column '$col'",
                )
            }
    }

    @Test
    fun `MIGRATION_4_5 creates the unique card index and the due-date index`() {
        val sql = getMigration45Sql()
        val uniqueIndex = "CREATE UNIQUE INDEX IF NOT EXISTS " +
            "`index_atelier_review_profileId_verbId_tense_person`"
        assertTrue(
            sql.any { it.contains(uniqueIndex) },
            "MIGRATION_4_5 should create the unique (profileId, verbId, tense, person) index",
        )
        val dueIndex = "CREATE INDEX IF NOT EXISTS `index_atelier_review_profileId_dueAt`"
        assertTrue(
            sql.any { it.contains(dueIndex) },
            "MIGRATION_4_5 should create the (profileId, dueAt) index",
        )
    }

    @Test
    fun `MIGRATION_4_5 does not touch existing tables`() {
        val sql = getMigration45Sql()
        assertTrue(
            sql.none { it.contains("ALTER TABLE") || it.contains("DROP TABLE") },
            "MIGRATION_4_5 should only create new objects",
        )
    }

    @Test
    fun `MIGRATION_5_6 creates math_facts_review table with all columns`() {
        val sql = getMigration56Sql()
        assertTrue(
            sql.any { it.contains("CREATE TABLE IF NOT EXISTS `math_facts_review`") },
            "MIGRATION_5_6 should create math_facts_review table",
        )
        val tableSql = sql.first { it.contains("`math_facts_review`") && it.contains("CREATE TABLE") }
        listOf("id", "profileId", "tableNumber", "multiplicand", "box", "dueAt", "lapses", "updatedAt")
            .forEach { col ->
                assertTrue(
                    tableSql.contains("`$col`"),
                    "math_facts_review should have column '$col'",
                )
            }
    }

    @Test
    fun `MIGRATION_5_6 creates the unique fact index and the due-date index`() {
        val sql = getMigration56Sql()
        val uniqueIndex = "CREATE UNIQUE INDEX IF NOT EXISTS " +
            "`index_math_facts_review_profileId_tableNumber_multiplicand`"
        assertTrue(
            sql.any { it.contains(uniqueIndex) },
            "MIGRATION_5_6 should create the unique (profileId, tableNumber, multiplicand) index",
        )
        val dueIndex = "CREATE INDEX IF NOT EXISTS `index_math_facts_review_profileId_dueAt`"
        assertTrue(
            sql.any { it.contains(dueIndex) },
            "MIGRATION_5_6 should create the (profileId, dueAt) index",
        )
    }

    @Test
    fun `MIGRATION_5_6 does not touch existing tables`() {
        val sql = getMigration56Sql()
        assertTrue(
            sql.none { it.contains("ALTER TABLE") || it.contains("DROP TABLE") },
            "MIGRATION_5_6 should only create new objects",
        )
    }

    private fun getMigration56Sql(): List<String> {
        val statements = mutableListOf<String>()
        val sqlSlot = slot<String>()
        val fakeDb = mockk<SupportSQLiteDatabase> {
            every { execSQL(capture(sqlSlot)) } answers {
                statements.add(sqlSlot.captured)
            }
        }
        Migrations.MIGRATION_5_6.migrate(fakeDb)
        return statements
    }

    private fun getMigration45Sql(): List<String> {
        val statements = mutableListOf<String>()
        val sqlSlot = slot<String>()
        val fakeDb = mockk<SupportSQLiteDatabase> {
            every { execSQL(capture(sqlSlot)) } answers {
                statements.add(sqlSlot.captured)
            }
        }
        Migrations.MIGRATION_4_5.migrate(fakeDb)
        return statements
    }

    private fun getMigration34Sql(): List<String> {
        val statements = mutableListOf<String>()
        val sqlSlot = slot<String>()
        val fakeDb = mockk<SupportSQLiteDatabase> {
            every { execSQL(capture(sqlSlot)) } answers {
                statements.add(sqlSlot.captured)
            }
        }
        Migrations.MIGRATION_3_4.migrate(fakeDb)
        return statements
    }

    private fun getMigrationSql(): List<String> {
        val statements = mutableListOf<String>()
        val sqlSlot = slot<String>()
        val fakeDb = mockk<SupportSQLiteDatabase> {
            every { execSQL(capture(sqlSlot)) } answers {
                statements.add(sqlSlot.captured)
            }
        }
        Migrations.MIGRATION_1_2.migrate(fakeDb)
        return statements
    }
}
