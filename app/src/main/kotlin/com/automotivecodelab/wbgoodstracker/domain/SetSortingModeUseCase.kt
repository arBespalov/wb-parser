package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import javax.inject.Inject

class SetSortingModeUseCase @Inject constructor(
    private val sortRepository: SortRepository
) {
    suspend operator fun invoke(sortingMode: SortingMode) {
        sortRepository.setSortingMode(sortingMode)
    }
}
