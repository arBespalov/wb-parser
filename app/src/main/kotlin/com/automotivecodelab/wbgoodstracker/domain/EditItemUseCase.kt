package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import javax.inject.Inject

class EditItemUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(item: Item) {
        itemsRepository.updateItem(item)
    }
}
