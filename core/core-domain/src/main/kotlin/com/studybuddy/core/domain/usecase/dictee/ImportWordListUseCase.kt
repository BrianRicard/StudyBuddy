package com.studybuddy.core.domain.usecase.dictee

import com.studybuddy.core.domain.repository.BackupRepository
import javax.inject.Inject

class ImportWordListUseCase @Inject constructor(private val repository: BackupRepository) {
    suspend operator fun invoke(
        csvContent: String,
        profileId: String,
    ): Int = repository.importCsv(csvContent, profileId)
}
