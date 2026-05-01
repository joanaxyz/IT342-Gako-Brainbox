package edu.cit.gako.brainbox.features.notebook.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.shared.ui.theme.Cream

@Composable
internal fun NotebookEditorScreen(
    notebookUuid: String,
    onClose: () -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit
) {
    var webErrorMessage by rememberSaveable(notebookUuid) { mutableStateOf<String?>(null) }
    var webInstanceKey by rememberSaveable(notebookUuid) { mutableStateOf(0) }

    when {
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
                        webInstanceKey += 1
                    },
                    onClose = onClose
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
                    webErrorMessage = message.ifBlank {
                        "We couldn't load the notebook editor right now."
                    }
                }
            )
        }
    }
}
