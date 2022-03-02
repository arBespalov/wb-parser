package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class RefreshAllItemsUseCase(
    private val userRepository: UserRepository,
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(onAuthenticationFailureCallback: () -> Unit = {}): Result<Unit> {
        val authenticationResult = userRepository.getUser()
        return if (authenticationResult.isFailure) {
            onAuthenticationFailureCallback.invoke()
            itemsRepository.refreshAllItems()
        } else {
            val user = authenticationResult.getOrNull()
            if (user == null) {
                itemsRepository.refreshAllItems()
            } else {
                itemsRepository.syncItems(user.idToken)
            }
        }
    }
}
