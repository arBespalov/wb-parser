package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository

class GetGroupsUseCase(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(): Array<String> {
        return itemsRepository.getGroups()
    }
}