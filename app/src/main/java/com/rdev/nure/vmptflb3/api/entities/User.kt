package com.rdev.nure.vmptflb3.api.entities

data class User(
    val id: Long,
    val name: String,
    val role: Short = 0,
)