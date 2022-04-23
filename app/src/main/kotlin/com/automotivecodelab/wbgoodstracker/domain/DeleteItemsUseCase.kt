package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import javax.inject.Inject

class DeleteItemsUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        itemsIdToDelete: List<String>,
        onAuthenticationFailureCallback: () -> Unit
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
