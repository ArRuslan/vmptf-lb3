package com.rdev.nure.vmptflb3.api.responses

import com.rdev.nure.vmptflb3.api.entities.User

data class AuthResponse(
    val token: String,
    val expires_at: Long,
    val user: User,
)