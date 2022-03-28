package com.automotivecodelab.wbgoodstracker.domain

import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.flow.Flow

class ObserveItemsByGroupUseCase(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(): Flow<Pair<List<Item>, String?>> {
        return itemsRepository.observeItems()
    }
}