package com.it342.brainbox.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Add token if available
        sessionManager.fetchAuthToken()?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val response = chain.proceed(requestBuilder.build())

        // Handle 401 Unauthorized
        if (response.code() == 401) {
            val refreshToken = sessionManager.fetchRefreshToken()
            if (refreshToken != null) {
                // Synchronously refresh token
                val refreshResponse = RetrofitClient.apiService.refreshAccessTokenSync(refreshToken).execute()
                
                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val newTokens = refreshResponse.body()!!
                    sessionManager.saveAuthToken(newTokens.accessToken)
                    sessionManager.saveRefreshToken(newTokens.refreshToken)

                    // Close old response and retry original request
                    response.close()
                    val newRequest = originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()
                    return chain.proceed(newRequest)
                } else {
                    // Refresh failed, clear session
                    sessionManager.clearSession()
                }
            }
        }

        return response
    }
}
