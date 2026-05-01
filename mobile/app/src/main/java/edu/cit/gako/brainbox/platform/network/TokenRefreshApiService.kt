package edu.cit.gako.brainbox.platform.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenRefreshApiService {
    @POST("api/auth/tokens/refresh")
    fun refreshAccessTokenEnvelopeSync(@Body request: TokenRefreshRequest): Call<ApiEnvelope<TokenRefreshResponse>>
}
