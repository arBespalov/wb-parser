package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(idToken: String): Result<Unit> {
        val result = itemsRepository.mergeItems(idToken)
        result.onSuccess { userRepository.signIn() }
        return result
    }
}
