package com.automotivecodelab.wbgoodstracker.data.user

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.google.android.gms.auth.api.identity.SignInCredential

interface AuthenticationService {
    suspend fun signIn(): User
    suspend fun updateToken(user: User): User
}