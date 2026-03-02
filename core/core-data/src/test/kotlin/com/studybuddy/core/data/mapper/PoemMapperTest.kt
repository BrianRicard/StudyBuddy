package com.studybuddy.core.data.mapper

import com.studybuddy.core.data.db.entity.CachedPoemEntity
import com.studybuddy.core.data.db.entity.ReadingSessionEntity
import com.studybuddy.core.data.db.entity.UserPoemEntity
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.PoemSource
import com.studybuddy.core.domain.model.ReadingSession
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PoemMapperTest {

    @Test
    fun `CachedPoemEntity toDomain maps correctly`() {
        val entity = CachedPoemEntity(
            id = "id-1",
            title = "Test Poem",
            author = "Author",
            lines = """["Line 1","Line 2"]""",
            language = "en",
            source = "API",
            tags = """["classic"]""",
            cachedAt = 1000L,
        )
        val poem = entity.toDomain(isFavourite = true)

        assertEquals("id-1", poem.id)
        assertEquals("Test Poem", poem.title)
        assertEquals("Author", poem.author)
        assertEquals(listOf("Line 1", "Line 2"), poem.lines)
        assertEquals("en", poem.language)
        assertEquals(PoemSource.API, poem.source)
        assertEquals(true, poem.isFavourite)
        assertEquals(listOf("classic"), poem.tags)
        assertEquals(Instant.fromEpochMilliseconds(1000L), poem.cachedAt)
    }

    @Test
    fun `UserPoemEntity toDomain maps correctly`() {
        val entity = UserPoemEntity(
            id = "id-2",
            profileId = "profile-1",
            title = "My Poem",
            author = "Me",
            lines = """["Hello","World"]""",
            language = "fr",
            createdAt = 2000L,
        )
        val poem = entity.toDomain(isFavourite = false)

        assertEquals("id-2", poem.id)
        assertEquals("My Poem", poem.title)
        assertEquals(PoemSource.USER, poem.source)
        assertEquals(false, poem.isFavourite)
        assertEquals(listOf("Hello", "World"), poem.lines)
    }

    @Test
    fun `Poem toCachedEntity round-trips correctly`() {
        val poem = Poem(
            id = "id-3",
            title = "Round Trip",
            author = "Author",
            lines = listOf("A", "B", "C"),
            language = "de",
            source = PoemSource.BUNDLED,
            tags = listOf("tag1", "tag2"),
            cachedAt = Instant.fromEpochMilliseconds(3000L),
        )
        val entity = poem.toCachedEntity()
        val roundTripped = entity.toDomain()

        assertEquals(poem.id, roundTripped.id)
        assertEquals(poem.title, roundTripped.title)
        assertEquals(poem.author, roundTripped.author)
        assertEquals(poem.lines, roundTripped.lines)
        assertEquals(poem.language, roundTripped.language)
        assertEquals(poem.source, roundTripped.source)
        assertEquals(poem.tags, roundTripped.tags)
    }

    @Test
    fun `Poem toUserEntity maps profile correctly`() {
        val poem = Poem(
            id = "id-4",
            title = "User Poem",
            author = "Kid",
            lines = listOf("Hello"),
            language = "en",
            source = PoemSource.USER,
        )
        val entity = poem.toUserEntity("profile-123")

        assertEquals("id-4", entity.id)
        assertEquals("profile-123", entity.profileId)
        assertEquals("User Poem", entity.title)
    }

    @Test
    fun `ReadingSessionEntity toDomain maps correctly`() {
        val entity = ReadingSessionEntity(
            id = "session-1",
            profileId = "profile-1",
            poemId = "poem-1",
            score = 0.85f,
            accuracyPct = 85.0f,
            durationSeconds = 120,
            language = "fr",
            createdAt = 5000L,
        )
        val session = entity.toDomain()

        assertEquals("session-1", session.id)
        assertEquals(0.85f, session.score)
        assertEquals(85.0f, session.accuracyPct)
        assertEquals(120, session.durationSeconds)
        assertEquals(Instant.fromEpochMilliseconds(5000L), session.createdAt)
    }

    @Test
    fun `ReadingSession toEntity round-trips correctly`() {
        val session = ReadingSession(
            id = "session-2",
            profileId = "profile-2",
            poemId = "poem-2",
            score = 0.92f,
            accuracyPct = 92.0f,
            durationSeconds = 60,
            language = "de",
            createdAt = Instant.fromEpochMilliseconds(6000L),
        )
        val entity = session.toEntity()
        val roundTripped = entity.toDomain()

        assertEquals(session.id, roundTripped.id)
        assertEquals(session.score, roundTripped.score)
        assertEquals(session.durationSeconds, roundTripped.durationSeconds)
        assertEquals(session.createdAt, roundTripped.createdAt)
    }
}
