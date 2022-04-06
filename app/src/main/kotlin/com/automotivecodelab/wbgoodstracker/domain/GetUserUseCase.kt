package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        return if (userRepository.isUserAuthenticated()) userRepository.getUser()
        else Result.failure(SecurityException())
    }
}
