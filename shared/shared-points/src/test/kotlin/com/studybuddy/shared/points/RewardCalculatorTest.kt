package com.studybuddy.shared.points

import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.InputMode
import com.studybuddy.core.domain.model.Operator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RewardCalculatorTest {

    private val calculator = RewardCalculator()

    // --- Poem Rewards ---

    @Test
    fun `poem - perfect short poem earns around 20 points`() {
        val result = calculator.calculate(
            RewardInput.PoemReward(
                starRating = 5,
                accuracy = 0.95f,
                completeness = 1.0f,
                wordCount = 15,
                language = "fr",
            ),
        )
        assertTrue(result.totalPoints in 15..25, "Expected ~20, got ${result.totalPoints}")
    }

    @Test
    fun `poem - good medium poem earns around 18 points`() {
        val result = calculator.calculate(
            RewardInput.PoemReward(
                starRating = 4,
                accuracy = 0.8f,
                completeness = 0.85f,
                wordCount = 40,
                language = "fr",
            ),
        )
        assertTrue(result.totalPoints in 14..22, "Expected ~18, got ${result.totalPoints}")
    }

    @Test
    fun `poem - perfect long poem earns around 35 points`() {
        val result = calculator.calculate(
            RewardInput.PoemReward(
                starRating = 5,
                accuracy = 0.95f,
                completeness = 1.0f,
                wordCount = 120,
                language = "en",
            ),
        )
        assertTrue(result.totalPoints in 30..40, "Expected ~35, got ${result.totalPoints}")
    }

    @Test
    fun `poem - struggling short poem earns minimum but at least 1`() {
        val result = calculator.calculate(
            RewardInput.PoemReward(
                starRating = 1,
                accuracy = 0.1f,
                completeness = 0.3f,
                wordCount = 15,
                language = "fr",
            ),
        )
        assertTrue(result.totalPoints >= 1, "Must earn at least 1 point")
        assertTrue(result.totalPoints <= 5, "Struggling attempt should earn few points, got ${result.totalPoints}")
    }

    // --- Dictee Rewards ---

    @Test
    fun `dictee - perfect easy 5 word keyboard earns around 13 points`() {
        val result = calculator.calculate(
            RewardInput.DicteeReward(
                correctWords = 5,
                totalWords = 5,
                inputMode = InputMode.KEYBOARD,
                difficulty = Difficulty.EASY,
                averageSimilarity = 1.0f,
            ),
        )
        assertTrue(result.totalPoints in 10..16, "Expected ~13, got ${result.totalPoints}")
    }

    @Test
    fun `dictee - perfect hard 10 word handwriting earns around 51 points`() {
        val result = calculator.calculate(
            RewardInput.DicteeReward(
                correctWords = 10,
                totalWords = 10,
                inputMode = InputMode.HANDWRITING,
                difficulty = Difficulty.HARD,
                averageSimilarity = 1.0f,
            ),
        )
        assertTrue(result.totalPoints in 45..60, "Expected ~51, got ${result.totalPoints}")
    }

    @Test
    fun `dictee - struggling 5 word earns at least 1`() {
        val result = calculator.calculate(
            RewardInput.DicteeReward(
                correctWords = 1,
                totalWords = 5,
                inputMode = InputMode.KEYBOARD,
                difficulty = Difficulty.EASY,
                averageSimilarity = 0.3f,
            ),
        )
        assertTrue(result.totalPoints >= 1, "Must earn at least 1 point")
        assertTrue(result.totalPoints <= 5, "Struggling should earn few, got ${result.totalPoints}")
    }

    @Test
    fun `dictee - perfect medium 20 word earns around 65 points`() {
        val result = calculator.calculate(
            RewardInput.DicteeReward(
                correctWords = 20,
                totalWords = 20,
                inputMode = InputMode.LETTER_TILES,
                difficulty = Difficulty.MEDIUM,
                averageSimilarity = 1.0f,
            ),
        )
        assertTrue(result.totalPoints in 55..75, "Expected ~65, got ${result.totalPoints}")
    }

    // --- Speed Math Rewards (THE KEY FIX) ---

    @Test
    fun `speed math - easy grind exploit should not exceed 10 points`() {
        val result = calculator.calculate(
            RewardInput.SpeedMathReward(
                correctAnswers = 5,
                totalProblems = 5,
                timeLimitSeconds = 30,
                operators = setOf(Operator.PLUS),
                numberRangeMin = 1,
                numberRangeMax = 10,
                averageResponseTimeMs = 3000,
            ),
        )
        assertTrue(
            result.totalPoints <= 10,
            "Easy grind exploit must not exceed 10 pts, got ${result.totalPoints}",
        )
    }

    @Test
    fun `speed math - hard grind should reward meaningfully`() {
        val result = calculator.calculate(
            RewardInput.SpeedMathReward(
                correctAnswers = 45,
                totalProblems = 50,
                timeLimitSeconds = 30,
                operators = setOf(Operator.PLUS, Operator.MINUS, Operator.MULTIPLY, Operator.DIVIDE),
                numberRangeMin = 1,
                numberRangeMax = 100,
                averageResponseTimeMs = 2000,
            ),
        )
        assertTrue(
            result.totalPoints >= 150,
            "Hard Speed Math should reward 150+, got ${result.totalPoints}",
        )
    }

    @Test
    fun `speed math - easy casual earns around 13 points`() {
        val result = calculator.calculate(
            RewardInput.SpeedMathReward(
                correctAnswers = 8,
                totalProblems = 10,
                timeLimitSeconds = 60,
                operators = setOf(Operator.PLUS, Operator.MINUS),
                numberRangeMin = 1,
                numberRangeMax = 20,
                averageResponseTimeMs = 5000,
            ),
        )
        assertTrue(result.totalPoints in 8..18, "Expected ~13, got ${result.totalPoints}")
    }

    @Test
    fun `speed math - no time limit has lowest difficulty multiplier`() {
        val withTimer = calculator.calculate(
            RewardInput.SpeedMathReward(
                correctAnswers = 10,
                totalProblems = 10,
                timeLimitSeconds = 60,
                operators = setOf(Operator.PLUS),
                numberRangeMin = 1,
                numberRangeMax = 20,
                averageResponseTimeMs = 3000,
            ),
        )
        val noTimer = calculator.calculate(
            RewardInput.SpeedMathReward(
                correctAnswers = 10,
                totalProblems = 10,
                timeLimitSeconds = 0,
                operators = setOf(Operator.PLUS),
                numberRangeMin = 1,
                numberRangeMax = 20,
                averageResponseTimeMs = 3000,
            ),
        )
        assertTrue(
            withTimer.totalPoints > noTimer.totalPoints,
            "Timer should reward more than no timer",
        )
    }

    @Test
    fun `speed math - more operators give higher multiplier`() {
        val oneOp = calculator.calculate(
            RewardInput.SpeedMathReward(
                correctAnswers = 10,
                totalProblems = 10,
                timeLimitSeconds = 60,
                operators = setOf(Operator.PLUS),
                numberRangeMin = 1,
                numberRangeMax = 20,
                averageResponseTimeMs = 3000,
            ),
        )
        val fourOps = calculator.calculate(
            RewardInput.SpeedMathReward(
                correctAnswers = 10,
                totalProblems = 10,
                timeLimitSeconds = 60,
                operators = setOf(Operator.PLUS, Operator.MINUS, Operator.MULTIPLY, Operator.DIVIDE),
                numberRangeMin = 1,
                numberRangeMax = 20,
                averageResponseTimeMs = 3000,
            ),
        )
        assertTrue(
            fourOps.totalPoints > oneOp.totalPoints,
            "More operators should reward more: 4ops=${fourOps.totalPoints}, 1op=${oneOp.totalPoints}",
        )
    }

    @Test
    fun `speed math - minimum 1 point for any completed session`() {
        val result = calculator.calculate(
            RewardInput.SpeedMathReward(
                correctAnswers = 0,
                totalProblems = 5,
                timeLimitSeconds = 30,
                operators = setOf(Operator.PLUS),
                numberRangeMin = 1,
                numberRangeMax = 10,
                averageResponseTimeMs = 0,
            ),
        )
        assertTrue(result.totalPoints >= 1, "Must earn at least 1 point")
    }

    // --- Math Challenge Rewards ---

    @Test
    fun `challenge - quick loss earns minimum`() {
        val result = calculator.calculate(
            RewardInput.MathChallengeReward(
                score = 300,
                solvedCount = 3,
                timeSurvivedMs = 30_000,
                highestLevel = 1,
                longestStreak = 2,
            ),
        )
        assertTrue(result.totalPoints in 1..5, "Quick loss: expected 1-5, got ${result.totalPoints}")
    }

    @Test
    fun `challenge - decent run earns around 23 points`() {
        val result = calculator.calculate(
            RewardInput.MathChallengeReward(
                score = 1500,
                solvedCount = 15,
                timeSurvivedMs = 120_000,
                highestLevel = 4,
                longestStreak = 7,
            ),
        )
        assertTrue(result.totalPoints in 15..35, "Decent run: expected ~23, got ${result.totalPoints}")
    }

    @Test
    fun `challenge - great run earns around 57 points`() {
        val result = calculator.calculate(
            RewardInput.MathChallengeReward(
                score = 2500,
                solvedCount = 25,
                timeSurvivedMs = 180_000,
                highestLevel = 6,
                longestStreak = 12,
            ),
        )
        assertTrue(result.totalPoints in 40..70, "Great run: expected ~57, got ${result.totalPoints}")
    }

    @Test
    fun `challenge - amazing run earns around 128 points`() {
        val result = calculator.calculate(
            RewardInput.MathChallengeReward(
                score = 4000,
                solvedCount = 40,
                timeSurvivedMs = 300_000,
                highestLevel = 8,
                longestStreak = 18,
            ),
        )
        assertTrue(result.totalPoints in 90..160, "Amazing run: expected ~128, got ${result.totalPoints}")
    }

    // --- Reading Rewards ---

    @Test
    fun `reading - perfect tier 1 with 3 questions earns 15 plus first try bonus`() {
        val result = calculator.calculate(
            RewardInput.ReadingReward(
                correctAnswers = 3,
                totalQuestions = 3,
                tier = 1,
                allCorrectFirstTry = true,
            ),
        )
        // base=15 * diff=1.0 * acc=1.0 + bonus=5 = 20
        assertEquals(20, result.totalPoints)
    }

    @Test
    fun `reading - perfect tier 2 with 4 questions earns 30 plus first try bonus`() {
        val result = calculator.calculate(
            RewardInput.ReadingReward(
                correctAnswers = 4,
                totalQuestions = 4,
                tier = 2,
                allCorrectFirstTry = true,
            ),
        )
        // base=20 * diff=1.5 * acc=1.0 + bonus=5 = 35
        assertEquals(35, result.totalPoints)
    }

    @Test
    fun `reading - perfect tier 3 with 5 questions earns 60 plus first try bonus`() {
        val result = calculator.calculate(
            RewardInput.ReadingReward(
                correctAnswers = 5,
                totalQuestions = 5,
                tier = 3,
                allCorrectFirstTry = true,
            ),
        )
        // base=30 * diff=2.0 * acc=1.0 + bonus=5 = 65
        assertEquals(65, result.totalPoints)
    }

    @Test
    fun `reading - partial score no first try bonus`() {
        val result = calculator.calculate(
            RewardInput.ReadingReward(
                correctAnswers = 2,
                totalQuestions = 4,
                tier = 1,
                allCorrectFirstTry = false,
            ),
        )
        // base=20 * diff=1.0 * acc=0.5 + bonus=0 = 10
        assertEquals(10, result.totalPoints)
    }

    @Test
    fun `reading - zero correct earns minimum 1 point`() {
        val result = calculator.calculate(
            RewardInput.ReadingReward(
                correctAnswers = 0,
                totalQuestions = 3,
                tier = 1,
                allCorrectFirstTry = false,
            ),
        )
        assertEquals(1, result.totalPoints)
    }

    @Test
    fun `reading - higher tier gives higher reward for same accuracy`() {
        val tier1 = calculator.calculate(
            RewardInput.ReadingReward(3, 3, 1, false),
        )
        val tier3 = calculator.calculate(
            RewardInput.ReadingReward(3, 3, 3, false),
        )
        assertTrue(
            tier3.totalPoints > tier1.totalPoints,
            "Tier 3 should reward more: tier3=${tier3.totalPoints}, tier1=${tier1.totalPoints}",
        )
    }

    @Test
    fun `reading - first try bonus only when all correct first try`() {
        val withBonus = calculator.calculate(
            RewardInput.ReadingReward(3, 3, 1, allCorrectFirstTry = true),
        )
        val noBonus = calculator.calculate(
            RewardInput.ReadingReward(3, 3, 1, allCorrectFirstTry = false),
        )
        assertEquals(
            RewardCalculator.READING_FIRST_TRY_BONUS,
            withBonus.totalPoints - noBonus.totalPoints,
        )
    }

    @Test
    fun `reading - breakdown total matches totalPoints`() {
        val result = calculator.calculate(
            RewardInput.ReadingReward(4, 5, 2, false),
        )
        assertEquals(result.totalPoints, result.breakdown.total)
    }

    // --- Cross-Section Balance ---

    @Test
    fun `all sections earn similar points per 5 minutes at medium difficulty`() {
        // 2 medium poems in 5 min
        val poemPoints = calculator.calculate(
            RewardInput.PoemReward(
                starRating = 4,
                accuracy = 0.8f,
                completeness = 0.9f,
                wordCount = 40,
                language = "fr",
            ),
        ).totalPoints * 2

        // 1 list of 10 words, medium, 80% correct
        val dicteePoints = calculator.calculate(
            RewardInput.DicteeReward(
                correctWords = 8,
                totalWords = 10,
                inputMode = InputMode.KEYBOARD,
                difficulty = Difficulty.MEDIUM,
                averageSimilarity = 0.8f,
            ),
        ).totalPoints

        // 20 problems, 60s, +-, 1-20, 80% correct
        val mathPoints = calculator.calculate(
            RewardInput.SpeedMathReward(
                correctAnswers = 16,
                totalProblems = 20,
                timeLimitSeconds = 60,
                operators = setOf(Operator.PLUS, Operator.MINUS),
                numberRangeMin = 1,
                numberRangeMax = 20,
                averageResponseTimeMs = 3000,
            ),
        ).totalPoints

        val maxPoints = maxOf(poemPoints, dicteePoints, mathPoints)
        val minPoints = minOf(poemPoints, dicteePoints, mathPoints)

        assertTrue(
            minPoints > 0,
            "All sections should earn points",
        )
        assertTrue(
            maxPoints.toFloat() / minPoints < 3.0f,
            "Sections should be within 3x: poems=$poemPoints, dictee=$dicteePoints, math=$mathPoints",
        )
    }

    // --- Breakdown ---

    @Test
    fun `breakdown total matches totalPoints`() {
        val result = calculator.calculate(
            RewardInput.SpeedMathReward(
                correctAnswers = 15,
                totalProblems = 20,
                timeLimitSeconds = 60,
                operators = setOf(Operator.PLUS, Operator.MINUS),
                numberRangeMin = 1,
                numberRangeMax = 50,
                averageResponseTimeMs = 4000,
            ),
        )
        assertEquals(result.totalPoints, result.breakdown.total)
    }

    @Test
    fun `all results have totalPoints at least 1`() {
        val inputs = listOf(
            RewardInput.PoemReward(1, 0.0f, 0.0f, 5, "fr"),
            RewardInput.DicteeReward(0, 5, InputMode.KEYBOARD, Difficulty.EASY, 0.0f),
            RewardInput.SpeedMathReward(0, 5, 30, setOf(Operator.PLUS), 1, 10, 0),
            RewardInput.MathChallengeReward(0, 0, 5000, 1, 0),
            RewardInput.ReadingReward(0, 3, 1, false),
        )
        inputs.forEach { input ->
            val result = calculator.calculate(input)
            assertTrue(
                result.totalPoints >= 1,
                "Minimum 1 point for any completed session: ${input::class.simpleName} gave ${result.totalPoints}",
            )
        }
    }
}
