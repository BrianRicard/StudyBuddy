package com.studybuddy.core.data.mapper

import com.studybuddy.core.data.db.entity.DicteeListEntity
import com.studybuddy.core.data.db.entity.DicteeWordEntity
import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.model.DicteeWord
import kotlinx.datetime.Instant

fun DicteeListEntity.toDomain(wordCount: Int = 0, masteredCount: Int = 0) = DicteeList(
    id = id,
    profileId = profileId,
    title = title,
    language = language,
    wordCount = wordCount,
    masteredCount = masteredCount,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt),
)

fun DicteeList.toEntity() = DicteeListEntity(
    id = id,
    profileId = profileId,
    title = title,
    language = language,
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds(),
)

fun DicteeWordEntity.toDomain() = DicteeWord(
    id = id,
    listId = listId,
    word = word,
    mastered = mastered,
    attempts = attempts,
    correctCount = correctCount,
    lastAttemptAt = lastAttemptAt?.let { Instant.fromEpochMilliseconds(it) },
)

fun DicteeWord.toEntity() = DicteeWordEntity(
    id = id,
    listId = listId,
    word = word,
    mastered = mastered,
    attempts = attempts,
    correctCount = correctCount,
    lastAttemptAt = lastAttemptAt?.toEpochMilliseconds(),
)
