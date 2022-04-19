package com.automotivecodelab.wbgoodstracker.domain.repositories

import com.automotivecodelab.wbgoodstracker.domain.models.User

interface UserRepository {
    suspend fun isUserAuthenticated(): Boolean
    suspend fun signIn()
    suspend fun signOut()
    suspend fun getUser(): Result<User>
}
