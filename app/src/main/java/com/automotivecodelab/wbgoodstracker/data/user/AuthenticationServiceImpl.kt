package com.automotivecodelab.wbgoodstracker.data.user

import android.content.Context
import com.automotivecodelab.wbgoodstracker.BuildConfig
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthenticationServiceImpl(val context: Context): AuthenticationService {

    private val TOKEN_REFRESH_INTERVAL = 10 * 60 * 1000

    override suspend fun signIn(): User {
        val gso = GoogleSignInOptions.Builder()
            .requestIdToken(BuildConfig.SERVER_CLIENT_ID)
            .requestEmail()
            .build()
        //building a bridge between coroutines and callback api
        val googleSignInAccount = suspendCoroutine<GoogleSignInAccount?> { continuation ->
            GoogleSignIn.getClient(context, gso).silentSignIn()
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
                .addOnCanceledListener { continuation.resume(null) }
        }

        if (googleSignInAccount?.idToken == null) {
            throw AuthenticationException()
        }

        return User(googleSignInAccount.idToken, googleSignInAccount.email)
    }

    override suspend fun updateToken(user: User): User {
        if (Date().time - user.lastUpdated > TOKEN_REFRESH_INTERVAL) {
            return signIn()
        }
        return user
    }
}