package edu.cit.gako.brainbox.platform.network

import com.google.gson.annotations.SerializedName

data class TokenRefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class TokenRefreshResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)
