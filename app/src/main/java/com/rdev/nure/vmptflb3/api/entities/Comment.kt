package com.rdev.nure.vmptflb3.api.entities

data class Comment(
    val id: Long,
    val text: String,
    val created_at: Long,
    val user: User,
)
