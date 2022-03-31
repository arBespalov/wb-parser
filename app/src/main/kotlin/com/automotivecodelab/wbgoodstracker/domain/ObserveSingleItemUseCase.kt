package com.automotivecodelab.wbgoodstracker.domain

import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.flow.Flow

class ObserveSingleItemUseCase(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(itemId: String): Flow<Item> {
        return itemsRepository.observeSingleItem(itemId)
    }
}