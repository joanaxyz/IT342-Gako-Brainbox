package com.it342.brainbox.network

import com.it342.brainbox.network.models.ApiEnvelope
import com.it342.brainbox.network.models.ForgotPasswordRequest
import com.it342.brainbox.network.models.LoginRequest
import com.it342.brainbox.network.models.LoginResponse
import com.it342.brainbox.network.models.LogoutRequest
import com.it342.brainbox.network.models.RegisterRequest
import com.it342.brainbox.network.models.ResetPasswordRequest
import com.it342.brainbox.network.models.VerifyCodeRequest
import com.it342.brainbox.network.models.VerifyCodeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/login")
    suspend fun loginEnvelope(@Body request: LoginRequest): ApiEnvelope<LoginResponse>

    @POST("api/auth/refresh-token")
    fun refreshAccessTokenSync(@Query("refreshToken") refreshToken: String): Call<LoginResponse>

    @POST("api/auth/refresh-token")
    fun refreshAccessTokenEnvelopeSync(@Query("refreshToken") refreshToken: String): Call<ApiEnvelope<LoginResponse>>

    @POST("api/auth/refresh-token")
    suspend fun refreshAccessToken(@Query("refreshToken") refreshToken: String): LoginResponse

    @POST("api/auth/refresh-token")
    suspend fun refreshAccessTokenEnvelope(@Query("refreshToken") refreshToken: String): ApiEnvelope<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): String

    @POST("api/auth/register")
    suspend fun registerEnvelope(@Body request: RegisterRequest): ApiEnvelope<Any?>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): String

    @POST("api/auth/forgot-password")
    suspend fun forgotPasswordEnvelope(@Body request: ForgotPasswordRequest): ApiEnvelope<Any?>

    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): VerifyCodeResponse

    @POST("api/auth/verify-code")
    suspend fun verifyCodeEnvelope(@Body request: VerifyCodeRequest): ApiEnvelope<VerifyCodeResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): String

    @POST("api/auth/reset-password")
    suspend fun resetPasswordEnvelope(@Body request: ResetPasswordRequest): ApiEnvelope<Any?>

    @POST("api/auth/logout")
    suspend fun logoutEnvelope(@Body request: LogoutRequest): ApiEnvelope<Any?>
}
