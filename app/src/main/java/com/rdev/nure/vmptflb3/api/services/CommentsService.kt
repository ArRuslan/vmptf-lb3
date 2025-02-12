package com.rdev.nure.vmptflb3.api.services

import com.rdev.nure.vmptflb3.api.entities.Comment
import com.rdev.nure.vmptflb3.api.responses.PaginationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentService {
    @GET("/comments/{article_id}")
    suspend fun fetchComments(
        @Path("article_id") articleId: Long,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
    ): Response<PaginationResponse<Comment>>
}