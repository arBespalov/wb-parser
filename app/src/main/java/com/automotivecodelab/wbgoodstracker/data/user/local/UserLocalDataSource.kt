package com.automotivecodelab.wbgoodstracker.data.user.local

import com.automotivecodelab.wbgoodstracker.domain.models.User

interface UserLocalDataSource {
    var user: User?
    fun isUserSignedIn(): Boolean
}
