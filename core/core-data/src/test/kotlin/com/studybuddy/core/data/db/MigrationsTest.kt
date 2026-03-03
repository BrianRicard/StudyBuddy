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
