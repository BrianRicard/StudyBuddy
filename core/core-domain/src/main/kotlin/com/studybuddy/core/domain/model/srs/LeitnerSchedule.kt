package com.studybuddy.core.domain.model.srs

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Leitner-box scheduling shared by the app's spaced-repetition drills
 * (Atelier des Verbes conjugation cards, Jardin des Tables math facts).
 *
 * Each card lives in a box from 0 (new / not yet planted) to [MAX_BOX]
 * (fully grown). A correct answer promotes the card one box and schedules it
 * further away; a wrong answer demotes it a single box — never all the way
 * down, so a lapse costs one growth stage at most (never-punishing) — and
 * brings it back tomorrow.
 */
object LeitnerSchedule {

    const val MAX_BOX = 4

    /**
     * Review interval per box, indexed by box number. Box 0 is "new": those
     * cards have no stored row yet and are due immediately, so index 0 is
     * never used for scheduling.
     */
    val BOX_INTERVALS: List<Duration> = listOf(Duration.ZERO, 1.days, 3.days, 7.days, 15.days)

    /** A lapsed card comes back tomorrow, whatever box it fell to. */
    val LAPSE_DELAY: Duration = 1.days

    /**
     * The card's state after an answer.
     *
     * @property box The new box.
     * @property nextDelay How long from now until the card is due again.
     */
    data class Outcome(
        val box: Int,
        val nextDelay: Duration,
    )

    /** Applies one answer to a card currently in [box]. */
    fun answered(
        box: Int,
        correct: Boolean,
    ): Outcome {
        val clamped = box.coerceIn(0, MAX_BOX)
        return if (correct) {
            val promoted = (clamped + 1).coerceAtMost(MAX_BOX)
            Outcome(box = promoted, nextDelay = BOX_INTERVALS[promoted])
        } else {
            Outcome(box = (clamped - 1).coerceAtLeast(0), nextDelay = LAPSE_DELAY)
        }
    }
}
