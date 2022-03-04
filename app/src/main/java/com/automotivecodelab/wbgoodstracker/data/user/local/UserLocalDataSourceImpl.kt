package com.automotivecodelab.wbgoodstracker.data.user.local

import android.content.SharedPreferences
import com.automotivecodelab.wbgoodstracker.domain.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val IS_USER_SIGNED_IN = "isUserSignedIn"

class UserLocalDataSourceImpl(
    private val sharedPreferences: SharedPreferences
) : UserLocalDataSource {
    private var _user: User? = null
    override val user = _user

    override suspend fun setUser(newUser: User?) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putBoolean(IS_USER_SIGNED_IN, newUser != null)
                .apply()
            _user = newUser
        }
    }

    override suspend fun isUserSignedIn(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(IS_USER_SIGNED_IN, false)
        }
    }
}
