package com.automotivecodelab.wbgoodstracker.data.user

import android.content.Context
import com.automotivecodelab.wbgoodstracker.BuildConfig
import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface AuthenticationService {
    suspend fun signIn(): User
    suspend fun signOut(): Result<Unit>
}

class AuthenticationServiceImpl @Inject constructor(
    private val context: Context
) : AuthenticationService {
    private val gso = GoogleSignInOptions.Builder()
        .requestIdToken(BuildConfig.SERVER_CLIENT_ID)
        .requestEmail()
        .build()

    override suspend fun signIn(): User {
        // building a bridge between coroutines and callback api
        val googleSignInAccount = suspendCoroutine<GoogleSignInAccount?> { continuation ->
            GoogleSignIn.getClient(context, gso).silentSignIn()
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
                .addOnCanceledListener { continuation.resume(null) }
        }
        if (googleSignInAccount?.idToken == null)
            throw Exception()
        else
            return User(googleSignInAccount.idToken!!, googleSignInAccount.email)
    }

    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            suspendCoroutine<Void> { continuation ->
                GoogleSignIn.getClient(context, gso).signOut()
                    .addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { throw Exception() }
                    .addOnCanceledListener { throw Exception() }
            }
        }
    }
}