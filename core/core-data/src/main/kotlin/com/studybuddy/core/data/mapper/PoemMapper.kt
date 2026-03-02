package com.studybuddy.core.data.mapper

import com.studybuddy.core.data.db.entity.CachedPoemEntity
import com.studybuddy.core.data.db.entity.ReadingSessionEntity
import com.studybuddy.core.data.db.entity.UserPoemEntity
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.PoemSource
import com.studybuddy.core.domain.model.ReadingSession
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun CachedPoemEntity.toDomain(isFavourite: Boolean = false): Poem = Poem(
    id = id,
    title = title,
    author = author,
    lines = json.decodeFromString<List<String>>(lines),
    language = language,
    source = PoemSource.valueOf(source),
    isFavourite = isFavourite,
    tags = json.decodeFromString<List<String>>(tags),
    cachedAt = Instant.fromEpochMilliseconds(cachedAt),
)

fun UserPoemEntity.toDomain(isFavourite: Boolean = false): Poem = Poem(
    id = id,
    title = title,
    author = author,
    lines = json.decodeFromString<List<String>>(lines),
    language = language,
    source = PoemSource.USER,
    isFavourite = isFavourite,
)

fun Poem.toCachedEntity(): CachedPoemEntity = CachedPoemEntity(
    id = id,
    title = title,
    author = author,
    lines = json.encodeToString(lines),
    language = language,
    source = source.name,
    tags = json.encodeToString(tags),
    cachedAt = cachedAt?.toEpochMilliseconds() ?: System.currentTimeMillis(),
)

fun Poem.toUserEntity(profileId: String): UserPoemEntity = UserPoemEntity(
    id = id,
    profileId = profileId,
    title = title,
    author = author,
    lines = json.encodeToString(lines),
    language = language,
    createdAt = System.currentTimeMillis(),
)

fun ReadingSessionEntity.toDomain(): ReadingSession = ReadingSession(
    id = id,
    profileId = profileId,
    poemId = poemId,
    score = score,
    accuracyPct = accuracyPct,
    durationSeconds = durationSeconds,
    language = language,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
)

fun ReadingSession.toEntity(): ReadingSessionEntity = ReadingSessionEntity(
    id = id,
    profileId = profileId,
    poemId = poemId,
    score = score,
    accuracyPct = accuracyPct,
    durationSeconds = durationSeconds,
    language = language,
    createdAt = createdAt.toEpochMilliseconds(),
)
