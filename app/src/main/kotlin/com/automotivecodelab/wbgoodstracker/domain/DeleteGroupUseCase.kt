package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import javax.inject.Inject

class DeleteGroupUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(groupName: String) {
        itemsRepository.deleteGroup(groupName)
    }
}
