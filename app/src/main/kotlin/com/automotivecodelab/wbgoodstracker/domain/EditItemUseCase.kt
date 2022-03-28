package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class EditItemUseCase(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(item: Item) {
        itemsRepository.updateItem(item)
    }
}
