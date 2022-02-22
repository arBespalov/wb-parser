package com.automotivecodelab.wbgoodstracker.data.user

import com.automotivecodelab.wbgoodstracker.data.user.local.UserLocalDataSource
import com.automotivecodelab.wbgoodstracker.domain.util.Result
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import com.automotivecodelab.wbgoodstracker.domain.models.User

class UserRepositoryImpl(
    private val localDataSource: UserLocalDataSource,
    private val authenticationService: AuthenticationService
): UserRepository {

    override suspend fun getUser(): Result<User?> {
        if (localDataSource.isUserSignedIn()) {

            val localUser = localDataSource.user

            try {
                if (localUser == null) {
                    authenticationService.signIn()
                } else {
                    authenticationService.updateToken(localUser)
                }.also {
                    localDataSource.user = it
                    return Result.Success(it)
                }
            } catch (e: AuthenticationException) {
                return Result.Error(e)
            }
        } else {
            return Result.Success(null)
        }
    }

    override fun handleSignInResult(user: User) {
        localDataSource.user = user
    }

    override fun signOut() {
        localDataSource.user = null
    }
}