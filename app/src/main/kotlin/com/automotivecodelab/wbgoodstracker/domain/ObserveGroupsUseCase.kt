package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.ItemGroups
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveGroupsUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(): Flow<ItemGroups> {
        return itemsRepository.observeGroups()
    }
}
