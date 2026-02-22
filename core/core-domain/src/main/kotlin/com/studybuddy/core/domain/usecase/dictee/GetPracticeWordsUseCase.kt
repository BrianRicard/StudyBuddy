package com.studybuddy.core.domain.usecase.dictee

import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.repository.DicteeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPracticeWordsUseCase @Inject constructor(private val repository: DicteeRepository) {
    operator fun invoke(listId: String): Flow<List<DicteeWord>> =
        repository.getWordsForList(listId).map { words ->
            words.sortedBy { word ->
                if (word.attempts == 0) {
                    0.0
                } else {
                    word.correctCount.toDouble() / word.attempts
                }
            }
        }
}
