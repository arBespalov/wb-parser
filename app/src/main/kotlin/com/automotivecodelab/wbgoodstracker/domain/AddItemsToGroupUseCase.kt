package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import javax.inject.Inject

class AddItemsToGroupUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(itemIds: List<String>, groupName: String?) {
        itemsRepository.addItemsToGroup(itemIds, groupName)
    }
}
