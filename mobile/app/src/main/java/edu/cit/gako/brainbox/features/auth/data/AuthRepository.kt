package edu.cit.gako.brainbox.features.auth.data

import edu.cit.gako.brainbox.platform.network.SessionManager
import edu.cit.gako.brainbox.platform.network.requireData
import edu.cit.gako.brainbox.platform.network.requireSuccess
import edu.cit.gako.brainbox.features.auth.data.dto.ForgotPasswordRequest
import edu.cit.gako.brainbox.features.auth.data.dto.GoogleLoginRequest
import edu.cit.gako.brainbox.features.auth.data.dto.LoginRequest
import edu.cit.gako.brainbox.features.auth.data.dto.LogoutRequest
import edu.cit.gako.brainbox.features.auth.data.dto.RegisterRequest
import edu.cit.gako.brainbox.features.auth.data.dto.ResetPasswordRequest
import edu.cit.gako.brainbox.features.home.profile.data.dto.UserProfile
import edu.cit.gako.brainbox.features.auth.data.dto.VerifyCodeRequest

class AuthRepository(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager
) {
    fun hasSession(): Boolean = !sessionManager.fetchAuthToken().isNullOrBlank()

    fun sessionUsername(): String = sessionManager.fetchUsername().orEmpty()

    suspend fun loadProfile(): UserProfile {
        return apiService.getUserProfileEnvelope()
            .requireData("We couldn't load your profile.")
    }

    suspend fun login(username: String, password: String): UserProfile {
        val tokens = apiService.loginEnvelope(LoginRequest(username, password))
            .requireData("We couldn't sign you in.")

        sessionManager.saveAuthToken(tokens.accessToken)
        sessionManager.saveRefreshToken(tokens.refreshToken)
        sessionManager.saveUsername(username)

        return fetchProfileOrFallback(username)
    }

    suspend fun loginWithGoogle(idToken: String): UserProfile {
        val tokens = apiService.googleLoginEnvelope(GoogleLoginRequest(idToken))
            .requireData("We couldn't sign you in with Google.")

        sessionManager.saveAuthToken(tokens.accessToken)
        sessionManager.saveRefreshToken(tokens.refreshToken)

        val profile = fetchProfileOrFallback("")
        sessionManager.saveUsername(profile.username)
        return profile
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

