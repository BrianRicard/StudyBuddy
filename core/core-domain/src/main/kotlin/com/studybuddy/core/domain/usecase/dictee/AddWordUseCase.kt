package com.studybuddy.core.domain.usecase.dictee

import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.repository.DicteeRepository
import javax.inject.Inject

class AddWordUseCase @Inject constructor(
    private val repository: DicteeRepository,
) {
    suspend operator fun invoke(word: DicteeWord) {
        repository.addWord(word)
    }
}
