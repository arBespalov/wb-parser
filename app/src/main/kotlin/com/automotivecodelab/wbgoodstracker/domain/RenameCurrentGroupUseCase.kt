package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import javax.inject.Inject

class RenameCurrentGroupUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(newGroupName: String) {
        itemsRepository.renameCurrentGroup(newGroupName)
    }
}