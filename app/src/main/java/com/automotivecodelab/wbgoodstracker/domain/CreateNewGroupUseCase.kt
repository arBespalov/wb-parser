package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class CreateNewGroupUseCase(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(groupName: String) {
        itemsRepository.createNewGroup(groupName)
    }
}