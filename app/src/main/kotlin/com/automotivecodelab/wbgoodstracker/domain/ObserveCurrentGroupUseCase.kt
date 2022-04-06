package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentGroupUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(): Flow<String?> {
        return itemsRepository.observeCurrentGroup()
    }
}