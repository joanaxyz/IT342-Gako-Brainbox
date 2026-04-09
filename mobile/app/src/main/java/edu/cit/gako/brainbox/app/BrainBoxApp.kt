package edu.cit.gako.brainbox.app

import androidx.compose.runtime.Composable
import edu.cit.gako.brainbox.auth.AuthScene
import edu.cit.gako.brainbox.home.HomeScene
import edu.cit.gako.brainbox.study.FlashcardStudyScreen
import edu.cit.gako.brainbox.study.QuizStudyScreen

@Composable
fun BrainBoxApp(
    state: AppState,
    onLogin: (String, String) -> Unit,
    onGoogleLogin: (String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onSendResetCode: (String) -> Unit,
    onVerifyResetCode: (String) -> Unit,
    onResetPassword: (String) -> Unit,
    onAuthStageChange: (AuthStage) -> Unit,
    onTabSelected: (HomeTab) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onExitStudySession: () -> Unit,
    onRecordQuizAttempt: (String, Int) -> Unit,
    onRecordFlashcardAttempt: (String, Int) -> Unit,
    onRefreshHome: () -> Unit,
    onLogout: () -> Unit,
    onFeatureRequest: (String) -> Unit
) {
    when {
        state.isBootstrapping -> LoadingScreen()
        !state.isAuthenticated -> AuthScene(
            state = state,
            onLogin = onLogin,
            onGoogleLogin = onGoogleLogin,
            onRegister = onRegister,
            onSendResetCode = onSendResetCode,
            onVerifyResetCode = onVerifyResetCode,
            onResetPassword = onResetPassword,
            onAuthStageChange = onAuthStageChange,
            onFeatureRequest = onFeatureRequest
        )
        state.activeQuiz != null -> QuizStudyScreen(
            quiz = state.activeQuiz,
            onExit = onExitStudySession,
            onRecordAttempt = onRecordQuizAttempt
        )
        state.activeFlashcardDeck != null -> FlashcardStudyScreen(
            deck = state.activeFlashcardDeck,
            onExit = onExitStudySession,
            onRecordAttempt = onRecordFlashcardAttempt
        )
        else -> HomeScene(
            state = state,
            onTabSelected = onTabSelected,
            onOpenQuiz = onOpenQuiz,
            onOpenFlashcardDeck = onOpenFlashcardDeck,
            onRefreshHome = onRefreshHome,
            onLogout = onLogout,
            onFeatureRequest = onFeatureRequest
        )
    }
}

