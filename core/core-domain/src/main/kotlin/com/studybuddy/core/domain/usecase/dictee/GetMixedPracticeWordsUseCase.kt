package com.studybuddy.core.domain.usecase.dictee

import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.repository.DicteeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Fetches words from multiple lists and merges them for a challenge session.
 * Words are sorted by mastery (lowest success rate first) so the session
 * always targets the weakest words across all selected lists.
 */
class GetMixedPracticeWordsUseCase @Inject constructor(
    private val repository: DicteeRepository,
) {
    operator fun invoke(listIds: List<String>): Flow<List<DicteeWord>> =
        repository.getWordsForLists(listIds).map { words ->
            words.sortedBy { word ->
                if (word.attempts == 0) 0.0
                else word.correctCount.toDouble() / word.attempts
            }
        }
}
