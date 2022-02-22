package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class SignOutUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke() {
        userRepository.signOut()
    }
}