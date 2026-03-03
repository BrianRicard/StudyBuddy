package com.studybuddy.feature.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.PointEvent
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.repository.AvatarRepository
import com.studybuddy.core.domain.repository.PointsRepository
import com.studybuddy.core.domain.repository.ProfileRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.ui.R as CoreUiR
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Represents a time-ago value that can be resolved to a localized string in the UI.
 */
sealed interface TimeAgo {
    /** Less than 1 minute ago. */
    data object JustNow : TimeAgo

    /** [minutes] minutes ago. */
    data class Minutes(val minutes: Long) : TimeAgo

    /** [hours] hours ago. */
    data class Hours(val hours: Long) : TimeAgo

    /** Yesterday. */
    data object Yesterday : TimeAgo

    /** [days] days ago. */
    data class Days(val days: Long) : TimeAgo
}

/**
 * Represents a recent activity entry on the home screen.
 */
data class RecentActivity(
    @StringRes val modeResId: Int,
    val source: PointSource,
    val points: Int,
    val reason: String,
    val timeAgo: TimeAgo,
)

/**
 * UI state for the Home screen.
 */
data class HomeState(
    val profileName: String = "",
    val avatarConfig: AvatarConfig = AvatarConfig.default(),
    val totalStars: Long = 0L,
    val locale: String = "fr",
    @StringRes val greetingResId: Int = CoreUiR.string.greeting_morning,
    val dayStreak: Int = 0,
    val weekDots: List<Boolean> = List(WEEK_DAYS) { false },
    val sessionsToday: Int = 0,
    val dailyGoal: Int = 5,
    val recentActivities: List<RecentActivity> = emptyList(),
    val isLoading: Boolean = true,
) {
    val dailyProgress: Float
        get() = if (dailyGoal > 0) {
            (sessionsToday.toFloat() / dailyGoal).coerceAtMost(1f)
        } else {
            0f
        }

    val isDailyGoalReached: Boolean
        get() = sessionsToday >= dailyGoal
}

/**
 * User actions dispatched to the Home ViewModel.
 */
sealed interface HomeIntent {
    data object NavigateToDictee : HomeIntent
    data object NavigateToMath : HomeIntent
    data object NavigateToMathChallenge : HomeIntent
    data object NavigateToPoems : HomeIntent
    data object NavigateToAvatar : HomeIntent
    data object NavigateToStats : HomeIntent
    data object NavigateToRewards : HomeIntent
    data object NavigateToSettings : HomeIntent
}

/**
 * One-shot side effects emitted by the Home ViewModel.
 */
sealed interface HomeEffect {
    data object OpenDictee : HomeEffect
    data object OpenMath : HomeEffect
    data object OpenMathChallenge : HomeEffect
    data object OpenPoems : HomeEffect
    data object OpenAvatar : HomeEffect
    data object OpenStats : HomeEffect
    data object OpenRewards : HomeEffect
    data object OpenSettings : HomeEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val avatarRepository: AvatarRepository,
    private val pointsRepository: PointsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HomeEffect>()
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

    init {
        observeProfile()
        observeSettings()
    }

    fun onIntent(intent: HomeIntent) {
        viewModelScope.launch {
            when (intent) {
                HomeIntent.NavigateToDictee -> _effects.emit(HomeEffect.OpenDictee)
                HomeIntent.NavigateToMath -> _effects.emit(HomeEffect.OpenMath)
                HomeIntent.NavigateToMathChallenge -> _effects.emit(HomeEffect.OpenMathChallenge)
                HomeIntent.NavigateToPoems -> _effects.emit(HomeEffect.OpenPoems)
                HomeIntent.NavigateToAvatar -> _effects.emit(HomeEffect.OpenAvatar)
                HomeIntent.NavigateToStats -> _effects.emit(HomeEffect.OpenStats)
                HomeIntent.NavigateToRewards -> _effects.emit(HomeEffect.OpenRewards)
                HomeIntent.NavigateToSettings -> _effects.emit(HomeEffect.OpenSettings)
            }
        }
    }

    @Suppress("OPT_IN_USAGE")
    private fun observeProfile() {
        viewModelScope.launch {
            profileRepository.getActiveProfile()
                .filterNotNull()
                .flatMapLatest { profile ->
                    combine(
                        avatarRepository.getAvatarConfig(profile.id),
                        pointsRepository.getTotalPoints(profile.id),
                        pointsRepository.getPointsForProfile(profile.id),
                        pointsRepository.getSessionsToday(profile.id),
                    ) { avatarConfig, totalPoints, pointEvents, sessionsToday ->
                        val timeZone = TimeZone.currentSystemDefault()
                        val streak = calculateDayStreak(pointEvents, timeZone)
                        val weekDots = calculateWeekDots(pointEvents, timeZone)
                        val recentActivities = buildRecentActivities(pointEvents)

                        _state.value.copy(
                            profileName = profile.name,
                            avatarConfig = avatarConfig ?: AvatarConfig.default(),
                            totalStars = totalPoints,
                            dayStreak = streak,
                            weekDots = weekDots,
                            sessionsToday = sessionsToday,
                            recentActivities = recentActivities,
                            isLoading = false,
                        )
                    }
                }
                .collect { newState -> _state.value = newState }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.getAppLocale(),
                settingsRepository.getDailyGoal(),
            ) { locale, dailyGoal ->
                Pair(locale, dailyGoal)
            }.collect { (locale, dailyGoal) ->
                val greetingResId = buildGreetingResId()
                _state.update {
                    it.copy(
                        locale = locale,
                        dailyGoal = dailyGoal,
                        greetingResId = greetingResId,
                    )
                }
            }
        }
    }

    @StringRes
    internal fun buildGreetingResId(): Int {
        val hour = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .hour

        return when {
            hour < AFTERNOON_HOUR -> CoreUiR.string.greeting_morning
            hour < EVENING_HOUR -> CoreUiR.string.greeting_afternoon
            else -> CoreUiR.string.greeting_evening
        }
    }

    internal fun calculateDayStreak(
        events: List<PointEvent>,
        timeZone: TimeZone,
    ): Int {
        if (events.isEmpty()) return 0

        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val daysWithEvents = events
            .map { it.timestamp.toLocalDateTime(timeZone).date }
            .toSet()
            .sortedDescending()

        if (daysWithEvents.isEmpty()) return 0

        var streak = 0
        var checkDate = today

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

    private fun calculateWeekDots(
        events: List<PointEvent>,
        timeZone: TimeZone,
    ): List<Boolean> {
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val daysFromMonday = today.dayOfWeek.ordinal
        val mondayOfWeek = today.minus(DatePeriod(days = daysFromMonday))

        val daysWithEvents = events
            .map { it.timestamp.toLocalDateTime(timeZone).date }
            .toSet()

        return (0 until WEEK_DAYS).map { offset ->
            val date = mondayOfWeek.plus(DatePeriod(days = offset))
            date in daysWithEvents
        }
    }

    private fun buildRecentActivities(events: List<PointEvent>): List<RecentActivity> {
        val now = Clock.System.now()

        return events
            .filter { it.source == PointSource.DICTEE || it.source == PointSource.MATH }
            .sortedByDescending { it.timestamp }
            .take(MAX_RECENT_ACTIVITIES)
            .map { event ->
                @StringRes val modeResId = when (event.source) {
                    PointSource.DICTEE -> CoreUiR.string.mode_dictee
                    PointSource.MATH -> CoreUiR.string.mode_math
                    else -> CoreUiR.string.mode_activity
                }
                val elapsed = now - event.timestamp
                val timeAgo = formatTimeAgo(elapsed)

                RecentActivity(
                    modeResId = modeResId,
                    source = event.source,
                    points = event.points,
                    reason = event.reason,
                    timeAgo = timeAgo,
                )
            }
    }

    private fun formatTimeAgo(duration: kotlin.time.Duration): TimeAgo {
        val minutes = duration.inWholeMinutes
        val hours = duration.inWholeHours
        val days = duration.inWholeDays

        return when {
            minutes < 1 -> TimeAgo.JustNow
            minutes < MINUTES_IN_HOUR -> TimeAgo.Minutes(minutes)
            hours < HOURS_IN_DAY -> TimeAgo.Hours(hours)
            days < 2 -> TimeAgo.Yesterday
            else -> TimeAgo.Days(days)
        }
    }

    companion object {
        private const val AFTERNOON_HOUR = 12
        private const val EVENING_HOUR = 18
        private const val MAX_RECENT_ACTIVITIES = 5
        private const val MINUTES_IN_HOUR = 60
        private const val HOURS_IN_DAY = 24
    }
}

private const val WEEK_DAYS = 7
