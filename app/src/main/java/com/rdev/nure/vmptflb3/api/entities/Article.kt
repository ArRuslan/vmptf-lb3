package com.rdev.nure.vmptflb3.api.entities

data class Article(
    val id: Long,
    val title: String,
    val text: String,
    val created_at: Long,
    val publisher: User,
    val category: Category,
)