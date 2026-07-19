package com.studybuddy.core.domain.usecase.mathfacts

import com.studybuddy.core.domain.model.mathfacts.MathFactsRoster
import com.studybuddy.core.domain.model.mathfacts.TablesCard
import com.studybuddy.core.domain.repository.MathFactsReviewRepository
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant

/** How a tables drill session picks its cards. Route argument — names are stable. */
enum class TablesMode {
    /** The Leitner queue: due first, then new, then upcoming. */
    REVISION,

    /** Random facts from the whole garden. */
    SURPRISE,

    /** The ten facts of one table, shuffled. */
    TABLE,
}

/**
 * Builds a tables drill session:
 *
 * 1. Due cards first, most overdue first — repetition beats novelty.
 * 2. Then new (never-seen) facts in introduction order: table-major (all of
 *    the table de 2 before the table de 3, …), ×1 up to ×10 — so tables are
 *    learned one at a time, the way school recites them.
 * 3. If the session still has room, upcoming cards (soonest due first) fill
 *    it — reviewing a little early is harmless and the session should always
 *    feel full.
 */
class BuildTablesSessionUseCase @Inject constructor(
    private val repository: MathFactsReviewRepository,
) {

    suspend operator fun invoke(
        mode: TablesMode,
        profileId: String,
        now: Instant,
        table: Int? = null,
        size: Int = SESSION_SIZE,
        random: Random = Random.Default,
    ): List<TablesCard> = when (mode) {
        TablesMode.REVISION -> revisionSession(profileId, now, size)

        TablesMode.SURPRISE ->
            MathFactsRoster.all
                .shuffled(random)
                .take(size)
                .map { TablesCard(fact = it, box = 0, isNew = false) }

        TablesMode.TABLE -> {
            val chosen = requireNotNull(table) { "TABLE mode needs a table" }
            require(MathFactsRoster.factsOf(chosen).isNotEmpty()) { "unknown table: $chosen" }
            MathFactsRoster.factsOf(chosen)
                .shuffled(random)
                .map { TablesCard(fact = it, box = 0, isNew = false) }
        }
    }

    private suspend fun revisionSession(
        profileId: String,
        now: Instant,
        size: Int,
    ): List<TablesCard> {
        val reviews = repository.getReviews(profileId).first()
            .filter { MathFactsRoster.isValid(it.fact) }
            .associateBy { it.fact }

        val due = reviews.values
            .filter { it.dueAt <= now }
            .sortedBy { it.dueAt }
            .map { TablesCard(fact = it.fact, box = it.box, isNew = false) }

        val new = MathFactsRoster.all
            .filterNot { it in reviews }
            .map { TablesCard(fact = it, box = 0, isNew = true) }

        val upcoming = reviews.values
            .filter { it.dueAt > now }
            .sortedBy { it.dueAt }
            .map { TablesCard(fact = it.fact, box = it.box, isNew = false) }

        return (due + new + upcoming).take(size)
    }

    companion object {
        const val SESSION_SIZE = 10
    }
}
