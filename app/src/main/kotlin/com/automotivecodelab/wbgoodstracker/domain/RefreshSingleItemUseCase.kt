package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import javax.inject.Inject

class RefreshSingleItemUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(itemId: String): Result<Unit> {
        return itemsRepository.refreshSingleItem(itemId)
    }
}
