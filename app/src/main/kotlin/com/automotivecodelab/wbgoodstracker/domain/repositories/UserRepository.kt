package com.automotivecodelab.wbgoodstracker.domain.repositories

import com.automotivecodelab.wbgoodstracker.domain.models.User

interface UserRepository {
    suspend fun isUserAuthenticated(): Boolean
    suspend fun setUserAuthenticated(isAuthenticated: Boolean)
    suspend fun getUser(): Result<User>
}
