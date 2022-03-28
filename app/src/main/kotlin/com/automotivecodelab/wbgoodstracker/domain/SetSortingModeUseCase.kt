package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository

class SetSortingModeUseCase(
    private val sortRepository: SortRepository
) {
    suspend operator fun invoke(sortingMode: SortingMode) {
        sortRepository.setSortingMode(sortingMode)
    }
}
