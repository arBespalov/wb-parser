package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class GetUserSortingModeComparatorUseCase(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(): Comparator<Item> {
        return itemsRepository.getSortingModeComparator()
    }
}
