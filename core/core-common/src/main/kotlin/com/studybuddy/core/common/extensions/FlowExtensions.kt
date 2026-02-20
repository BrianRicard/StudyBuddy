package com.studybuddy.core.common.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import com.studybuddy.core.common.result.Result

/**
 * Maps a Flow<T> into a Flow<Result<T>> with Loading and Error states.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it)) }
