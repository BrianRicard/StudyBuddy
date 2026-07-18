package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.AtelierCard
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.datetime.Instant

/** How a drill session picks its cards. Route argument — names are stable. */
enum class DrillMode {
    /** The Leitner queue: due first, then new, then upcoming. */
    REVISION,

    /** Random cards from the whole Atelier. */
    SURPRISE,

    /** The six persons of one verb × tense garden cell, shuffled. */
    CELL,
}

class BuildDrillSessionUseCase @Inject constructor(
    private val revisionSession: GetAtelierSessionUseCase,
) {

    suspend operator fun invoke(
        mode: DrillMode,
        profileId: String,
        now: Instant,
        verbId: String? = null,
        tense: ConjugationTense? = null,
        random: Random = Random.Default,
    ): List<AtelierCard> = when (mode) {
        DrillMode.REVISION -> revisionSession(profileId, now)

        DrillMode.SURPRISE -> allCards().shuffled(random).take(GetAtelierSessionUseCase.SESSION_SIZE)

        DrillMode.CELL -> {
            val verb = requireNotNull(FrenchVerbs.byId(requireNotNull(verbId))) { "unknown verb: $verbId" }
            val cellTense = requireNotNull(tense)
            ConjugationPerson.entries
                .map { person ->
                    AtelierCard(verb = verb, tense = cellTense, person = person, box = 0, isNew = false)
                }
                .shuffled(random)
        }
    }

    private fun allCards(): List<AtelierCard> = FrenchVerbs.all.flatMap { verb ->
        ConjugationTense.entries.flatMap { tense ->
            ConjugationPerson.entries.map { person ->
                AtelierCard(verb = verb, tense = tense, person = person, box = 0, isNew = false)
            }
        }
    }
}
