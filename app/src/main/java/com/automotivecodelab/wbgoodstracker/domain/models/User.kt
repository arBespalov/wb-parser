package com.automotivecodelab.wbgoodstracker.domain.models

import java.util.*

data class User(
    val idToken: String,
    val email: String?,
    val lastUpdated: Long = Date().time
)
