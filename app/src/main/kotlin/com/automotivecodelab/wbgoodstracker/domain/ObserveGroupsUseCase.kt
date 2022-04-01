package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.flow.Flow

class ObserveGroupsUseCase(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(): Flow<List<String>> {
        return itemsRepository.getGroups()
    }
}
