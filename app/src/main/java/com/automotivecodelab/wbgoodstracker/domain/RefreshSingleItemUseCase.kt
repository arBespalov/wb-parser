package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.util.Result

class RefreshSingleItemUseCase(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(item: Item): Result<Unit> {
        return itemsRepository.refreshSingleItem(item)
    }
}