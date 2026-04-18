package edu.cit.gako.brainbox.notebook

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.shared.SyncNoticeBanner
import edu.cit.gako.brainbox.ui.theme.Cream
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White

@Composable
internal fun OfflineNotebookViewer(
    notebookUuid: String,
    onClose: () -> Unit
) {
    if (notebookUuid == "new") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            NotebookEditorError(
                message = "Connect to the internet to create a new notebook.",
                onRetry = {},
                onClose = onClose
            )
        }
        return
    }

    val context = LocalContext.current
    val appGraph = remember(context.applicationContext) {
        BrainBoxAppGraph.from(context.applicationContext)
    }

    val editorViewModel: NotebookEditorViewModel = viewModel(
        key = "nb-offline-$notebookUuid",
        factory = NotebookEditorViewModel.factory(
            notebookUuid = notebookUuid,
            appContext = context.applicationContext,
            repository = appGraph.repository,
            localInfrastructure = appGraph.localInfrastructure
        )
    )
    val playbackViewModel: NotebookPlaybackViewModel = viewModel(
        key = "nb-offline-playback-$notebookUuid",
        factory = NotebookPlaybackViewModel.factory(
            appContext = context.applicationContext,
            audioStore = appGraph.audioStore
        )
    )

    val uiState by editorViewModel.uiState.collectAsState()
    val playbackState by playbackViewModel.playbackUiState.collectAsState()
    val reviewState by playbackViewModel.reviewUiState.collectAsState()

    LaunchedEffect(uiState.resolvedNotebookUuid, uiState.contentHtml, uiState.title) {
        playbackViewModel.bindNotebook(
            notebook = uiState.toNotebookDetail(),
            html = uiState.contentHtml,
            offlineOnly = true
        )
    }

    BackHandler(onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
    ) {
        when {
            uiState.isLoading -> LoadingNotebookEditor()
            !uiState.fatalErrorMessage.isNullOrBlank() -> NotebookEditorError(
                message = uiState.fatalErrorMessage.orEmpty(),
                onRetry = editorViewModel::loadNotebook,
                onClose = onClose
            )
            else -> OfflineViewerWorkspace(
                uiState = uiState,
                playbackState = playbackState,
                reviewState = reviewState,
                onBack = onClose,
                onStartOrTogglePlayback = {
                    val notebook = uiState.toNotebookDetail() ?: return@OfflineViewerWorkspace
                    if (playbackState.notebookId == notebook.uuid && playbackState.isVisible) {
                        playbackViewModel.togglePlayback()
                    } else {
                        playbackViewModel.playNotebook(notebook, uiState.contentHtml, offlineOnly = true)
                    }
                },
                onReplayChunk = {
                    playbackViewModel.seekToChunk(playbackState.currentChunkIndex.coerceAtLeast(0))
                },
                onAdvanceSpeechRate = {
                    playbackViewModel.setSpeechRate(offlineNextSpeechRate(playbackState.speechRate))
                },
                onSeekToHeading = playbackViewModel::seekToChunk
            )
        }
    }
}

@Composable
private fun OfflineViewerWorkspace(
    uiState: EditorUiState,
    playbackState: PlaybackUiState,
    reviewState: ReviewUiState,
    onBack: () -> Unit,
    onStartOrTogglePlayback: () -> Unit,
    onReplayChunk: () -> Unit,
    onAdvanceSpeechRate: () -> Unit,
    onSeekToHeading: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OfflineViewerTopBar(title = uiState.title, onBack = onBack)

            SyncNoticeBanner(
                message = "Read only — editing is unavailable offline.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(bottom = 154.dp)
            ) {
                NotebookReviewModePane(
                    title = uiState.title,
                    playbackState = playbackState,
                    reviewState = reviewState,
                    onSeekToHeading = onSeekToHeading,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        NotebookReviewPlaybackDock(
            title = uiState.title,
            playbackState = playbackState,
            onStartOrTogglePlayback = onStartOrTogglePlayback,
            onReplay = onReplayChunk,
            onSpeed = onAdvanceSpeechRate,
            onOpenNavigator = {},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun OfflineViewerTopBar(title: String, onBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink
                )
            }
            Text(
                text = title.ifBlank { "Untitled notebook" },
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Offline",
                style = MaterialTheme.typography.labelMedium,
                color = Ink3,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

private fun offlineNextSpeechRate(current: Float): Float {
    return when (current) {
        in 0.0f..0.85f -> 1.0f
        in 0.86f..1.1f -> 1.25f
        in 1.11f..1.35f -> 1.5f
        else -> 0.75f
    }
}
