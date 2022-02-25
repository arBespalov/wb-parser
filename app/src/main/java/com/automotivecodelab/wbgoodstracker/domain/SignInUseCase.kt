package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import com.automotivecodelab.wbgoodstracker.domain.util.Result

class SignInUseCase(
    private val userRepository: UserRepository,
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        userRepository.handleSignInResult(user)
        return itemsRepository.mergeItems(user.idToken)
    }
}
