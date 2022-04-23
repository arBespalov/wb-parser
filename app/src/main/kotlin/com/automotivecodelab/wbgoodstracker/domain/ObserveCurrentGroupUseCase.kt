package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCurrentGroupUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(): Flow<String?> {
        return itemsRepository.observeCurrentGroup()
    }
}
