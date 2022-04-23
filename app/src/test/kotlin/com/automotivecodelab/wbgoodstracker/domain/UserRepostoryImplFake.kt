package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class UserRepostoryImplFake : UserRepository {
    override suspend fun isUserAuthenticated(): Boolean {
        return false
    }

    override suspend fun signIn() {
        TODO("Not yet implemented")
    }

    override suspend fun signOut() {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(): Result<User> {
        TODO("Not yet implemented")
    }
}
