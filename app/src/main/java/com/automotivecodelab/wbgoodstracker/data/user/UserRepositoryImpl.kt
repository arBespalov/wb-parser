package com.automotivecodelab.wbgoodstracker.data.user

import com.automotivecodelab.wbgoodstracker.data.user.local.UserLocalDataSource
import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository

class UserRepositoryImpl(
    private val localDataSource: UserLocalDataSource,
    private val authenticationService: AuthenticationService
) : UserRepository {

    override suspend fun getUser(): Result<User?> {
        if (localDataSource.isUserSignedIn()) {
            val localUser = localDataSource.user
            return runCatching {
                if (localUser == null) {
                    authenticationService.signIn()
                } else {
                    authenticationService.updateToken(localUser)
                }.also {
                    localDataSource.setUser(it)
                    return Result.success(it)
                }
            }
        } else {
            return Result.success(null)
        }
    }

    override suspend fun handleSignInResult(user: User) {
        localDataSource.setUser(user)
    }

    override suspend fun signOut() {
        localDataSource.setUser(null)
    }
}
