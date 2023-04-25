package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.MergeStatus
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignInDebugUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val scope: CoroutineScope
) {
    operator fun invoke(userId: String) {
        scope.launch {
            if (itemsRepository.mergeStatus.first() == MergeStatus.InProgress)
                error("trying to start merge while it is already started")
            itemsRepository.setMergeStatus(MergeStatus.InProgress)
            itemsRepository.mergeItemsDebug(userId)
                .onSuccess { itemsRepository.setMergeStatus(MergeStatus.Success) }
                .onFailure { itemsRepository.setMergeStatus(MergeStatus.Error(it)) }
        }
    }
}