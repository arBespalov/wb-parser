package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class DeleteItemsUseCase(
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        itemsIdToDelete: Array<String>,
        onAuthenticationFailureCallback: () -> Unit = {}
    ) {
        if (userRepository.isUserAuthenticated()) {
            userRepository.getUser()
                .onFailure {
                    onAuthenticationFailureCallback()
                    itemsRepository.deleteItems(itemsIdToDelete)
                }
                .onSuccess { user ->
                    itemsRepository.deleteItems(itemsIdToDelete, user.idToken)
                }
        } else {
            itemsRepository.deleteItems(itemsIdToDelete)
        }
    }
}
