package edu.cit.gako.brainbox.app

import androidx.compose.runtime.Composable
import edu.cit.gako.brainbox.auth.AuthScene
import edu.cit.gako.brainbox.home.HomeScene
import edu.cit.gako.brainbox.notebook.NativeNotebookEditorScreen
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
    onCreateNotebook: () -> Unit,
    onOpenNotebook: (String) -> Unit,
    onCloseNotebookEditor: () -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenQuizFromNotebook: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onOpenFlashcardDeckFromNotebook: (String) -> Unit,
    onExitStudySession: () -> Unit,
    onRecordQuizAttempt: (String, Int) -> Unit,
    onRecordFlashcardAttempt: (String, Int) -> Unit,
    onLogout: () -> Unit,
    onFeatureRequest: (String) -> Unit,
    onAddToQueue: (edu.cit.gako.brainbox.network.models.NotebookSummary) -> Unit = {},
    onRemoveFromQueue: (String) -> Unit = {},
    onSkipNext: () -> Unit = {}
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
        state.activeNotebookUuid != null -> NativeNotebookEditorScreen(
            notebookUuid = state.activeNotebookUuid,
            onClose = onCloseNotebookEditor,
            onOpenQuiz = onOpenQuizFromNotebook,
            onOpenFlashcardDeck = onOpenFlashcardDeckFromNotebook
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
            onCreateNotebook = onCreateNotebook,
            onOpenNotebook = onOpenNotebook,
            onOpenQuiz = onOpenQuiz,
            onOpenFlashcardDeck = onOpenFlashcardDeck,
            onLogout = onLogout,
            onFeatureRequest = onFeatureRequest,
            onAddToQueue = onAddToQueue,
            onRemoveFromQueue = onRemoveFromQueue,
            onSkipNext = onSkipNext
        )
    }
}

