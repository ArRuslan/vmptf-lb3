package com.rdev.nure.vmptflb3.api.services

import com.rdev.nure.vmptflb3.api.entities.Category
import com.rdev.nure.vmptflb3.api.responses.PaginationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoryService {
    @GET("/categories/search")
    suspend fun fetchArticles(
        @Query("name") name: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
    ): Response<PaginationResponse<Category>>
}