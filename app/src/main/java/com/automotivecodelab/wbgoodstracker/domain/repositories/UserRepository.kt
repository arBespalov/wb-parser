package com.automotivecodelab.wbgoodstracker.domain.repositories

import com.automotivecodelab.wbgoodstracker.domain.util.Result
import com.automotivecodelab.wbgoodstracker.domain.models.User

interface UserRepository {
    suspend fun getUser(): Result<User?>
    fun handleSignInResult(user: User)
    fun signOut()
}