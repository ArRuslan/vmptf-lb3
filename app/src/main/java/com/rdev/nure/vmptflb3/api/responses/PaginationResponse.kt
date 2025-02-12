package com.rdev.nure.vmptflb3.api.responses

data class PaginationResponse<T>(
    val count: Long,
    val result: List<T>,
)