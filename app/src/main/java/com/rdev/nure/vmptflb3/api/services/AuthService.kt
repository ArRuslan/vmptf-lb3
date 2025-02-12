package com.rdev.nure.vmptflb3.api.services

import com.rdev.nure.vmptflb3.api.requests.LoginRequest
import com.rdev.nure.vmptflb3.api.responses.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>
}