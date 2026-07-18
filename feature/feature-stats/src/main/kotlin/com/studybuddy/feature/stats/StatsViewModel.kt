package com.studybuddy.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.MathSession
import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.conjugation.AtelierMilestone
import com.studybuddy.core.domain.model.conjugation.AtelierMilestoneStatus
import com.studybuddy.core.domain.model.conjugation.ConjugationPathStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import com.studybuddy.core.domain.model.conjugation.MilestoneStatus
import com.studybuddy.core.domain.repository.AtelierReviewRepository
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.repository.MathRepository
import com.studybuddy.core.domain.repository.PointsRepository
import com.studybuddy.core.domain.usecase.conjugation.GetAtelierMilestonesUseCase
import com.studybuddy.core.domain.usecase.conjugation.GetConjugationMilestonesUseCase
import com.studybuddy.core.domain.usecase.conjugation.GetConjugationPathUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Represents a single day's data in the weekly chart.
 */
data class DayData(val dayOfWeek: String, val points: Int, val isToday: Boolean)

/**
 * UI state for the Stats / Progress screen.
 */
data class StatsState(
    val totalStars: Long = 0L,
    val dayStreak: Int = 0,
    val totalSessions: Int = 0,
    val weeklyData: List<DayData> = emptyList(),
    val dicteeAccuracy: Float? = null,
    val dicteeAccuracyTrend: Float? = null,
    val mathAvgSpeed: Long? = null,
    val mathSpeedTrend: Long? = null,
    val verbsMastered: Int = 0,
    val verbsTotal: Int = ConjugationStages.all.size,
    val conjugationGamesDone: Int = 0,
    val milestones: List<MilestoneStatus> = emptyList(),
    val atelierVerbsMastered: Int = 0,
    val atelierVerbsTotal: Int = FrenchVerbs.all.size,
    val atelierCardsDue: Int = 0,
    val atelierMilestones: List<AtelierMilestoneStatus> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val pointsRepository: PointsRepository,
    private val mathRepository: MathRepository,
    private val dicteeRepository: DicteeRepository,
    private val getConjugationPath: GetConjugationPathUseCase,
    private val getConjugationMilestones: GetConjugationMilestonesUseCase,
    private val atelierReviewRepository: AtelierReviewRepository,
    private val getAtelierMilestones: GetAtelierMilestonesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init {
        observeStats()
    }

    private fun observeStats() {
        viewModelScope.launch {
            // combine() has typed overloads up to 5 flows, so the quest stats are
            // built first and the Atelier review flow is folded in afterwards.
            val questStats = combine(
                pointsRepository.getTotalPoints(AppConstants.DEFAULT_PROFILE_ID),
                pointsRepository.getPointsForProfile(AppConstants.DEFAULT_PROFILE_ID),
                mathRepository.getSessionsForProfile(AppConstants.DEFAULT_PROFILE_ID),
                dicteeRepository.getListsForProfile(AppConstants.DEFAULT_PROFILE_ID),
                getConjugationPath(AppConstants.DEFAULT_PROFILE_ID),
            ) { totalPoints, pointEvents, mathSessions, dicteeLists, conjugationPath ->
                StatsState(
                    totalStars = totalPoints,
                    dayStreak = calculateDayStreak(pointEvents),
                    totalSessions = countTotalSessions(pointEvents),
                    weeklyData = buildWeeklyData(pointEvents),
                    dicteeAccuracy = calculateDicteeAccuracy(dicteeLists),
                    dicteeAccuracyTrend = calculateDicteeAccuracyTrend(dicteeLists),
                    mathAvgSpeed = calculateMathAvgSpeed(mathSessions),
                    mathSpeedTrend = calculateMathSpeedTrend(mathSessions),
                    verbsMastered = conjugationPath.count { it.isCompleted },
                    conjugationGamesDone = countConjugationGames(conjugationPath),
                    milestones = getConjugationMilestones(conjugationPath),
                    isLoading = false,
                )
            }

            combine(
                questStats,
                atelierReviewRepository.getReviews(AppConstants.DEFAULT_PROFILE_ID),
            ) { state, atelierReviews ->
                val now = Clock.System.now()
                val milestones = getAtelierMilestones(atelierReviews)
                state.copy(
                    atelierVerbsMastered = milestones
                        .single { it.milestone == AtelierMilestone.ALL_VERBS_MASTERED }.current,
                    atelierCardsDue = atelierReviews.count { it.dueAt <= now },
                    atelierMilestones = milestones,
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    /**
     * Calculates the current day streak by counting consecutive days
     * (going backwards from today) that have at least one point event.
     */
    internal fun calculateDayStreak(events: List<PointEvent>): Int {
        if (events.isEmpty()) return 0

        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date

        val daysWithEvents = events
            .map { it.timestamp.toLocalDateTime(timeZone).date }
            .toSet()
            .sortedDescending()

        if (daysWithEvents.isEmpty()) return 0

        var streak = 0
        var checkDate = today

        // If today has no events, start checking from yesterday
        if (checkDate !in daysWithEvents) {
            val yesterday = today.minus(DatePeriod(days = 1))
            if (yesterday !in daysWithEvents) return 0
            checkDate = yesterday
        }

        while (checkDate in daysWithEvents) {
            streak++
            checkDate = checkDate.minus(DatePeriod(days = 1))
        }

        return streak
    }

    /**
     * Counts total study sessions by counting point events from learning sources.
     */
    private fun countTotalSessions(events: List<PointEvent>): Int = events.count {
        it.source == PointSource.DICTEE ||
            it.source == PointSource.MATH ||
            it.source == PointSource.CONJUGATION
    }

    /** Counts completed quest games (steps) across all stages. */
    private fun countConjugationGames(path: List<ConjugationPathStage>): Int = path.sumOf { it.completedStepCount }

    /**
     * Builds weekly data for the chart by grouping point events into each day
     * of the current week (Monday through Sunday).
     */
    internal fun buildWeeklyData(events: List<PointEvent>): List<DayData> {
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val todayDayOfWeek = today.dayOfWeek

        // Calculate the Monday of the current week (Monday = isoDayNumber 1)
        val daysFromMonday = todayDayOfWeek.isoDayNumber - 1
        val mondayOfWeek = today.minus(DatePeriod(days = daysFromMonday))

        val pointsByDate = events.groupBy { event ->
            event.timestamp.toLocalDateTime(timeZone).date
        }.mapValues { (_, dayEvents) ->
            dayEvents.sumOf { it.points }
        }

        return DayOfWeek.entries.map { dayOfWeek ->
            val daysOffset = dayOfWeek.isoDayNumber - 1
            val date = mondayOfWeek.plus(
                DatePeriod(days = daysOffset),
            )
            val javaDow = java.time.DayOfWeek.of(dayOfWeek.isoDayNumber)
            DayData(
                dayOfWeek = javaDow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                points = pointsByDate[date] ?: 0,
                isToday = date == today,
            )
        }
    }

    /**
     * Calculates overall dictee accuracy from mastered vs total word counts across all lists.
     * Returns null if there are no words to measure.
     */
    private fun calculateDicteeAccuracy(lists: List<com.studybuddy.core.domain.model.DicteeList>): Float? {
        val totalWords = lists.sumOf { it.wordCount }
        if (totalWords == 0) return null
        val totalMastered = lists.sumOf { it.masteredCount }
        return totalMastered.toFloat() / totalWords.toFloat()
    }

    /**
     * Calculates dictee accuracy trend by comparing the most recent half of lists
     * against the older half. A positive value indicates improvement.
     * Returns null if there is insufficient data.
     */
    private fun calculateDicteeAccuracyTrend(lists: List<com.studybuddy.core.domain.model.DicteeList>): Float? {
        if (lists.size < MIN_LISTS_FOR_TREND) return null

        val sortedLists = lists.sortedBy { it.updatedAt }
        val midpoint = sortedLists.size / 2
        val olderLists = sortedLists.subList(0, midpoint)
        val newerLists = sortedLists.subList(midpoint, sortedLists.size)

        val olderTotal = olderLists.sumOf { it.wordCount }
        val newerTotal = newerLists.sumOf { it.wordCount }

        if (olderTotal == 0 || newerTotal == 0) return null

        val olderAccuracy = olderLists.sumOf { it.masteredCount }.toFloat() / olderTotal
        val newerAccuracy = newerLists.sumOf { it.masteredCount }.toFloat() / newerTotal

        return newerAccuracy - olderAccuracy
    }

    /**
     * Calculates the average response time (in ms) from the last 10 math sessions.
     * Returns null if there are no sessions.
     */
    private fun calculateMathAvgSpeed(sessions: List<MathSession>): Long? {
        if (sessions.isEmpty()) return null
        val recentSessions = sessions
            .sortedByDescending { it.completedAt }
            .take(TREND_SESSION_COUNT)
        return recentSessions.map { it.avgResponseMs }.average().toLong()
    }

    /**
     * Calculates the math speed trend by comparing the last 10 sessions' average speed
     * against the previous 10 sessions. A negative value indicates improvement (faster).
     * Returns null if there is insufficient data.
     */
    private fun calculateMathSpeedTrend(sessions: List<MathSession>): Long? {
        if (sessions.size < MIN_SESSIONS_FOR_TREND) return null

        val sorted = sessions.sortedByDescending { it.completedAt }
        val recentSessions = sorted.take(TREND_SESSION_COUNT)
        val olderSessions = sorted.drop(TREND_SESSION_COUNT).take(TREND_SESSION_COUNT)

        if (olderSessions.isEmpty()) return null

        val recentAvg = recentSessions.map { it.avgResponseMs }.average().toLong()
        val olderAvg = olderSessions.map { it.avgResponseMs }.average().toLong()

        return recentAvg - olderAvg
    }

    companion object {
        private const val TREND_SESSION_COUNT = 10
        private const val MIN_SESSIONS_FOR_TREND = 4
        private const val MIN_LISTS_FOR_TREND = 2
    }
}
