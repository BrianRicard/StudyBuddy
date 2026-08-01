package com.studybuddy.core.domain.model.srs

import kotlinx.datetime.Instant

/**
 * Shared milestone arithmetic for the SRS gardens (Atelier des Verbes,
 * Jardin des Tables).
 *
 * Milestones are historical facts, not a view of current state: they are
 * derived from each card's latched "first reached the top box" stamp, so an
 * achievement date never drifts and a later lapse never un-earns it. Parents
 * tie real-world rewards to these, so they have to stay put.
 */
object LeitnerMilestones {

    /**
     * When each group (a verb, a table) became fully mastered, ascending.
     *
     * @param cards Every card, already filtered to the valid roster.
     * @param keyOf A card's unique identity, so a duplicated row cannot
     * complete a group on its own.
     * @param groupOf Which group a card belongs to.
     * @param masteredAt When the card first topped out, null if it never has.
     * @param sizeOfGroup How many cards a group needs to count as whole.
     */
    fun <T, G, K> groupCompletionTimes(
        cards: List<T>,
        keyOf: (T) -> K,
        groupOf: (T) -> G,
        masteredAt: (T) -> Instant?,
        sizeOfGroup: (G) -> Int,
    ): List<Instant> = cards
        .mapNotNull { card -> masteredAt(card)?.let { Triple(keyOf(card), groupOf(card), it) } }
        .distinctBy { (key, _, _) -> key }
        .groupBy({ (_, group, _) -> group }, { (_, _, time) -> time })
        .filter { (group, times) -> times.size >= sizeOfGroup(group) }
        // A group was whole when its last remaining card topped out.
        .map { (_, times) -> times.max() }
        .sorted()

    /**
     * Progress and achievement time for "complete [target] groups".
     *
     * @param times Group completion times, ascending.
     * @return current progress (capped at [target]) to when it was reached.
     */
    fun progressTowards(
        times: List<Instant>,
        target: Int,
    ): Pair<Int, Instant?> = times.size.coerceAtMost(target) to times.getOrNull(target - 1)
}
