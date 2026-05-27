package edu.cit.gako.brainbox.platform.network

import edu.cit.gako.brainbox.platform.persistence.SessionCredentials
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager,
    private val tokenRefreshApiService: TokenRefreshApiService
) : Interceptor {
    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        val authToken = sessionManager.fetchAuthToken()

        authToken?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        val response = chain.proceed(requestBuilder.build())
        if (response.code() != 401) {
            return response
        }

        val retryToken = synchronized(refreshLock) {
            val latestToken = sessionManager.fetchAuthToken()
            if (!latestToken.isNullOrBlank() && latestToken != authToken) {
                latestToken
            } else {
                val refreshToken = sessionManager.fetchRefreshToken() ?: return@synchronized null
                val refreshResponse = tokenRefreshApiService
                    .refreshAccessTokenEnvelopeSync(TokenRefreshRequest(refreshToken))
                    .execute()
                val refreshPayload = refreshResponse.body()
                val newTokens = refreshPayload?.data

                if (refreshResponse.isSuccessful && refreshPayload?.success == true && newTokens != null) {
                    sessionManager.replace(
                        SessionCredentials(
                            accessToken = newTokens.accessToken,
                            refreshToken = newTokens.refreshToken,
                            username = sessionManager.fetchUsername()
                        )
                    )
                    newTokens.accessToken
                } else {
                    sessionManager.clearSession()
                    null
                }
            }
        }

        if (!retryToken.isNullOrBlank()) {
            response.close()
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $retryToken")
                .build()
            return chain.proceed(newRequest)
        }

        return response
    }
}

