package edu.cit.gako.brainbox.notebook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream2
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White

@Composable
internal fun NotebookAiCard(
    uiState: EditorUiState,
    selectedText: String,
    onSelectTool: (String) -> Unit,
    onPromptChange: (String) -> Unit,
    onSendPrompt: () -> Unit,
    onNewChat: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onDismiss: () -> Unit,
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
    val toolSet = if (uiState.isReviewMode) reviewAiTools else editorAiTools
    val activeTool = toolSet.firstOrNull { it.key == uiState.activeAiToolKey } ?: toolSet.first()
    val selectedConfig = uiState.aiConfigs.firstOrNull { it.id == uiState.selectedAiConfigId }
    val hasConversationContext = uiState.aiConversationTitle != "New chat" || uiState.aiMessages.size > 1

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = AccentBg) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        uiState.aiConversationTitle.ifBlank { "New chat" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        buildAiHeaderMeta(uiState.isReviewMode, selectedConfig?.name, selectedConfig?.model),
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                NotebookAiHeaderAction(
                    icon = Icons.Filled.History,
                    label = "Open chat history",
                    onClick = onOpenHistory
                )
                NotebookAiHeaderAction(
                    icon = Icons.Filled.Settings,
                    label = "Open AI settings",
                    onClick = onOpenSettings
                )
                NotebookAiHeaderAction(
                    icon = Icons.Filled.Add,
                    label = "Start new chat",
                    onClick = onNewChat
                )
                NotebookAiHeaderAction(
                    icon = Icons.Filled.Close,
                    label = "Close AI",
                    onClick = onDismiss
                )
            }
        }

        androidx.compose.material3.HorizontalDivider(color = Border)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (hasConversationContext) {
                NotebookAiConversationStrip(
                    title = uiState.aiConversationTitle.ifBlank { "New chat" },
                    configName = selectedConfig?.name,
                    configModel = selectedConfig?.model,
                    onNewChat = onNewChat
                )
            }

            NotebookAiActiveToolStrip(
                tool = activeTool,
                selectedText = selectedText,
                onSwitchToChat = { onSelectTool(NOTEBOOK_AI_TOOL_CHAT) }
            )

            NotebookAiMessageList(
                messages = uiState.aiMessages,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                emptyStateText = "Start a conversation with Notebook AI."
            )

            uiState.aiProposalState.takeIf { it.isVisible }?.let { proposal ->
                NotebookAiProposalCard(
                    proposal = NotebookAiProposalCardUi(
                        title = "Editor proposal ready",
                        summary = proposal.responseText.ifBlank { "Review the proposed note changes before saving them." },
                        proposedContentPreview = proposal.proposedContent.htmlToPreviewText(),
                        sourcePrompt = proposal.sourcePrompt,
                        footerLabel = "Accept to write the proposal into the editor."
                    ),
                    onAccept = onAcceptProposal,
                    onReject = onRejectProposal
                )
            }

            uiState.pendingQuizDraft?.let { draft ->
                StudyDraftCard(
                    title = draft.title.ifBlank { "Generated Quiz" },
                    summary = draft.description ?: "Quiz draft generated from this notebook.",
                    meta = "${draft.questions.size} questions",
                    previewLines = draft.questions.take(3).mapIndexed { index, question -> "${index + 1}. ${question.text}" },
                    primaryLabel = "Create quiz",
                    secondaryLabel = "Dismiss",
                    primaryIcon = Icons.Filled.Quiz,
                    onPrimary = onCreateQuiz,
                    onSecondary = onDismissPendingQuiz
                )
            }

            uiState.createdQuiz?.let { quiz ->
                StudyDraftCard(
                    title = quiz.title,
                    summary = quiz.description ?: "Quiz created and ready to open.",
                    meta = "${quiz.questionCount} questions",
                    previewLines = quiz.questions.take(3).mapIndexed { index, question -> "${index + 1}. ${question.text}" },
                    primaryLabel = "View quiz",
                    secondaryLabel = "Dismiss",
                    primaryIcon = Icons.Filled.OpenInNew,
                    onPrimary = onViewQuiz,
                    onSecondary = onDismissCreatedQuiz
                )
            }

            uiState.pendingFlashcardDraft?.let { draft ->
                StudyDraftCard(
                    title = draft.title.ifBlank { "Generated Flashcards" },
                    summary = draft.description ?: "Flashcard deck draft generated from this notebook.",
                    meta = "${draft.cards.size} cards",
                    previewLines = draft.cards.take(3).mapIndexed { index, card -> "${index + 1}. ${card.front} -> ${card.back}" },
                    primaryLabel = "Create deck",
                    secondaryLabel = "Dismiss",
                    primaryIcon = Icons.Filled.MenuBook,
                    onPrimary = onCreateFlashcards,
                    onSecondary = onDismissPendingFlashcards
                )
            }

            uiState.createdFlashcardDeck?.let { deck ->
                StudyDraftCard(
                    title = deck.title,
                    summary = deck.description ?: "Flashcard deck created and ready to open.",
                    meta = "${deck.cardCount} cards",
                    previewLines = deck.cards.take(3).mapIndexed { index, card -> "${index + 1}. ${card.front} -> ${card.back}" },
                    primaryLabel = "View deck",
                    secondaryLabel = "Dismiss",
                    primaryIcon = Icons.Filled.OpenInNew,
                    onPrimary = onViewFlashcards,
                    onSecondary = onDismissCreatedFlashcards
                )
            }

            NotebookAiPromptComposer(
                prompt = uiState.aiPrompt,
                onPromptChange = onPromptChange,
                onSend = { onSendPrompt() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isAiWorking && uiState.aiConfigs.isNotEmpty(),
                placeholder = if (activeTool.key == NOTEBOOK_AI_TOOL_CHAT) {
                    if (uiState.isReviewMode) "Ask a review follow-up..." else "Ask Notebook AI anything about this note..."
                } else {
                    "Run ${activeTool.label.lowercase()} or add a follow-up..."
                },
                sendLabel = if (activeTool.key == NOTEBOOK_AI_TOOL_CHAT) "Send" else "Run",
                supportingText = if (uiState.aiConfigs.isEmpty()) {
                    "Add an AI configuration in settings before sending a prompt."
                } else if (uiState.isAiWorking) {
                    "Notebook AI is responding..."
                } else {
                    activeTool.description
                }
            )
        }
    }
}

@Composable
internal fun NotebookAiToolRail(
    tools: List<NotebookAiToolChipUi>,
    onToolClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 10.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        tools.forEach { tool ->
            Surface(
                modifier = Modifier.clickable(enabled = tool.enabled) { onToolClick(tool.id) },
                shape = RoundedCornerShape(16.dp),
                color = if (tool.selected) AccentBg else White,
                border = BorderStroke(1.dp, if (tool.selected) Accent else Border)
            ) {
                Icon(
                    imageVector = tool.icon ?: Icons.Filled.AutoAwesome,
                    contentDescription = tool.label,
                    tint = if (tool.selected) Accent else Ink2,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(18.dp)
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun NotebookAiSettingsSheet(
    uiState: EditorUiState,
    onDismiss: () -> Unit,
    onSelectConfig: (Long) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("AI settings", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(
                "Choose the configuration used by this mobile AI overlay. Full configuration editing still lives on the web.",
                style = MaterialTheme.typography.bodySmall,
                color = Ink3
            )

            if (uiState.aiConfigs.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Cream2,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Text(
                        "No AI configuration found yet. Add one on the web, then return here.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink2
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.aiConfigs.forEach { config ->
                        val isSelected = uiState.selectedAiConfigId == config.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectConfig(config.id)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) AccentBg else White,
                            border = BorderStroke(1.dp, if (isSelected) Accent else Border)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Accent.copy(alpha = 0.12f) else Cream2
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isSelected) Accent else Ink2,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(config.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                                    Text(config.model, style = MaterialTheme.typography.bodySmall, color = Ink3)
                                }
                                if (isSelected) {
                                    Text("Active", style = MaterialTheme.typography.labelMedium, color = Accent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun NotebookAiHistorySheet(
    uiState: EditorUiState,
    onDismiss: () -> Unit,
    onNewChat: () -> Unit,
    onRestoreConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val normalizedQuery = searchQuery.trim()
    val filteredHistory = uiState.aiConversationHistory.filter { item ->
        normalizedQuery.isBlank() ||
            item.title.contains(normalizedQuery, ignoreCase = true) ||
            item.preview.contains(normalizedQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Chat history", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text(
                        "Recent conversations from this editor session.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                }
                TextButton(onClick = {
                    onNewChat()
                    onDismiss()
                }) {
                    Text("New chat")
                }
            }

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search conversations") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Cream2,
                    unfocusedContainerColor = Cream2,
                    disabledContainerColor = Cream2,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )

            if (filteredHistory.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Cream2,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Text(
                        "No stored conversations yet. Start a new chat once and it will appear here.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink2
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredHistory.forEach { conversation ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onRestoreConversation(conversation.id)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = White,
                            border = BorderStroke(1.dp, Border)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(12.dp), color = Cream2) {
                                    Icon(
                                        imageVector = Icons.Filled.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = Ink2,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        conversation.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        conversation.preview,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Ink3,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${conversation.messageCount} messages",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Ink3
                                    )
                                }
                                IconButton(onClick = { onDeleteConversation(conversation.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete conversation",
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

private fun buildAiHeaderMeta(reviewMode: Boolean, configName: String?, configModel: String?): String {
    val modeLabel = if (reviewMode) "Review AI" else "Notebook AI"
    return listOfNotNull(modeLabel, configName, configModel).joinToString(" • ")
}

@Composable
private fun NotebookAiHeaderAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Ink2
        )
    }
}

@Composable
private fun NotebookAiConversationStrip(
    title: String,
    configName: String?,
    configModel: String?,
    onNewChat: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Cream2,
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "Continuing $title",
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                listOfNotNull(configName, configModel).takeIf { it.isNotEmpty() }?.let { details ->
                    Text(
                        details.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            TextButton(onClick = onNewChat) {
                Text("New chat")
            }
        }
    }
}

@Composable
private fun NotebookAiActiveToolStrip(
    tool: NotebookAiToolDefinition,
    selectedText: String,
    onSwitchToChat: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(12.dp), color = AccentBg) {
                        Icon(
                            imageVector = notebookAiToolIcon(tool.key),
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(tool.label, style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text(
                            if (selectedText.isBlank()) "Whole note" else "Using selection",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink3
                        )
                    }
                }
                if (tool.key != NOTEBOOK_AI_TOOL_CHAT) {
                    TextButton(onClick = onSwitchToChat) {
                        Text("Back to chat")
                    }
                }
            }
            Text(tool.description, style = MaterialTheme.typography.bodySmall, color = Ink2)
        }
    }
}

internal fun notebookAiToolIcon(toolKey: String): ImageVector {
    return when (toolKey) {
        NOTEBOOK_AI_TOOL_CHAT -> Icons.Filled.ChatBubbleOutline
        NOTEBOOK_AI_TOOL_IMPROVE -> Icons.Filled.AutoFixHigh
        NOTEBOOK_AI_TOOL_EXPAND -> Icons.Filled.OpenInFull
        NOTEBOOK_AI_TOOL_SUMMARIZE -> Icons.Filled.Subject
        NOTEBOOK_AI_TOOL_EXPLAIN -> Icons.Filled.Lightbulb
        NOTEBOOK_AI_TOOL_QUIZ -> Icons.Filled.Quiz
        NOTEBOOK_AI_TOOL_FLASHCARDS -> Icons.Filled.MenuBook
        NOTEBOOK_AI_TOOL_STUDY_GUIDE -> Icons.Filled.MenuBook
        else -> Icons.Filled.Edit
    }
}

@Composable
private fun StudyDraftCard(
    title: String,
    summary: String,
    meta: String,
    previewLines: List<String>,
    primaryLabel: String,
    secondaryLabel: String,
    primaryIcon: ImageVector,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
                    Text(summary, style = MaterialTheme.typography.bodySmall, color = Ink2)
                }
                Surface(shape = RoundedCornerShape(999.dp), color = AccentBg) {
                    Text(meta, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = Accent)
                }
            }

            previewLines.forEach { line ->
                Text(line, style = MaterialTheme.typography.bodySmall, color = Ink3)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onPrimary, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White)) {
                    Icon(primaryIcon, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(primaryLabel)
                }
                OutlinedButton(onClick = onSecondary) {
                    Text(secondaryLabel)
                }
            }
        }
    }
}
