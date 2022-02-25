package com.automotivecodelab.wbgoodstracker.data.user.local

import android.content.SharedPreferences
import com.automotivecodelab.wbgoodstracker.domain.models.User

const val IS_USER_SIGNED_IN = "isUserSignedIn"

class UserLocalDataSourceImpl(
    private val sharedPreferences: SharedPreferences
) : UserLocalDataSource {

    override var user: User? = null
        set(value) {
            sharedPreferences.edit()
                .putBoolean(IS_USER_SIGNED_IN, value != null)
                .apply()
            field = value
        }

    override fun isUserSignedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_USER_SIGNED_IN, false)
    }
}
