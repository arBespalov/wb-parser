package com.automotivecodelab.wbgoodstracker.data.user

import android.content.Context
import com.automotivecodelab.wbgoodstracker.BuildConfig
import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface AuthenticationService {
    suspend fun signIn(): User
}

class AuthenticationServiceImpl(val context: Context) : AuthenticationService {

    override suspend fun signIn(): User {
        val gso = GoogleSignInOptions.Builder()
            .requestIdToken(BuildConfig.SERVER_CLIENT_ID)
            .requestEmail()
            .build()
        // building a bridge between coroutines and callback api
        val googleSignInAccount = suspendCoroutine<GoogleSignInAccount?> { continuation ->
            GoogleSignIn.getClient(context, gso).silentSignIn()
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
                .addOnCanceledListener { continuation.resume(null) }
        }
        if (googleSignInAccount?.idToken == null) {
            throw Exception()
        } else {
            return User(googleSignInAccount.idToken!!, googleSignInAccount.email)
        }
    }
}