package edu.cit.gako.brainbox.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import edu.cit.gako.brainbox.data.BrainBoxRepository
import edu.cit.gako.brainbox.network.models.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class BrainBoxAppController(
    private val repository: BrainBoxRepository,
    private val scope: CoroutineScope,
    private val onMessage: (String) -> Unit
) {
    var state by mutableStateOf(AppState())
        private set

    fun bootstrap() {
        scope.launch {
            if (!repository.hasSession()) {
                state = state.copy(isBootstrapping = false)
                return@launch
            }

            syncHome(setBusy = false)
        }
    }

    fun handleGoogleLogin(idToken: String) {
        scope.launch {
            state = state.copy(isBusy = true)

            try {
                val user = repository.loginWithGoogle(idToken)
                state = state.copy(
                    isAuthenticated = true,
                    user = user,
                    currentTab = HomeTab.DASHBOARD
                )
                syncHome(setBusy = false)
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't sign you in with Google. Try again.")
            }
        }
    }

    fun handleLogin(username: String, password: String) {
        scope.launch {
            state = state.copy(isBusy = true)

            try {
                val user = repository.login(username, password)
                state = state.copy(
                    isAuthenticated = true,
                    user = user,
                    currentTab = HomeTab.DASHBOARD
                )
                syncHome(setBusy = false)
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't sign you in. Check your credentials and try again.")
            }
        }
    }

    fun handleRegister(username: String, email: String, password: String) {
        scope.launch {
            state = state.copy(isBusy = true)

            try {
                repository.register(username, email, password)
                state = state.copy(
                    isBusy = false,
                    authStage = AuthStage.LOGIN
                )
                showMessage("Account created. Sign in to continue.")
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't create that account yet. Check the details and try again.")
            }
        }
    }

    fun handleSendResetCode(email: String) {
        scope.launch {
            state = state.copy(isBusy = true)

            try {
                repository.sendPasswordResetCode(email)
                state = state.copy(
                    isBusy = false,
                    authStage = AuthStage.FORGOT_CODE,
                    pendingResetEmail = email
                )
                showMessage("Reset code sent to $email.")
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't send a reset code right now.")
            }
        }
    }

    fun handleVerifyResetCode(code: String) {
        scope.launch {
            val email = state.pendingResetEmail
            if (email.isBlank()) {
                showMessage("Start with your email so we know where to send the reset code.")
                return@launch
            }

            state = state.copy(isBusy = true)

            try {
                val resetToken = repository.verifyPasswordResetCode(email, code)
                state = state.copy(
                    isBusy = false,
                    authStage = AuthStage.RESET_PASSWORD,
                    resetToken = resetToken
                )
                showMessage("Code verified. Set your new password.")
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("That reset code didn't work. Try again.")
            }
        }
    }

    fun handleResetPassword(password: String) {
        scope.launch {
            val token = state.resetToken
            if (token.isNullOrBlank()) {
                showMessage("Request a fresh reset code before setting a new password.")
                return@launch
            }

            state = state.copy(isBusy = true)

            try {
                repository.resetPassword(token, password)
                state = state.copy(
                    isBusy = false,
                    authStage = AuthStage.LOGIN,
                    pendingResetEmail = "",
                    resetToken = null
                )
                showMessage("Password updated. You can sign in now.")
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't reset your password yet.")
            }
        }
    }

    fun handleAuthStageChange(stage: AuthStage) {
        state = when (stage) {
            AuthStage.LOGIN -> state.copy(
                authStage = stage,
                pendingResetEmail = "",
                resetToken = null
            )
            else -> state.copy(authStage = stage)
        }
    }

    fun handleTabSelected(tab: HomeTab) {
        state = state.copy(currentTab = tab)
    }

    fun handleOpenQuiz(uuid: String) {
        scope.launch {
            state = state.copy(isBusy = true)

            try {
                val quiz = repository.getQuiz(uuid)
                state = state.copy(
                    isBusy = false,
                    activeQuiz = quiz,
                    activeFlashcardDeck = null
                )
                syncStudyContext(quiz.notebookUuid)
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't open that quiz yet.")
            }
        }
    }

    fun handleOpenFlashcardDeck(uuid: String) {
        scope.launch {
            state = state.copy(isBusy = true)

            try {
                val deck = repository.getFlashcardDeck(uuid)
                state = state.copy(
                    isBusy = false,
                    activeFlashcardDeck = deck,
                    activeQuiz = null
                )
                syncStudyContext(deck.notebookUuid)
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't open that deck yet.")
            }
        }
    }

    fun handleExitStudySession() {
        state = state.copy(
            activeQuiz = null,
            activeFlashcardDeck = null
        )
    }

    fun handleRecordQuizAttempt(uuid: String, score: Int) {
        scope.launch {
            runCatching {
                repository.recordQuizAttempt(uuid, score)
                syncHome(setBusy = false)
            }.onFailure {
                showMessage("We couldn't save your quiz score.")
            }
        }
    }

    fun handleRecordFlashcardAttempt(uuid: String, mastery: Int) {
        scope.launch {
            runCatching {
                repository.recordFlashcardAttempt(uuid, mastery)
                syncHome(setBusy = false)
            }.onFailure {
                showMessage("We couldn't save your deck progress.")
            }
        }
    }

    fun refreshHome() {
        scope.launch {
            syncHome(setBusy = true)
        }
    }

    fun handleLogout() {
        scope.launch {
            repository.logout()
            state = AppState(isBootstrapping = false)
            showMessage("Signed out.")
        }
    }

    private suspend fun syncHome(setBusy: Boolean) {
        if (setBusy) {
            state = state.copy(isBusy = true)
        }

        try {
            val bundle = repository.loadHome()
            state = state.copy(
                isBootstrapping = false,
                isBusy = false,
                isAuthenticated = true,
                user = bundle.user,
                homeData = bundle.homeData
            )
        } catch (error: HttpException) {
            repository.logout()
            state = AppState(isBootstrapping = false)
            showMessage(
                if (error.code() == 401) {
                    "Your session expired. Sign in again to keep going."
                } else {
                    "We couldn't sync the home data right now."
                }
            )
        } catch (_: Exception) {
            state = state.copy(
                isBootstrapping = false,
                isBusy = false,
                isAuthenticated = repository.hasSession(),
                user = state.user ?: fallbackUser(),
                homeData = state.homeData.copy(
                    syncNotice = "The shell is ready, but live home data is still catching up."
                )
            )
            showMessage("The mobile shell is live. Home data syncing is still catching up.")
        }
    }

    private fun fallbackUser(): UserProfile {
        return UserProfile(
            username = repository.sessionUsername().ifBlank { "BrainBox User" },
            email = "",
            createdAt = null
        )
    }

    private fun syncStudyContext(notebookUuid: String?) {
        if (notebookUuid.isNullOrBlank()) {
            return
        }

        scope.launch {
            runCatching {
                repository.markNotebookReviewed(notebookUuid)
                val bundle = repository.loadHome()
                state = state.copy(
                    user = bundle.user,
                    homeData = bundle.homeData
                )
            }
        }
    }

    private fun showMessage(message: String) {
        onMessage(message)
    }
}

