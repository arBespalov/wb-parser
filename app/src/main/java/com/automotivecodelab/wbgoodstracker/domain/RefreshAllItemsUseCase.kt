package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import com.automotivecodelab.wbgoodstracker.domain.util.Result

class RefreshAllItemsUseCase(
    private val userRepository: UserRepository,
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(onAuthenticationFailureCallback: () -> Unit = {}): Result<Unit> {
        return when (val authenticationResult = userRepository.getUser()) {
            is Result.Error -> {
                onAuthenticationFailureCallback.invoke()
                itemsRepository.refreshAllItems()
            }
            is Result.Success -> {
                if (authenticationResult.data == null) {
                    itemsRepository.refreshAllItems()
                } else {
                    itemsRepository.syncItems(authenticationResult.data.idToken)
                }
            }
        }
    }
}
