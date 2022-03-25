package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class RefreshAllItemsUseCase(
    private val userRepository: UserRepository,
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(onAuthenticationFailureCallback: () -> Unit = {}): Result<Unit> {
        return if (userRepository.isUserAuthenticated()) {
            val user = userRepository.getUser()
            if (user.isFailure) {
                onAuthenticationFailureCallback.invoke()
                itemsRepository.refreshAllItems()
            } else {
                itemsRepository.syncItems(user.getOrThrow().idToken)
            }
        } else {
            itemsRepository.refreshAllItems()
        }
    }
}
