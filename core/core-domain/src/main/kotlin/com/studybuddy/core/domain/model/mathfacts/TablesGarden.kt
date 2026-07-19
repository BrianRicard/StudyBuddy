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
 * @property tables All tables in roster order.
 */
data class TablesGarden(
    val dueCardCount: Int,
    val dueTableCount: Int,
    val tables: List<TableGarden>,
)
