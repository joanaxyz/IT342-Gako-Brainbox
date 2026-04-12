package edu.cit.gako.brainbox.notebook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.home.buildChunkProgressLabel
import edu.cit.gako.brainbox.home.playbackProgressFraction
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream
import edu.cit.gako.brainbox.ui.theme.Cream2
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.ErrorRed
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White
import edu.cit.gako.brainbox.shared.BrainBoxPlaybar
import edu.cit.gako.brainbox.shared.BrainBoxPlaybarIconAction
import edu.cit.gako.brainbox.shared.BrainBoxPlaybarPillAction

@Composable
internal fun NotebookEditorTopBar(
    uiState: EditorUiState,
    isOverflowOpen: Boolean,
    onOverflowChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCategory: () -> Unit,
    onToggleReview: () -> Unit,
    onToggleAi: () -> Unit,
    onSave: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onHistory: () -> Unit,
    onDelete: () -> Unit
) {
    var isTitleEditing by remember(uiState.isReviewMode) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White.copy(alpha = 0.96f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotebookChromeIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                label = "Back",
                onClick = onBack
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (uiState.isReviewMode) "Review mode" else "Notebook editor",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink3
                )
                if (!uiState.isReviewMode && isTitleEditing) {
                    TextField(
                        value = uiState.title,
                        onValueChange = onTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = Ink,
                            fontWeight = FontWeight.SemiBold
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Accent
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { isTitleEditing = false })
                    )
                } else {
                    Text(
                        text = uiState.title.ifBlank { "Untitled notebook" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!uiState.isReviewMode) {
                NotebookChromeIconButton(
                    icon = Icons.Filled.Save,
                    label = "Save notebook",
                    onClick = onSave
                )
            }
            NotebookChromeIconButton(
                icon = Icons.Filled.AutoAwesome,
                label = if (uiState.showAiPanel) "Hide AI" else "Open AI",
                onClick = onToggleAi
            )
            NotebookChromeIconButton(
                icon = if (uiState.isReviewMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                label = if (uiState.isReviewMode) "Return to edit mode" else "Open review mode",
                onClick = onToggleReview
            )
            NotebookOverflowMenu(
                expanded = isOverflowOpen,
                onExpandedChange = onOverflowChange,
                isReviewMode = uiState.isReviewMode,
                isTitleEditing = isTitleEditing,
                onEditTitle = { isTitleEditing = !isTitleEditing },
                onEditCategory = onCategory,
                onImport = onImport,
                onExport = onExport,
                onHistory = onHistory,
                onDelete = onDelete
            )
        }

        if (uiState.isReviewMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 0.dp)
                    .padding(bottom = 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NotebookSaveStatePill(uiState.saveState)
            }
        }

        HorizontalDivider(color = Border)
    }
}

@Composable
internal fun NotebookNavigatorCard(
    headings: List<String>,
    isExpanded: Boolean,
    saveState: EditorSaveState,
    onExpandedChange: (Boolean) -> Unit,
    onSelectHeading: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else -90f,
        label = "navigatorCardChevron"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!isExpanded) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Navigator",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (headings.isEmpty()) {
                            "Add headings to build quick jumps around the note."
                        } else {
                            "${headings.size} headings ready for quick jumps."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink2
                    )
                }
                NotebookSaveStatePill(saveState)
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse navigator" else "Expand navigator",
                    tint = Ink2,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(chevronRotation)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = Border)
                    if (headings.isEmpty()) {
                        Text(
                            text = "No headings detected yet. Use headings in the note to populate the navigator.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink2
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            headings.forEachIndexed { index, heading ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectHeading(index) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Cream,
                                    border = BorderStroke(1.dp, Border)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(shape = RoundedCornerShape(999.dp), color = White) {
                                            Text(
                                                text = "${index + 1}",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Ink
                                            )
                                        }
                                        Text(
                                            text = heading,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Ink,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = null,
                                            tint = Ink3
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NotebookReviewPlaybackDock(
    title: String,
    playbackState: PlaybackUiState,
    onStartOrTogglePlayback: () -> Unit,
    onReplay: () -> Unit,
    onSpeed: () -> Unit,
    onOpenNavigator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Cream.copy(alpha = 0.96f))
    ) {
        HorizontalDivider(color = Border)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BrainBoxPlaybar(
                title = title.ifBlank { "Review audio" },
                supportingText = buildReviewPlaybarSummary(playbackState),
                eyebrow = "Review playback",
                progressText = buildChunkProgressLabel(playbackState),
                progress = playbackProgressFraction(playbackState),
                isPlaying = playbackState.isPlaying,
                onPrimaryAction = onStartOrTogglePlayback,
                artworkLabel = "RV",
                artworkBrush = SolidColor(AccentBg),
                artworkContentTint = Accent,
                leadingActions = {
                    BrainBoxPlaybarIconAction(
                        icon = Icons.Filled.Replay,
                        label = "Replay from current section",
                        onClick = onReplay,
                        enabled = playbackState.isVisible
                    )
                },
                trailingActions = {
                    BrainBoxPlaybarPillAction(
                        label = "${playbackState.speechRate}x",
                        onClick = onSpeed
                    )
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotebookCompactAction(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = "Navigator",
                    onClick = onOpenNavigator,
                    emphasized = playbackState.isPlaying || playbackState.canResume
                )
                if (playbackState.isOfflineOnly) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = White,
                        border = BorderStroke(1.dp, Border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                tint = Ink2,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "Offline voice",
                                style = MaterialTheme.typography.labelLarge,
                                color = Ink
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildReviewPlaybarSupportingText(playbackState: PlaybackUiState): String {
    val status = when {
        playbackState.isPlaying -> "Tracking transcript"
        playbackState.canResume -> "Paused in review"
        else -> "Ready for playback"
    }

    return buildString {
        append(status)
        append(" · ")
        append("${playbackState.speechRate}x")
        if (playbackState.isOfflineOnly) {
            append(" · Offline voice")
        }
    }
}

private fun buildReviewPlaybarSummary(playbackState: PlaybackUiState): String {
    val status = when {
        playbackState.isPlaying -> "Tracking transcript"
        playbackState.canResume -> "Paused in review"
        else -> "Ready for playback"
    }

    return buildString {
        append(status)
        append(" - ")
        append("${playbackState.speechRate}x")
        if (playbackState.isOfflineOnly) {
            append(" - Offline voice")
        }
    }
}

@Composable
internal fun NotebookAiOverlay(
    uiState: EditorUiState,
    selectedText: String,
    onDismiss: () -> Unit,
    onSelectTool: (String) -> Unit,
    onPromptChange: (String) -> Unit,
    onSendPrompt: () -> Unit,
    onSelectConfig: (Long) -> Unit,
    onNewChat: () -> Unit,
    onRestoreConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onAcceptProposal: () -> Unit,
    onRejectProposal: () -> Unit,
    onCreateQuiz: () -> Unit,
    onDismissPendingQuiz: () -> Unit,
    onViewQuiz: () -> Unit,
    onDismissCreatedQuiz: () -> Unit,
    onCreateFlashcards: () -> Unit,
    onDismissPendingFlashcards: () -> Unit,
    onViewFlashcards: () -> Unit,
    onDismissCreatedFlashcards: () -> Unit
) {
    val dismissInteraction = remember { MutableInteractionSource() }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isHistoryOpen by remember { mutableStateOf(false) }
    val toolSet = if (uiState.isReviewMode) reviewAiTools else editorAiTools
    val tools = toolSet.map { tool ->
        NotebookAiToolChipUi(
            id = tool.key,
            label = tool.label,
            selected = uiState.activeAiToolKey == tool.key,
            icon = notebookAiToolIcon(tool.key)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x611C1917))
            .clickable(
                interactionSource = dismissInteraction,
                indication = null,
                onClick = onDismiss
            )
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 5 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 5 }),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.96f),
                shape = RoundedCornerShape(28.dp),
                color = White,
                border = BorderStroke(1.dp, Border),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxHeight(),
                        color = Cream2,
                        border = BorderStroke(1.dp, Border)
                    ) {
                        NotebookAiToolRail(
                            tools = tools,
                            onToolClick = onSelectTool,
                            modifier = Modifier.width(60.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Border)
                    )
                    NotebookAiCard(
                        uiState = uiState,
                        selectedText = selectedText,
                        onSelectTool = onSelectTool,
                        onPromptChange = onPromptChange,
                        onSendPrompt = onSendPrompt,
                        onNewChat = onNewChat,
                        onOpenSettings = { isSettingsOpen = true },
                        onOpenHistory = { isHistoryOpen = true },
                        onDismiss = onDismiss,
                        onAcceptProposal = onAcceptProposal,
                        onRejectProposal = onRejectProposal,
                        onCreateQuiz = onCreateQuiz,
                        onDismissPendingQuiz = onDismissPendingQuiz,
                        onViewQuiz = onViewQuiz,
                        onDismissCreatedQuiz = onDismissCreatedQuiz,
                        onCreateFlashcards = onCreateFlashcards,
                        onDismissPendingFlashcards = onDismissPendingFlashcards,
                        onViewFlashcards = onViewFlashcards,
                        onDismissCreatedFlashcards = onDismissCreatedFlashcards
                    )
                }
            }
        }
    }

    if (isSettingsOpen) {
        NotebookAiSettingsSheet(
            uiState = uiState,
            onDismiss = { isSettingsOpen = false },
            onSelectConfig = onSelectConfig
        )
    }

    if (isHistoryOpen) {
        NotebookAiHistorySheet(
            uiState = uiState,
            onDismiss = { isHistoryOpen = false },
            onNewChat = onNewChat,
            onRestoreConversation = onRestoreConversation,
            onDeleteConversation = onDeleteConversation
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotebookNavigatorSheet(
    title: String,
    subtitle: String,
    headings: List<String>,
    onDismiss: () -> Unit,
    onSelectHeading: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = Ink)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Ink2)
            }

            if (headings.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Cream,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Text(
                        text = "No headings detected yet. Add headings in the note to build a navigator.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink2
                    )
                }
            } else {
                headings.forEachIndexed { index, heading ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectHeading(index)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = White,
                        border = BorderStroke(1.dp, Border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(999.dp), color = Cream2) {
                                Text(
                                    text = "${index + 1}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Ink
                                )
                            }
                            Text(
                                text = heading,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = Ink3
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Composable
private fun NotebookModeToggle(
    isReviewMode: Boolean,
    onToggleReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(3.dp)) {
            NotebookModeToggleButton(
                label = "Edit",
                selected = !isReviewMode,
                onClick = { if (isReviewMode) onToggleReview() },
                modifier = Modifier.weight(1f)
            )
            NotebookModeToggleButton(
                label = "Review",
                selected = isReviewMode,
                onClick = { if (!isReviewMode) onToggleReview() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RowScope.NotebookModeToggleButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        color = if (selected) Ink else Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) White else Ink2,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun NotebookPillAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    emphasized: Boolean = false
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (emphasized) AccentBg else White,
        border = BorderStroke(1.dp, if (emphasized) Accent.copy(alpha = 0.18f) else Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (emphasized) Accent else Ink2,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NotebookCompactAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    emphasized: Boolean = false
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (emphasized) Ink else White,
        border = BorderStroke(1.dp, if (emphasized) Ink else Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (emphasized) White else Ink2,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (emphasized) White else Ink
            )
        }
    }
}

@Composable
private fun NotebookChromeIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    rotation: Float = 0f
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Box(
            modifier = Modifier.padding(9.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Ink,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
private fun NotebookSaveStatePill(state: EditorSaveState) {
    val badge = when (state) {
        EditorSaveState.SAVED -> Triple("Saved", AccentBg, Accent)
        EditorSaveState.UNSAVED -> Triple("Unsaved", Cream2, Ink2)
        EditorSaveState.SAVING -> Triple("Saving", Cream2, Accent)
        EditorSaveState.ERROR -> Triple("Attention", Color(0xFFFEE2E2), ErrorRed)
    }

    Surface(shape = RoundedCornerShape(999.dp), color = badge.second) {
        Text(
            text = badge.first,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = badge.third
        )
    }
}

@Composable
private fun NotebookOverflowMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isReviewMode: Boolean,
    isTitleEditing: Boolean,
    onEditTitle: () -> Unit,
    onEditCategory: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onHistory: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        NotebookChromeIconButton(
            icon = Icons.Filled.MoreVert,
            label = "More notebook actions",
            onClick = { onExpandedChange(!expanded) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            if (!isReviewMode) {
                DropdownMenuItem(
                    text = {
                        Text(if (isTitleEditing) "Done editing title" else "Edit title")
                    },
                    onClick = {
                        onExpandedChange(false)
                        onEditTitle()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Edit category") },
                    onClick = {
                        onExpandedChange(false)
                        onEditCategory()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Import notebook") },
                    onClick = {
                        onExpandedChange(false)
                        onImport()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export notebook") },
                    onClick = {
                        onExpandedChange(false)
                        onExport()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Version history") },
                    onClick = {
                        onExpandedChange(false)
                        onHistory()
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Delete notebook") },
                onClick = {
                    onExpandedChange(false)
                    onDelete()
                }
            )
        }
    }
}
