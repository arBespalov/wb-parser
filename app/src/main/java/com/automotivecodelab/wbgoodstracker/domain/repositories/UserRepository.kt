package com.automotivecodelab.wbgoodstracker.domain.repositories

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.util.Result

interface UserRepository {
    suspend fun getUser(): Result<User?>
    fun handleSignInResult(user: User)
    fun signOut()
}
