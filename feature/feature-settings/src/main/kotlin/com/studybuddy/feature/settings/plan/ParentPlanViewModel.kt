package com.studybuddy.feature.settings.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.LearningMode
import com.studybuddy.core.domain.model.PlanDayResult
import com.studybuddy.core.domain.model.PlanTask
import com.studybuddy.core.domain.repository.ParentPlanRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.usecase.plan.GetPlanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

data class ParentPlanState(
    /** ISO weekday, Monday = 1. */
    val selectedDay: Int = 1,
    val tasks: List<PlanTask> = emptyList(),
    val history: List<PlanDayResult> = emptyList(),
    val completionBonus: Int = PointValues.DEFAULT_PLAN_COMPLETION_BONUS,
    val isLoading: Boolean = true,
) {
    /** How many sessions of [mode] are set for the selected day; 0 means "not asked for". */
    fun countFor(mode: LearningMode): Int =
        tasks.firstOrNull { it.dayOfWeek == selectedDay && it.mode == mode }?.targetCount ?: 0

    fun taskCountForDay(day: Int): Int = tasks.count { it.dayOfWeek == day }
}

sealed interface ParentPlanIntent {
    data class SelectDay(val dayOfWeek: Int) : ParentPlanIntent

    /**
     * A *delta*, not a target. Two quick taps must mean +2, and the rendered count
     * they were computed from is already stale by the time either write lands.
     */
    data class ChangeCount(
        val mode: LearningMode,
        val delta: Int,
    ) : ParentPlanIntent

    data class SetCompletionBonus(val points: Int) : ParentPlanIntent

    /** Copies the selected day onto the other six — seven-day setup is otherwise tedious. */
    data object CopyDayToAll : ParentPlanIntent

    data object ClearDay : ParentPlanIntent
}

@HiltViewModel
class ParentPlanViewModel @Inject constructor(
    private val planRepository: ParentPlanRepository,
    private val settingsRepository: SettingsRepository,
    private val getPlanHistory: GetPlanHistoryUseCase,
    private val clock: Clock = Clock.System,
) : ViewModel() {

    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    /** Serialises read-modify-write on the plan; taps arrive faster than Room re-emits. */
    private val writes = Mutex()

    private val _state = MutableStateFlow(
        ParentPlanState(
            selectedDay = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.isoDayNumber,
        ),
    )
    val state: StateFlow<ParentPlanState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                planRepository.getPlan(profileId),
                settingsRepository.getPlanCompletionBonus(),
                getPlanHistory(profileId),
            ) { tasks, bonus, history ->
                Triple(tasks, bonus, history)
            }.collect { (tasks, bonus, history) ->
                _state.update {
                    it.copy(tasks = tasks, completionBonus = bonus, history = history, isLoading = false)
                }
            }
        }
    }

    fun onIntent(intent: ParentPlanIntent) {
        when (intent) {
            is ParentPlanIntent.SelectDay -> _state.update { it.copy(selectedDay = intent.dayOfWeek) }
            is ParentPlanIntent.ChangeCount -> changeCount(intent.mode, intent.delta)
            is ParentPlanIntent.SetCompletionBonus -> viewModelScope.launch {
                settingsRepository.setPlanCompletionBonus(intent.points)
            }
            ParentPlanIntent.CopyDayToAll -> copyDayToAll()
            ParentPlanIntent.ClearDay -> clearDay()
        }
    }

    private fun changeCount(
        mode: LearningMode,
        delta: Int,
    ) {
        val day = _state.value.selectedDay
        viewModelScope.launch {
            writes.withLock {
                val current = planRepository.findTask(planTaskId(day, mode))?.targetCount ?: 0
                writeCount(day, mode, current + delta)
            }
        }
    }

    /**
     * A count of zero means "not asked for", so the row is deleted rather than stored
     * as 0. The id is deterministic, so a repeated tap upserts the same row instead of
     * inserting a second one.
     */
    private suspend fun writeCount(
        day: Int,
        mode: LearningMode,
        count: Int,
    ) {
        val clamped = count.coerceIn(0, MAX_SESSIONS_PER_MODE)
        val id = planTaskId(day, mode)

        if (clamped == 0) {
            planRepository.deleteTask(id)
            return
        }
        planRepository.upsertTask(
            PlanTask(
                id = id,
                profileId = profileId,
                dayOfWeek = day,
                mode = mode,
                targetCount = clamped,
                updatedAt = clock.now(),
            ),
        )
    }

    private fun copyDayToAll() {
        val source = _state.value.selectedDay
        val counts = LearningMode.entries.associateWith { _state.value.countFor(it) }
        viewModelScope.launch {
            writes.withLock {
                for (day in ALL_DAYS) {
                    if (day == source) continue
                    counts.forEach { (mode, count) -> writeCount(day, mode, count) }
                }
            }
        }
    }

    private fun clearDay() {
        val day = _state.value.selectedDay
        viewModelScope.launch {
            writes.withLock {
                LearningMode.entries.forEach { planRepository.deleteTask(planTaskId(day, it)) }
            }
        }
    }

    private fun planTaskId(
        day: Int,
        mode: LearningMode,
    ) = "$profileId:$day:${mode.name}"

    companion object {
        /** Enough for any realistic evening; the cap stops a slipped finger setting 99. */
        const val MAX_SESSIONS_PER_MODE = 9
        val ALL_DAYS = 1..7
    }
}
