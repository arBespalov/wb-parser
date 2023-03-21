package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.MergeStatus
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMergeLoadingState @Inject constructor(
    private val itemsRepository: ItemsRepository
) {
    operator fun invoke(): Flow<MergeStatus> {
        return itemsRepository.mergeStatus
    }
}