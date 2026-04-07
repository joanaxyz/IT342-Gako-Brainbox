package com.it342.brainbox.network

import com.it342.brainbox.network.models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/refresh-token")
    fun refreshAccessTokenSync(@Query("refreshToken") refreshToken: String): Call<LoginResponse>

    @POST("api/auth/refresh-token")
    suspend fun refreshAccessToken(@Query("refreshToken") refreshToken: String): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): String

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): String

    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): VerifyCodeResponse

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): String

    @GET("api/user/me")
    suspend fun getUserProfile(): UserProfile
}
