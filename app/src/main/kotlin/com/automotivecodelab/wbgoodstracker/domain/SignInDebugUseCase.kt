package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignInDebugUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val scope: CoroutineScope
) {
    operator fun invoke(userId: String) {
        scope.launch {
            itemsRepository.mergeItemsDebug(userId)
        }
    }
}