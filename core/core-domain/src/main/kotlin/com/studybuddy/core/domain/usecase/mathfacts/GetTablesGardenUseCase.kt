package com.studybuddy.core.domain.usecase.mathfacts

import com.studybuddy.core.domain.model.mathfacts.MathFactReview
import com.studybuddy.core.domain.model.mathfacts.MathFactsRoster
import com.studybuddy.core.domain.model.mathfacts.TableGarden
import com.studybuddy.core.domain.model.mathfacts.TablesGarden
import com.studybuddy.core.domain.model.srs.LeitnerGrowth
import com.studybuddy.core.domain.repository.MathFactsReviewRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/**
 * Emits the tables garden: per-table growth stages plus the due counts that
 * drive the Révision button and the home-screen nudge.
 */
class GetTablesGardenUseCase @Inject constructor(
    private val repository: MathFactsReviewRepository,
) {

    operator fun invoke(
        profileId: String,
        now: Instant,
    ): Flow<TablesGarden> = repository.getReviews(profileId).map { reviews -> buildGarden(reviews, now) }

    private fun buildGarden(
        reviews: List<MathFactReview>,
        now: Instant,
    ): TablesGarden {
        val valid = reviews.filter { MathFactsRoster.isValid(it.fact) }
        val byFact = valid.associateBy { it.fact }
        val due = valid.filter { it.dueAt <= now }

        val tables = MathFactsRoster.tables.map { table ->
            TableGarden(
                table = table,
                growth = LeitnerGrowth.fromBoxes(
                    MathFactsRoster.factsOf(table).map { fact -> byFact[fact]?.box ?: 0 },
                ),
            )
        }

        return TablesGarden(
            dueCardCount = due.size,
            dueTableCount = due.distinctBy { it.table }.size,
            tables = tables,
        )
    }
}
