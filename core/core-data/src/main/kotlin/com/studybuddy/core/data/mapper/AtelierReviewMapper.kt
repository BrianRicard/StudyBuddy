package com.studybuddy.core.data.mapper

import com.studybuddy.core.data.db.entity.AtelierReviewEntity
import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import kotlinx.datetime.Instant

/**
 * Maps a row to the domain model, or null when the tense or person name is
 * unknown (e.g. data written by a newer app version) — one bad row must not
 * kill the whole review flow.
 */
fun AtelierReviewEntity.toDomainOrNull(): AtelierReview? {
    val parsedTense = runCatching { ConjugationTense.valueOf(tense) }.getOrNull() ?: return null
    val parsedPerson = runCatching { ConjugationPerson.valueOf(person) }.getOrNull() ?: return null
    return AtelierReview(
        id = id,
        profileId = profileId,
        verbId = verbId,
        tense = parsedTense,
        person = parsedPerson,
        box = box,
        dueAt = Instant.fromEpochMilliseconds(dueAt),
        lapses = lapses,
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
    )
}

fun AtelierReview.toEntity() = AtelierReviewEntity(
    id = id,
    profileId = profileId,
    verbId = verbId,
    tense = tense.name,
    person = person.name,
    box = box,
    dueAt = dueAt.toEpochMilliseconds(),
    lapses = lapses,
    updatedAt = updatedAt.toEpochMilliseconds(),
)
