package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.ReadingDao
import com.studybuddy.core.data.db.entity.ReadingPassageEntity
import com.studybuddy.core.data.db.entity.ReadingQuestionEntity
import com.studybuddy.core.data.db.entity.ReadingResultEntity
import com.studybuddy.core.data.network.ReadingContentLoader
import com.studybuddy.core.domain.model.ReadingDictionaryEntry
import com.studybuddy.core.domain.model.ReadingPassage
import com.studybuddy.core.domain.model.ReadingQuestion
import com.studybuddy.core.domain.model.ReadingQuestionType
import com.studybuddy.core.domain.model.ReadingResult
import com.studybuddy.core.domain.model.ReadingTheme
import com.studybuddy.core.domain.repository.ReadingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private const val TIER_UNLOCK_COUNT = 3
private const val TIER_UNLOCK_ACCURACY = 0.7f

@Singleton
class LocalReadingRepository @Inject constructor(
    private val readingDao: ReadingDao,
    private val contentLoader: ReadingContentLoader,
) : ReadingRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val dictionaryCache = mutableMapOf<String, List<ReadingDictionaryEntry>>()

    override fun getPassagesByLanguage(language: String): Flow<List<ReadingPassage>> =
        readingDao.getPassagesByLanguage(language.uppercase()).map { entities ->
            entities.map { entity ->
                val questions = readingDao.getQuestionsForPassage(entity.id)
                val bestResult = readingDao.getBestResultForPassage(entity.id)
                entity.toDomain(questions, bestResult)
            }
        }

    override suspend fun getPassageById(id: String): ReadingPassage? {
        val entity = readingDao.getPassageById(id) ?: return null
        val questions = readingDao.getQuestionsForPassage(id)
        val bestResult = readingDao.getBestResultForPassage(id)
        return entity.toDomain(questions, bestResult)
    }

    override suspend fun saveResult(result: ReadingResult) {
        readingDao.insertResult(result.toEntity())
    }

    override suspend fun getBestResult(passageId: String): ReadingResult? =
        readingDao.getBestResultForPassage(passageId)?.toDomain()

    override fun getAllResults(): Flow<List<ReadingResult>> =
        readingDao.getAllResults().map { entities -> entities.map { it.toDomain() } }

    override suspend fun isNextTierUnlocked(
        currentTier: Int,
        language: String,
    ): Boolean {
        if (currentTier <= 1) return true
        val previousTier = currentTier - 1
        val passedCount = readingDao.getPassedPassageIds(
            tier = previousTier,
            language = language.uppercase(),
            minAccuracy = TIER_UNLOCK_ACCURACY,
        ).size
        return passedCount >= TIER_UNLOCK_COUNT
    }

    override suspend fun loadContentIfNeeded(language: String) {
        val existingCount = readingDao.getPassageCount(language.uppercase())
        if (existingCount > 0) return

        val content = contentLoader.loadPassages(language)
        val passageEntities = content.passages.map { passage ->
            ReadingPassageEntity(
                id = passage.id,
                language = passage.language,
                tier = passage.tier,
                theme = passage.theme,
                title = passage.title,
                passage = passage.passage,
                wordCount = passage.wordCount,
                source = passage.source,
                sourceAttribution = passage.sourceAttribution,
            )
        }
        val questionEntities = content.passages.flatMap { passage ->
            passage.questions.mapIndexed { index, question ->
                ReadingQuestionEntity(
                    id = "${passage.id}_q${index + 1}",
                    passageId = passage.id,
                    questionIndex = index,
                    type = question.type,
                    questionText = question.questionText,
                    options = question.options?.let { json.encodeToString(ListSerializer(String.serializer()), it) },
                    correctAnswer = question.correctAnswer,
                    explanation = question.explanation,
                    evidenceSentenceIndex = question.evidenceSentenceIndex,
                )
            }
        }
        readingDao.insertPassagesWithQuestions(passageEntities, questionEntities)
    }

    override fun getDictionaryEntries(language: String): List<ReadingDictionaryEntry> =
        dictionaryCache.getOrPut(language.lowercase()) {
            val dictionary = contentLoader.loadDictionary(language)
            dictionary.words.map { word ->
                ReadingDictionaryEntry(
                    word = word.word,
                    language = word.language,
                    definition = word.definition,
                    translations = word.translations,
                    passageIds = word.passageIds,
                )
            }
        }

    override suspend fun sync() {
        // Cloud migration hook — no-op for local-first architecture
    }

    private fun ReadingPassageEntity.toDomain(
        questions: List<ReadingQuestionEntity>,
        bestResult: ReadingResultEntity?,
    ): ReadingPassage = ReadingPassage(
        id = id,
        language = language,
        tier = tier,
        theme = ReadingTheme.entries.firstOrNull { it.name == theme } ?: ReadingTheme.ANIMALS,
        title = title,
        passage = passage,
        wordCount = wordCount,
        source = source,
        sourceAttribution = sourceAttribution,
        questions = questions.map { it.toDomain() },
        bestScore = bestResult?.score,
        bestTotal = bestResult?.totalQuestions,
    )

    private fun ReadingQuestionEntity.toDomain(): ReadingQuestion = ReadingQuestion(
        id = id,
        passageId = passageId,
        questionIndex = questionIndex,
        type = ReadingQuestionType.entries.firstOrNull { it.name == type }
            ?: ReadingQuestionType.MULTIPLE_CHOICE,
        questionText = questionText,
        options = options?.let {
            json.decodeFromString(
                ListSerializer(String.serializer()),
                it,
            )
        },
        correctAnswer = correctAnswer,
        explanation = explanation,
        evidenceSentenceIndex = evidenceSentenceIndex,
    )

    private fun ReadingResultEntity.toDomain(): ReadingResult = ReadingResult(
        id = id,
        passageId = passageId,
        score = score,
        totalQuestions = totalQuestions,
        pointsEarned = pointsEarned,
        readingTimeMs = readingTimeMs,
        questionsTimeMs = questionsTimeMs,
        completedAt = completedAt,
        allCorrectFirstTry = allCorrectFirstTry,
    )

    private fun ReadingResult.toEntity(): ReadingResultEntity = ReadingResultEntity(
        passageId = passageId,
        score = score,
        totalQuestions = totalQuestions,
        pointsEarned = pointsEarned,
        readingTimeMs = readingTimeMs,
        questionsTimeMs = questionsTimeMs,
        completedAt = completedAt,
        allCorrectFirstTry = allCorrectFirstTry,
    )
}
