package com.automotivecodelab.wbgoodstracker.domain

import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class ObserveSingleItemUseCase(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(itemId: String): LiveData<Item> {
        return itemsRepository.observeSingleItem(itemId)
    }
}
