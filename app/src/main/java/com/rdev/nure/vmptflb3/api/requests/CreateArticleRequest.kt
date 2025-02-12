package com.rdev.nure.vmptflb3.api.requests

data class CreateArticleRequest(
    val title: String,
    val text: String,
    val category_id: Long,
)
