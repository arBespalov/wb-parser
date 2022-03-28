package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class AddItemsToGroupUseCase(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(itemIds: List<String>, groupName: String?) {
        itemsRepository.addItemsToGroup(itemIds, groupName)
    }
}
