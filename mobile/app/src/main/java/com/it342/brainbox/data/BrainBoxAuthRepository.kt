package com.it342.brainbox.data

import com.it342.brainbox.network.ApiService
import com.it342.brainbox.network.SessionManager
import com.it342.brainbox.network.models.ForgotPasswordRequest
import com.it342.brainbox.network.models.LoginRequest
import com.it342.brainbox.network.models.LogoutRequest
import com.it342.brainbox.network.models.RegisterRequest
import com.it342.brainbox.network.models.ResetPasswordRequest
import com.it342.brainbox.network.models.UserProfile
import com.it342.brainbox.network.models.VerifyCodeRequest

internal class BrainBoxAuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    fun hasSession(): Boolean = !sessionManager.fetchAuthToken().isNullOrBlank()

    fun sessionUsername(): String = sessionManager.fetchUsername().orEmpty()

    suspend fun login(username: String, password: String): UserProfile {
        val tokens = apiService.loginEnvelope(LoginRequest(username, password))
            .requireData("We couldn't sign you in.")

        sessionManager.saveAuthToken(tokens.accessToken)
        sessionManager.saveRefreshToken(tokens.refreshToken)
        sessionManager.saveUsername(username)

        return fetchProfileOrFallback(username)
    }

    suspend fun register(username: String, email: String, password: String) {
        apiService.registerEnvelope(RegisterRequest(username, email, password))
            .requireSuccess("We couldn't create that account yet.")
    }

    suspend fun sendPasswordResetCode(email: String) {
        apiService.forgotPasswordEnvelope(ForgotPasswordRequest(email))
            .requireSuccess("We couldn't send the reset code.")
    }

    suspend fun verifyPasswordResetCode(email: String, code: String): String {
        return apiService.verifyCodeEnvelope(VerifyCodeRequest(email, code))
            .requireData("We couldn't verify that reset code.")
            .resetToken
    }

    suspend fun resetPassword(token: String, password: String) {
        apiService.resetPasswordEnvelope(ResetPasswordRequest(token, password))
            .requireSuccess("We couldn't reset your password.")
    }

    suspend fun logout() {
        val refreshToken = sessionManager.fetchRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            runCatching {
                apiService.logoutEnvelope(LogoutRequest(refreshToken))
                    .requireSuccess("We couldn't sign you out cleanly.")
            }
        }

        sessionManager.clearSession()
    }

    private suspend fun fetchProfileOrFallback(username: String): UserProfile {
        return runCatching {
            apiService.getUserProfileEnvelope().requireData("We couldn't load your profile.")
        }.getOrElse {
            UserProfile(
                username = username,
                email = "",
                createdAt = null
            )
        }
    }
}
