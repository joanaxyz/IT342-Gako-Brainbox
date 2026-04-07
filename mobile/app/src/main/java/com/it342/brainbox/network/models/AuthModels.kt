package com.it342.brainbox.network.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class VerifyCodeRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String
)

data class VerifyCodeResponse(
    @SerializedName("resetToken") val resetToken: String
)

data class ResetPasswordRequest(
    @SerializedName("token") val token: String,
    @SerializedName("newPassword") val newPassword: String
)
