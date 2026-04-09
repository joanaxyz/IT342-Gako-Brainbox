package com.it342.brainbox

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.it342.brainbox.app.BrainBoxApp
import com.it342.brainbox.app.BrainBoxAppController
import com.it342.brainbox.data.BrainBoxRepository
import com.it342.brainbox.network.RetrofitClient
import com.it342.brainbox.network.SessionManager
import com.it342.brainbox.ui.theme.BrainboxTheme

class MainActivity : ComponentActivity() {

    private lateinit var controller: BrainBoxAppController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)
        val repository = BrainBoxRepository(RetrofitClient.apiService, sessionManager)
        controller = BrainBoxAppController(repository, lifecycleScope, ::showMessage)

        setContent {
            BrainboxTheme {
                BrainBoxApp(
                    state = controller.state,
                    onLogin = controller::handleLogin,
                    onRegister = controller::handleRegister,
                    onSendResetCode = controller::handleSendResetCode,
                    onVerifyResetCode = controller::handleVerifyResetCode,
                    onResetPassword = controller::handleResetPassword,
                    onAuthStageChange = controller::handleAuthStageChange,
                    onTabSelected = controller::handleTabSelected,
                    onOpenQuiz = controller::handleOpenQuiz,
                    onOpenFlashcardDeck = controller::handleOpenFlashcardDeck,
                    onExitStudySession = controller::handleExitStudySession,
                    onRecordQuizAttempt = controller::handleRecordQuizAttempt,
                    onRecordFlashcardAttempt = controller::handleRecordFlashcardAttempt,
                    onRefreshHome = controller::refreshHome,
                    onLogout = controller::handleLogout,
                    onFeatureRequest = ::showMessage
                )
            }
        }

        controller.bootstrap()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
