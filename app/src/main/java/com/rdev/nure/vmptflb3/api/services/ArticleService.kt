package com.rdev.nure.vmptflb3.api.services

import com.rdev.nure.vmptflb3.api.entities.Article
import com.rdev.nure.vmptflb3.api.requests.CreateArticleRequest
import com.rdev.nure.vmptflb3.api.requests.EditArticleRequest
import com.rdev.nure.vmptflb3.api.responses.PaginationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ArticleService {
    @GET("/articles/search")
    suspend fun fetchArticles(
        @Query("title") title: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("publisher_id") publisherId: Long? = null,
        @Query("category_id") categoryId: Long? = null,
    ): Response<PaginationResponse<Article>>

    @POST("/articles")
    suspend fun createArticle(
        @Body body: CreateArticleRequest,
        @Header("Authorization") authToken: String,
    ): Response<Article>

    @DELETE("/articles/{article_id}")
    suspend fun deleteArticle(
        @Path("article_id") articleId: Long,
        @Header("Authorization") authToken: String,
    ): Response<Void>

    @PATCH("/articles/{article_id}")
    suspend fun editArticle(
        @Path("article_id") articleId: Long,
        @Body body: EditArticleRequest,
        @Header("Authorization") authToken: String,
    ): Response<Article>
}