package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class SetSortingModeUseCase(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(sortingMode: SortingMode) {
        itemsRepository.setSortingMode(sortingMode)
    }
}