package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import com.automotivecodelab.wbgoodstracker.domain.util.Result

class DeleteItemsUseCase(
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(itemsIdToDelete: Array<String>, onAuthenticationFailureCallback: ()-> Unit = {}) {
        return when (val authenticationResult = userRepository.getUser()) {
            is Result.Error -> {
                onAuthenticationFailureCallback()
                itemsRepository.deleteItems(itemsIdToDelete)
            }
            is Result.Success<User?> -> {
                if (authenticationResult.data != null) {
                    itemsRepository.deleteItems(
                        itemsIdToDelete,
                        authenticationResult.data.idToken
                    )
                } else {
                    itemsRepository.deleteItems(itemsIdToDelete)
                }
            }
        }
    }
}