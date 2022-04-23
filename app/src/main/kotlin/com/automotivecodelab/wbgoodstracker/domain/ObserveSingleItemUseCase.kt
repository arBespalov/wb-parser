package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSingleItemUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(itemId: String): Flow<Item> {
        return itemsRepository.observeSingleItem(itemId)
    }
}
