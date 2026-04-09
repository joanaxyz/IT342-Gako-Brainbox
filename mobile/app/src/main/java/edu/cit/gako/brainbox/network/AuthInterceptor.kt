package edu.cit.gako.brainbox.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager,
    private val authApiService: AuthApiService
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        sessionManager.fetchAuthToken()?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val response = chain.proceed(requestBuilder.build())
        if (response.code() != 401) {
            return response
        }

        val refreshToken = sessionManager.fetchRefreshToken() ?: return response
        val refreshResponse = authApiService.refreshAccessTokenEnvelopeSync(refreshToken).execute()
        val refreshPayload = refreshResponse.body()
        val newTokens = refreshPayload?.data

        if (refreshResponse.isSuccessful && refreshPayload?.success == true && newTokens != null) {
            sessionManager.saveAuthToken(newTokens.accessToken)
            sessionManager.saveRefreshToken(newTokens.refreshToken)

            response.close()
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer ${newTokens.accessToken}")
                .build()
            return chain.proceed(newRequest)
        }

        sessionManager.clearSession()
        return response
    }
}

