package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class SetCurrentGroupUseCase(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(groupName: String?) {
        itemsRepository.setCurrentGroup(groupName)
    }
}
