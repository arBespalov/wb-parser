package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveItemsWithGroupUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(): Flow<Pair<List<Item>, String?>> {
        return itemsRepository.observeItems()
    }
}
