package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class RenameCurrentGroupUseCase(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(newGroupName: String) {
        itemsRepository.renameCurrentGroup(newGroupName)
    }
}