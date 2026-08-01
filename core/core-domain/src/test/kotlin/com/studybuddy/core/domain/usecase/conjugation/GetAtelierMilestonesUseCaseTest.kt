package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.AtelierMilestone
import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import com.studybuddy.core.domain.model.srs.LeitnerSchedule
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetAtelierMilestonesUseCaseTest {

    private val useCase = GetAtelierMilestonesUseCase()

    private fun review(
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        box: Int,
        updatedAtMillis: Long = 1_000,
        masteredAtMillis: Long? = updatedAtMillis.takeIf { box >= LeitnerSchedule.MAX_BOX },
    ) = AtelierReview(
        id = "$verbId-$tense-$person",
        profileId = "p1",
        verbId = verbId,
        tense = tense,
        person = person,
        box = box,
        dueAt = Instant.fromEpochMilliseconds(updatedAtMillis),
        lapses = 0,
        updatedAt = Instant.fromEpochMilliseconds(updatedAtMillis),
        masteredAt = masteredAtMillis?.let { Instant.fromEpochMilliseconds(it) },
    )

    /** Every card of [verbId] at [box], each stamped [updatedAtMillis]. */
    private fun fullVerb(
        verbId: String,
        box: Int,
        updatedAtMillis: Long,
    ) = ConjugationTense.entries.flatMap { tense ->
        ConjugationPerson.entries.map { person ->
            review(verbId, tense, person, box, updatedAtMillis)
        }
    }

    private fun List<com.studybuddy.core.domain.model.conjugation.AtelierMilestoneStatus>.of(
        milestone: AtelierMilestone,
    ) = single { it.milestone == milestone }

    @Test
    fun `an empty profile has no milestones achieved`() {
        val result = useCase(emptyList())

        assertEquals(AtelierMilestone.entries.size, result.size)
        assertTrue(result.none { it.isAchieved })
        assertEquals(FrenchVerbs.all.size, result.of(AtelierMilestone.ALL_VERBS_MASTERED).target)
    }

    @Test
    fun `one card at the top box clinches the first-card milestone only`() {
        val result = useCase(
            listOf(
                review("etre", ConjugationTense.PRESENT, ConjugationPerson.JE, LeitnerSchedule.MAX_BOX, 500),
                review("etre", ConjugationTense.PRESENT, ConjugationPerson.TU, box = 1),
            ),
        )

        val firstCard = result.of(AtelierMilestone.FIRST_CARD_MASTERED)
        assertTrue(firstCard.isAchieved)
        assertEquals(Instant.fromEpochMilliseconds(500), firstCard.achievedAt)
        assertFalse(result.of(AtelierMilestone.FIRST_VERB_MASTERED).isAchieved)
    }

    @Test
    fun `cards below the top box never count`() {
        val result = useCase(fullVerb("etre", box = LeitnerSchedule.MAX_BOX - 1, updatedAtMillis = 100))

        assertTrue(result.none { it.isAchieved })
        assertEquals(0, result.of(AtelierMilestone.FIRST_CARD_MASTERED).current)
    }

    @Test
    fun `all cards of a verb at the top box masters that verb`() {
        val result = useCase(fullVerb("aimer", box = LeitnerSchedule.MAX_BOX, updatedAtMillis = 900))

        val firstVerb = result.of(AtelierMilestone.FIRST_VERB_MASTERED)
        assertTrue(firstVerb.isAchieved)
        assertEquals(1, firstVerb.current)
        // Timestamp is the last update that completed the mastery.
        assertEquals(Instant.fromEpochMilliseconds(900), firstVerb.achievedAt)
    }

    @Test
    fun `a verb with a card that never topped out is not mastered`() {
        val cards = fullVerb("aimer", box = LeitnerSchedule.MAX_BOX, updatedAtMillis = 900).toMutableList()
        // One card has never reached the top box at all.
        val neverMastered = cards.removeAt(0).copy(box = 1, masteredAt = null)
        cards.add(neverMastered)

        val result = useCase(cards)

        assertFalse(result.of(AtelierMilestone.FIRST_VERB_MASTERED).isAchieved)
        // The other 17 cards still count toward the first-card milestone.
        assertTrue(result.of(AtelierMilestone.FIRST_CARD_MASTERED).isAchieved)
    }

    @Test
    fun `a later lapse does not un-earn a verb the child already mastered`() {
        val cards = fullVerb("aimer", box = LeitnerSchedule.MAX_BOX, updatedAtMillis = 900).toMutableList()
        // The child slips on one card months later: the box falls, but the
        // card was mastered once and the parent already gave the reward.
        val lapsed = cards.removeAt(0).copy(box = LeitnerSchedule.MAX_BOX - 1)
        cards.add(lapsed)

        val result = useCase(cards)

        val verb = result.of(AtelierMilestone.FIRST_VERB_MASTERED)
        assertTrue(verb.isAchieved)
        assertEquals(Instant.fromEpochMilliseconds(900), verb.achievedAt)
    }

    @Test
    fun `re-drilling a mastered verb never moves its achievement date`() {
        val earned = fullVerb("aimer", box = LeitnerSchedule.MAX_BOX, updatedAtMillis = 900)
        // Every card is reviewed again a month later; only updatedAt moves.
        val reviewedAgain = earned.map { it.copy(updatedAt = Instant.fromEpochMilliseconds(9_000)) }

        val before = useCase(earned).of(AtelierMilestone.FIRST_VERB_MASTERED)
        val after = useCase(reviewedAgain).of(AtelierMilestone.FIRST_VERB_MASTERED)

        assertEquals(before.achievedAt, after.achievedAt)
        assertEquals(Instant.fromEpochMilliseconds(900), after.achievedAt)
    }

    @Test
    fun `five mastered verbs clinch the five-verbs milestone with the fifth timestamp`() {
        val verbs = FrenchVerbs.all.take(6).map { it.id }
        val reviews = verbs.flatMapIndexed { index, verbId ->
            fullVerb(verbId, box = LeitnerSchedule.MAX_BOX, updatedAtMillis = (index + 1) * 1_000L)
        }

        val result = useCase(reviews)

        val five = result.of(AtelierMilestone.FIVE_VERBS_MASTERED)
        assertTrue(five.isAchieved)
        assertEquals(5, five.current)
        // Achieved when the 5th verb (updatedAt 5000) was mastered, not the 6th.
        assertEquals(Instant.fromEpochMilliseconds(5_000), five.achievedAt)
        assertEquals(6, result.of(AtelierMilestone.ALL_VERBS_MASTERED).current)
        assertFalse(result.of(AtelierMilestone.ALL_VERBS_MASTERED).isAchieved)
    }

    @Test
    fun `mastering every verb clinches all milestones`() {
        val reviews = FrenchVerbs.all.flatMap { verb ->
            fullVerb(verb.id, box = LeitnerSchedule.MAX_BOX, updatedAtMillis = 2_000)
        }

        val result = useCase(reviews)

        assertTrue(result.all { it.isAchieved })
        assertEquals(FrenchVerbs.all.size, result.of(AtelierMilestone.ALL_VERBS_MASTERED).current)
    }

    @Test
    fun `unknown verb rows are ignored`() {
        val result = useCase(fullVerb("licorne", box = LeitnerSchedule.MAX_BOX, updatedAtMillis = 100))

        assertTrue(result.none { it.isAchieved })
    }
}
