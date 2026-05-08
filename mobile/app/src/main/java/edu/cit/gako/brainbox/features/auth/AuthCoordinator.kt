package edu.cit.gako.brainbox.features.auth

import android.util.Log
import edu.cit.gako.brainbox.app.AppState
import edu.cit.gako.brainbox.app.AuthStage
import edu.cit.gako.brainbox.app.HomeTab
import edu.cit.gako.brainbox.features.auth.data.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class AuthCoordinator(
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope,
    private val getState: () -> AppState,
    private val setState: (AppState) -> Unit,
    private val prepareAudioService: () -> Unit,
    private val syncHome: suspend (setBusy: Boolean) -> Unit,
    private val showMessage: (String) -> Unit
) {
    fun handleGoogleLogin(idToken: String) {
        scope.launch {
            setState(getState().copy(isBusy = true))

            try {
                val user = authRepository.loginWithGoogle(idToken)
                setState(
                    getState().copy(
                        isAuthenticated = true,
                        user = user,
                        currentTab = HomeTab.DASHBOARD
                    )
                )
                prepareAudioService()
                syncHome(false)
            } catch (_: Exception) {
                setState(getState().copy(isBusy = false))
                showMessage("We couldn't sign you in with Google. Try again.")
            }
        }
    }

    fun handleLogin(username: String, password: String) {
        scope.launch {
            setState(getState().copy(isBusy = true))

            try {
                val user = authRepository.login(username, password)
                setState(
                    getState().copy(
                        isAuthenticated = true,
                        user = user,
                        currentTab = HomeTab.DASHBOARD
                    )
                )
                prepareAudioService()
                syncHome(false)
            } catch (_: Exception) {
                setState(getState().copy(isBusy = false))
                showMessage("We couldn't sign you in. Check your credentials and try again.")
            }
        }
    }

    fun handleRegister(username: String, email: String, password: String) {
        scope.launch {
            Log.d("AuthCoordinator", "handleRegister started for: $username")
            setState(getState().copy(isBusy = true))

            try {
                authRepository.register(username, email, password)
                Log.d("AuthCoordinator", "Registration API success, current authStage: ${getState().authStage}")
                // Registration successful - redirect to login
                handleAuthStageChange(AuthStage.LOGIN)
                Log.d("AuthCoordinator", "After handleAuthStageChange(LOGIN), authStage: ${getState().authStage}")
                setState(getState().copy(isBusy = false))
                Log.d("AuthCoordinator", "Registration complete, redirected to LOGIN")
                showMessage("Account created! Please sign in to continue.")
            } catch (e: Exception) {
                Log.e("AuthCoordinator", "Registration failed: ${e.message}", e)
                setState(getState().copy(isBusy = false))
                // Show specific error message based on exception
                val errorMsg = e.message?.lowercase() ?: ""
                when {
                    errorMsg.contains("already") || errorMsg.contains("taken") || errorMsg.contains("exists") ->
                        showMessage("This username or email is already registered. Try signing in instead.")
                    errorMsg.contains("network") || errorMsg.contains("connection") || errorMsg.contains("timeout") ->
                        showMessage("Network error. Check your connection and try again.")
                    errorMsg.isNotBlank() ->
                        showMessage("Registration failed: ${e.message}")
                    else ->
                        showMessage("We couldn't create that account. Please try again.")
                }
            }
        }
    }

    fun handleSendResetCode(email: String) {
        scope.launch {
            setState(getState().copy(isBusy = true))

            try {
                authRepository.sendPasswordResetCode(email)
                setState(
                    getState().copy(
                        isBusy = false,
                        authStage = AuthStage.FORGOT_CODE,
                        pendingResetEmail = email
                    )
                )
                showMessage("Reset code sent to $email.")
            } catch (_: Exception) {
                setState(getState().copy(isBusy = false))
                showMessage("We couldn't send a reset code right now.")
            }
        }
    }

    fun handleVerifyResetCode(code: String) {
        scope.launch {
            val email = getState().pendingResetEmail
            if (email.isBlank()) {
                showMessage("Start with your email so we know where to send the reset code.")
                return@launch
            }

            setState(getState().copy(isBusy = true))

            try {
                val resetToken = authRepository.verifyPasswordResetCode(email, code)
                setState(
                    getState().copy(
                        isBusy = false,
                        authStage = AuthStage.RESET_PASSWORD,
                        resetToken = resetToken
                    )
                )
                showMessage("Code verified. Set your new password.")
            } catch (_: Exception) {
                setState(getState().copy(isBusy = false))
                showMessage("That reset code didn't work. Try again.")
            }
        }
    }

    fun handleResetPassword(password: String) {
        scope.launch {
            val token = getState().resetToken
            if (token.isNullOrBlank()) {
                showMessage("Request a fresh reset code before setting a new password.")
                return@launch
            }

            setState(getState().copy(isBusy = true))

            try {
                authRepository.resetPassword(token, password)
                setState(
                    getState().copy(
                        isBusy = false,
                        authStage = AuthStage.LOGIN,
                        pendingResetEmail = "",
                        resetToken = null
                    )
                )
                showMessage("Password updated. You can sign in now.")
            } catch (_: Exception) {
                setState(getState().copy(isBusy = false))
                showMessage("We couldn't reset your password yet.")
            }
        }
    }

    fun handleAuthStageChange(stage: AuthStage) {
        setState(
            when (stage) {
                AuthStage.LOGIN -> getState().copy(
                    authStage = stage,
                    pendingResetEmail = "",
                    resetToken = null
                )
                else -> getState().copy(authStage = stage)
            }
        )
    }

    fun handleLogout() {
        scope.launch {
            authRepository.logout()
            setState(AppState(isBootstrapping = false))
            showMessage("Signed out.")
        }
    }
}
