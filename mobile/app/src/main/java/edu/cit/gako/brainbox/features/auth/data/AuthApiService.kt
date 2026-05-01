package edu.cit.gako.brainbox.features.auth.data

import edu.cit.gako.brainbox.platform.network.ApiEnvelope
import edu.cit.gako.brainbox.features.auth.data.dto.ForgotPasswordRequest
import edu.cit.gako.brainbox.features.auth.data.dto.GoogleLoginRequest
import edu.cit.gako.brainbox.features.auth.data.dto.LoginRequest
import edu.cit.gako.brainbox.features.auth.data.dto.LoginResponse
import edu.cit.gako.brainbox.features.auth.data.dto.LogoutRequest
import edu.cit.gako.brainbox.features.auth.data.dto.RegisterRequest
import edu.cit.gako.brainbox.features.auth.data.dto.RefreshTokenRequest
import edu.cit.gako.brainbox.features.auth.data.dto.ResetPasswordRequest
import edu.cit.gako.brainbox.features.home.profile.data.dto.UserProfile
import edu.cit.gako.brainbox.features.auth.data.dto.VerifyCodeRequest
import edu.cit.gako.brainbox.features.auth.data.dto.VerifyCodeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/login")
    suspend fun loginEnvelope(@Body request: LoginRequest): ApiEnvelope<LoginResponse>

    @POST("api/auth/tokens/refresh")
    fun refreshAccessTokenSync(@Body request: RefreshTokenRequest): Call<LoginResponse>

    @POST("api/auth/tokens/refresh")
    fun refreshAccessTokenEnvelopeSync(@Body request: RefreshTokenRequest): Call<ApiEnvelope<LoginResponse>>

    @POST("api/auth/tokens/refresh")
    suspend fun refreshAccessToken(@Body request: RefreshTokenRequest): LoginResponse

    @POST("api/auth/tokens/refresh")
    suspend fun refreshAccessTokenEnvelope(@Body request: RefreshTokenRequest): ApiEnvelope<LoginResponse>

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

    @POST("api/auth/google")
    suspend fun googleLoginEnvelope(@Body request: GoogleLoginRequest): ApiEnvelope<LoginResponse>

    @GET("api/users/me")
    suspend fun getUserProfileEnvelope(): ApiEnvelope<UserProfile>
}

