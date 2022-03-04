package com.automotivecodelab.wbgoodstracker.data.user.local

import com.automotivecodelab.wbgoodstracker.domain.models.User

interface UserLocalDataSource {
    val user: User?
    suspend fun setUser(user: User?)
    suspend fun isUserSignedIn(): Boolean
}
