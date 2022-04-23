package com.automotivecodelab.wbgoodstracker.data.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val authenticationService: AuthenticationService
) : UserRepository {
    private val IS_USER_AUTHENTICATED = booleanPreferencesKey("isUserSignedIn")

    override suspend fun isUserAuthenticated(): Boolean {
        return dataStore.data
            .map { prefs ->
                prefs[IS_USER_AUTHENTICATED] ?: false
            }
            .first()
    }

    override suspend fun signIn() {
        dataStore.edit { prefs ->
            prefs[IS_USER_AUTHENTICATED] = true
        }
    }

    override suspend fun signOut() {
        authenticationService.signOut()
            .onSuccess {
                dataStore.edit { prefs ->
                    prefs[IS_USER_AUTHENTICATED] = false
                }
            }
    }

    override suspend fun getUser(): Result<User> {
        return runCatching {
            authenticationService.signIn()
        }
    }
}
