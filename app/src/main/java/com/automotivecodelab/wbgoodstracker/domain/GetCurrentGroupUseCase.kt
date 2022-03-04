package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class GetCurrentGroupUseCase(
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(): String {
        return itemsRepository.getCurrentGroup()
    }
}
