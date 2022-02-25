package com.automotivecodelab.wbgoodstracker.domain

import androidx.lifecycle.LiveData
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class ObserveItemsByGroupUseCase(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(groupName: String): LiveData<List<Item>> {
        return itemsRepository.observeItems(groupName)
    }
}
