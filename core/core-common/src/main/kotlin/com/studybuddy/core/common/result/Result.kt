package com.studybuddy.core.common.result

/**
 * A generic class that holds a loading, success, or error state.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}

/**
 * Returns the data if this is a Success, or null otherwise.
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * Returns true if this is a Success.
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * Returns true if this is Loading.
 */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading
