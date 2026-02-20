package com.studybuddy.core.domain.usecase.dictee

import com.studybuddy.core.domain.model.DicteeList
import com.studybuddy.core.domain.repository.DicteeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDicteeListsUseCase @Inject constructor(
    private val repository: DicteeRepository,
) {
    operator fun invoke(profileId: String): Flow<List<DicteeList>> =
        repository.getListsForProfile(profileId)
}
