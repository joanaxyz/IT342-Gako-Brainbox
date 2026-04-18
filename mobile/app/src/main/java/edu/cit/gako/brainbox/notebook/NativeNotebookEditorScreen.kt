package edu.cit.gako.brainbox.notebook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import edu.cit.gako.brainbox.ui.theme.Cream

@Composable
internal fun NativeNotebookEditorScreen(
    notebookUuid: String,
    onClose: () -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit
) {
    val context = LocalContext.current
    val appGraph = remember(context.applicationContext) {
        BrainBoxAppGraph.from(context.applicationContext)
    }
    val connectivitySnapshot by appGraph.localInfrastructure.connectivityMonitor.state.collectAsState(
        initial = appGraph.localInfrastructure.connectivityMonitor.currentState()
    )
    val cachedNotebook by remember(notebookUuid) {
        if (notebookUuid == "new") {
            kotlinx.coroutines.flow.flowOf(null)
        } else {
            appGraph.localInfrastructure.offlineRepository.observeNotebook(notebookUuid)
        }
    }.collectAsState(initial = null)
    val hasLocalCopy = cachedNotebook != null
    val canAttemptRemote = notebookUuid == "new" || connectivitySnapshot.isConnected
    var webErrorMessage by rememberSaveable(notebookUuid) { mutableStateOf<String?>(null) }
    var showOfflineCopy by rememberSaveable(notebookUuid) { mutableStateOf(false) }
    var webInstanceKey by rememberSaveable(notebookUuid) { mutableStateOf(0) }

    val shouldUseOfflineViewer = notebookUuid != "new" && (showOfflineCopy || (!canAttemptRemote && hasLocalCopy))

    when {
        shouldUseOfflineViewer -> {
            OfflineNotebookViewer(
                notebookUuid = notebookUuid,
                onClose = onClose
            )
        }

        !canAttemptRemote -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Cream)
                    .statusBarsPadding()
                    .padding(24.dp)
            ) {
                NotebookEditorError(
                    message = if (hasLocalCopy) {
                        "You're offline. Retry when you're back online or open the cached copy."
                    } else {
                        "Connect to the internet to load this notebook."
                    },
                    onRetry = {
                        webErrorMessage = null
                        webInstanceKey += 1
                    },
                    onClose = onClose,
                    secondaryActionLabel = if (hasLocalCopy) "Open cached copy" else "Close",
                    onSecondaryAction = if (hasLocalCopy) {
                        { showOfflineCopy = true }
                    } else {
                        null
                    }
                )
            }
        }

        !webErrorMessage.isNullOrBlank() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Cream)
                    .statusBarsPadding()
                    .padding(24.dp)
            ) {
                NotebookEditorError(
                    message = webErrorMessage.orEmpty(),
                    onRetry = {
                        webErrorMessage = null
                        showOfflineCopy = false
                        webInstanceKey += 1
                    },
                    onClose = onClose,
                    secondaryActionLabel = if (hasLocalCopy) "Open cached copy" else "Close",
                    onSecondaryAction = if (hasLocalCopy) {
                        { showOfflineCopy = true }
                    } else {
                        null
                    }
                )
            }
        }

        else -> key(webInstanceKey) {
            WebViewNotebookEditor(
                notebookUuid = notebookUuid,
                onClose = onClose,
                onOpenQuiz = onOpenQuiz,
                onOpenFlashcardDeck = onOpenFlashcardDeck,
                onWebError = { message ->
                    if (!connectivitySnapshot.isConnected && hasLocalCopy) {
                        showOfflineCopy = true
                    } else {
                        webErrorMessage = message.ifBlank {
                            "We couldn't load the live notebook editor right now."
                        }
                    }
                }
            )
        }
    }
}
