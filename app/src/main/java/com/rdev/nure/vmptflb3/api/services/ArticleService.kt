package com.rdev.nure.vmptflb3.api.services

import com.rdev.nure.vmptflb3.api.entities.Article
import com.rdev.nure.vmptflb3.api.entities.Comment
import com.rdev.nure.vmptflb3.api.requests.CreateArticleRequest
import com.rdev.nure.vmptflb3.api.requests.PostCommentRequest
import com.rdev.nure.vmptflb3.api.responses.PaginationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ArticleService {
    @GET("/articles/search")
    suspend fun fetchArticles(
        @Query("title") title: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
    ): Response<PaginationResponse<Article>>

    @POST("/articles")
    suspend fun createArticle(
        @Body body: CreateArticleRequest,
        @Header("Authorization") authToken: String,
    ): Response<Article>
}