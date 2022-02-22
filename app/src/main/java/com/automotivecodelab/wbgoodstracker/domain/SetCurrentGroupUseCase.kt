package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class SetCurrentGroupUseCase(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(groupName: String) {
        itemsRepository.setCurrentGroup(groupName)
    }
}