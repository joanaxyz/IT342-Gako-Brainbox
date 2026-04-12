package edu.cit.gako.brainbox.notebook

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.ui.theme.Cream
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
internal fun NativeNotebookEditorScreen(
    notebookUuid: String,
    onClose: () -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appGraph = remember(context.applicationContext) {
        BrainBoxAppGraph.from(context.applicationContext)
    }
    val editorViewModel: NotebookEditorViewModel = viewModel(
        key = "nb-editor-$notebookUuid",
        factory = NotebookEditorViewModel.factory(
            notebookUuid = notebookUuid,
            appContext = context.applicationContext,
            repository = appGraph.repository,
            localInfrastructure = appGraph.localInfrastructure
        )
    )
    val playbackViewModel: NotebookPlaybackViewModel = viewModel(
        key = "nb-playback-$notebookUuid",
        factory = NotebookPlaybackViewModel.factory(
            appContext = context.applicationContext,
            audioStore = appGraph.audioStore
        )
    )

    val uiState by editorViewModel.uiState.collectAsState()
    val playbackState by playbackViewModel.playbackUiState.collectAsState()
    val reviewState by playbackViewModel.reviewUiState.collectAsState()
    val isOnline by rememberNetworkAvailable()
    val editorBridgeState = remember { mutableStateOf<NativeEditorBridge?>(null) }

    var isOverflowOpen by remember { mutableStateOf(false) }
    var isNavigatorSheetOpen by remember(notebookUuid) { mutableStateOf(false) }
    var isNavigatorExpanded by remember(notebookUuid) { mutableStateOf(true) }
    var wasReviewMode by remember(notebookUuid) { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = readDocumentText(context.applicationContext, uri)
                if (text.isBlank()) {
                    Toast.makeText(context, "The selected file was empty.", Toast.LENGTH_SHORT).show()
                } else {
                    editorViewModel.updateHtml(text.toImportedHtml())
                }
            }
        }
    }

    LaunchedEffect(uiState.resolvedNotebookUuid, uiState.contentHtml, uiState.title, uiState.categoryId, isOnline) {
        playbackViewModel.bindNotebook(
            notebook = uiState.toNotebookDetail(),
            html = uiState.contentHtml,
            offlineOnly = !isOnline
        )
    }

    LaunchedEffect(Unit) {
        editorViewModel.uiEvents.collectLatest { event ->
            when (event) {
                NotebookUiEvent.CloseEditor -> onClose()
                is NotebookUiEvent.ExportHtml -> shareNotebookHtml(context, event.title, event.html)
                is NotebookUiEvent.Message -> Toast.makeText(context, event.value, Toast.LENGTH_SHORT).show()
                is NotebookUiEvent.OpenQuiz -> onOpenQuiz(event.uuid)
                is NotebookUiEvent.OpenFlashcardDeck -> onOpenFlashcardDeck(event.uuid)
            }
        }
    }

    LaunchedEffect(uiState.isReviewMode) {
        if (wasReviewMode && !uiState.isReviewMode && playbackState.notebookId == uiState.toNotebookDetail()?.uuid) {
            playbackViewModel.stopPlayback()
        }
        wasReviewMode = uiState.isReviewMode
    }

    BackHandler(enabled = uiState.showAiPanel) {
        editorViewModel.toggleAiPanel()
    }
    BackHandler(onBack = editorViewModel::requestClose)

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
            else -> {
                NotebookEditorWorkspace(
                    uiState = uiState,
                    playbackState = playbackState,
                    reviewState = reviewState,
                    isOnline = isOnline,
                    isOverflowOpen = isOverflowOpen,
                    isNavigatorSheetOpen = isNavigatorSheetOpen,
                    isNavigatorExpanded = isNavigatorExpanded,
                    editorBridgeState = editorBridgeState,
                    onOverflowChange = { isOverflowOpen = it },
                    onOpenNavigator = { isNavigatorSheetOpen = true },
                    onDismissNavigator = { isNavigatorSheetOpen = false },
                    onNavigatorExpandedChange = { isNavigatorExpanded = it },
                    onBack = editorViewModel::requestClose,
                    onTitleChange = editorViewModel::updateTitle,
                    onSave = { editorViewModel.saveNotebook(isOnline) },
                    onToggleReview = editorViewModel::toggleReviewMode,
                    onToggleAi = editorViewModel::toggleAiPanel,
                    onCategory = { editorViewModel.requestCategorySheet(true) },
                    onImport = { importLauncher.launch(arrayOf("text/*", "text/html")) },
                    onExport = editorViewModel::requestExport,
                    onHistory = editorViewModel::requestHistory,
                    onDelete = { editorViewModel.requestDeleteDialog(true) },
                    onHtmlChange = editorViewModel::updateHtml,
                    onSeekToHeading = playbackViewModel::seekToChunk,
                    onStartOrTogglePlayback = {
                        handleNotebookPlay(uiState, playbackState, playbackViewModel, !isOnline)
                    },
                    onReplayChunk = {
                        playbackViewModel.seekToChunk(playbackState.currentChunkIndex.coerceAtLeast(0))
                    },
                    onAdvanceSpeechRate = {
                        playbackViewModel.setSpeechRate(nextSpeechRate(playbackState.speechRate))
                    },
                    onSelectEditSection = { index ->
                        uiState.outline.getOrNull(index)?.let { heading ->
                            editorBridgeState.value?.focusHeading(heading)
                        }
                    },
                    onSelectReviewSection = { index ->
                        reviewState.headingTargets.getOrNull(index)?.let { target ->
                            playbackViewModel.seekToChunk(target.chunkIndex)
                        }
                    }
                )
            }
        }

        if (uiState.showAiPanel) {
            NotebookAiOverlay(
                uiState = uiState,
                selectedText = if (uiState.isReviewMode) "" else editorBridgeState.value?.selectedText().orEmpty(),
                onDismiss = editorViewModel::toggleAiPanel,
                onSelectTool = editorViewModel::selectAiTool,
                onPromptChange = editorViewModel::updateAiPrompt,
                onSendPrompt = {
                    editorViewModel.submitAiPrompt(
                        if (uiState.isReviewMode) "" else editorBridgeState.value?.selectedText().orEmpty()
                    )
                },
                onSelectConfig = editorViewModel::selectAiConfig,
                onNewChat = editorViewModel::startNewAiChat,
                onRestoreConversation = editorViewModel::restoreAiConversation,
                onDeleteConversation = editorViewModel::deleteAiConversation,
                onAcceptProposal = editorViewModel::acceptAiProposal,
                onRejectProposal = editorViewModel::rejectAiProposal,
                onCreateQuiz = editorViewModel::createPendingQuiz,
                onDismissPendingQuiz = editorViewModel::dismissPendingQuiz,
                onViewQuiz = editorViewModel::openCreatedQuiz,
                onDismissCreatedQuiz = editorViewModel::dismissCreatedQuiz,
                onCreateFlashcards = editorViewModel::createPendingFlashcardDeck,
                onDismissPendingFlashcards = editorViewModel::dismissPendingFlashcards,
                onViewFlashcards = editorViewModel::openCreatedFlashcardDeck,
                onDismissCreatedFlashcards = editorViewModel::dismissCreatedFlashcardDeck
            )
        }

        NotebookEditorDialogs(
            uiState = uiState,
            onCloseCategorySheet = { editorViewModel.requestCategorySheet(false) },
            onSelectCategory = editorViewModel::selectCategory,
            onDismissHistory = editorViewModel::dismissHistory,
            onRestoreVersion = editorViewModel::restoreVersion,
            onDelete = { editorViewModel.deleteNotebook(isOnline) },
            onCloseDeleteDialog = { editorViewModel.requestDeleteDialog(false) },
            onDismissCloseDialog = editorViewModel::dismissCloseDialog,
            onSaveAndClose = { editorViewModel.saveNotebook(isOnline, closeAfterSave = true) },
            onDiscardAndClose = editorViewModel::discardChangesAndClose,
            onDismissConflict = editorViewModel::dismissConflict,
            onUseConflictedNotebook = editorViewModel::useConflictedNotebook
        )
    }
}

@Composable
private fun NotebookEditorWorkspace(
    uiState: EditorUiState,
    playbackState: PlaybackUiState,
    reviewState: ReviewUiState,
    isOnline: Boolean,
    isOverflowOpen: Boolean,
    isNavigatorSheetOpen: Boolean,
    isNavigatorExpanded: Boolean,
    editorBridgeState: androidx.compose.runtime.MutableState<NativeEditorBridge?>,
    onOverflowChange: (Boolean) -> Unit,
    onOpenNavigator: () -> Unit,
    onDismissNavigator: () -> Unit,
    onNavigatorExpandedChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onSave: () -> Unit,
    onToggleReview: () -> Unit,
    onToggleAi: () -> Unit,
    onCategory: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onHistory: () -> Unit,
    onDelete: () -> Unit,
    onHtmlChange: (String) -> Unit,
    onSeekToHeading: (Int) -> Unit,
    onStartOrTogglePlayback: () -> Unit,
    onReplayChunk: () -> Unit,
    onAdvanceSpeechRate: () -> Unit,
    onSelectEditSection: (Int) -> Unit,
    onSelectReviewSection: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            NotebookEditorTopBar(
                uiState = uiState,
                isOverflowOpen = isOverflowOpen,
                onOverflowChange = onOverflowChange,
                onBack = onBack,
                onTitleChange = onTitleChange,
                onCategory = onCategory,
                onToggleReview = onToggleReview,
                onToggleAi = onToggleAi,
                onSave = onSave,
                onImport = onImport,
                onExport = onExport,
                onHistory = onHistory,
                onDelete = onDelete
            )

            NotebookInlineNotice(
                uiState = uiState,
                isOnline = isOnline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )

            Crossfade(
                targetState = uiState.isReviewMode,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                label = "editorModeWorkspace"
            ) { isReviewMode ->
                if (isReviewMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .padding(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NotebookNavigatorCard(
                            headings = uiState.outline,
                            isExpanded = isNavigatorExpanded,
                            saveState = uiState.saveState,
                            onExpandedChange = onNavigatorExpandedChange,
                            onSelectHeading = onSelectEditSection,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            NativeRichTextEditorCard(
                                initialHtml = uiState.contentHtml,
                                bridgeState = editorBridgeState,
                                onHtmlChange = onHtmlChange,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isReviewMode) {
            NotebookReviewPlaybackDock(
                title = uiState.title,
                playbackState = playbackState,
                onStartOrTogglePlayback = onStartOrTogglePlayback,
                onReplay = onReplayChunk,
                onSpeed = onAdvanceSpeechRate,
                onOpenNavigator = onOpenNavigator,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }

        if (isNavigatorSheetOpen) {
            NotebookNavigatorSheet(
                title = "Review navigator",
                subtitle = "Jump through the same heading map used by playback on the web review surface.",
                headings = reviewState.headingTargets.map { it.title },
                onDismiss = onDismissNavigator,
                onSelectHeading = onSelectReviewSection
            )
        }
    }
}

private fun handleNotebookPlay(
    uiState: EditorUiState,
    playbackState: PlaybackUiState,
    playbackViewModel: NotebookPlaybackViewModel,
    offlineOnly: Boolean
) {
    val notebook = uiState.toNotebookDetail() ?: return
    if (playbackState.notebookId == notebook.uuid && playbackState.isVisible) {
        playbackViewModel.togglePlayback()
    } else {
        playbackViewModel.playNotebook(notebook, uiState.contentHtml, offlineOnly)
    }
}

private fun nextSpeechRate(current: Float): Float {
    return when (current) {
        in 0.0f..0.85f -> 1.0f
        in 0.86f..1.1f -> 1.25f
        in 1.11f..1.35f -> 1.5f
        else -> 0.75f
    }
}

@Composable
private fun rememberNetworkAvailable(): androidx.compose.runtime.State<Boolean> {
    val context = LocalContext.current.applicationContext
    val connectivityManager = remember(context) {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val onlineState = remember { mutableStateOf(connectivityManager.isCurrentlyOnline()) }

    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onlineState.value = true
            }

            override fun onLost(network: Network) {
                onlineState.value = connectivityManager.isCurrentlyOnline()
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                onlineState.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }

        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback
        )

        onDispose {
            runCatching {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
    }

    return onlineState
}

private fun ConnectivityManager.isCurrentlyOnline(): Boolean {
    val active = activeNetwork ?: return false
    val capabilities = getNetworkCapabilities(active) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
