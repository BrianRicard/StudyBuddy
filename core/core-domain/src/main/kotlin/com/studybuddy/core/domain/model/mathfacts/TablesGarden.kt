package com.studybuddy.core.domain.model.mathfacts

import com.studybuddy.core.domain.model.srs.LeitnerGrowth

/** One drillable item in a session: a fact plus its review state. */
data class TablesCard(
    val fact: MathFact,
    val box: Int,
    val isNew: Boolean,
)

/** One table's row in the garden: its growth derived from all ten facts. */
data class TableGarden(
    val table: Int,
    val growth: LeitnerGrowth,
)

/**
 * The whole garden for one profile.
 *
 * @property dueCardCount Facts due for review now.
 * @property dueTableCount Distinct tables with at least one due fact — the
 * number shown in the "tables à arroser" nudge.
 * @property newCardCount Facts never seen yet. A revision session serves these
 * once the due ones run out, so a garden with new cards still has work to do —
 * without this the very first launch would claim everything is watered.
 * @property tables All tables in roster order.
 */
data class TablesGarden(
    val dueCardCount: Int,
    val dueTableCount: Int,
    val newCardCount: Int,
    val tables: List<TableGarden>,
) {
    /** True only when there is genuinely nothing to drill right now. */
    val isFullyWatered: Boolean get() = dueCardCount == 0 && newCardCount == 0
}
