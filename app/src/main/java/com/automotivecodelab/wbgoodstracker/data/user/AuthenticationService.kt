package com.automotivecodelab.wbgoodstracker.data.user

import com.automotivecodelab.wbgoodstracker.domain.models.User

interface AuthenticationService {
    suspend fun signIn(): User
    suspend fun updateToken(user: User): User
}
