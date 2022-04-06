package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val itemsRepository: ItemsRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        userRepository.setUserAuthenticated(true)
        return itemsRepository.mergeItems(user.idToken)
    }
}
