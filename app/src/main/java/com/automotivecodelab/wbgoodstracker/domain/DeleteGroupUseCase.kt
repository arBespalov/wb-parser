package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class DeleteGroupUseCase(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(groupName: String) {
        itemsRepository.deleteGroup(groupName)
    }
}