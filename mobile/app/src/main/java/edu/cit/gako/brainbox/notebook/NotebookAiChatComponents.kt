package edu.cit.gako.brainbox.notebook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.rememberLazyListState

enum class NotebookAiMessageRole {
    User,
    Assistant,
    Tool,
    System
}

data class NotebookAiMessageUi(
    val id: String,
    val role: NotebookAiMessageRole,
    val text: String,
    val timestampLabel: String? = null,
    val isPending: Boolean = false,
    val leadingLabel: String? = null
)

data class NotebookAiToolChipUi(
    val id: String,
    val label: String,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val icon: ImageVector? = null
)

data class NotebookAiProposalCardUi(
    val title: String,
    val summary: String,
    val proposedContentPreview: String,
    val sourcePrompt: String? = null,
    val footerLabel: String? = null
)

data class NotebookAiCreatedContentCardUi(
    val title: String,
    val createdContentPreview: String,
    val summary: String? = null,
    val footerLabel: String? = null
)

@Composable
fun NotebookAiMessageList(
    messages: List<NotebookAiMessageUi>,
    modifier: Modifier = Modifier,
    emptyStateText: String = "Start a conversation with Notebook AI.",
    autoScrollToBottom: Boolean = true
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, autoScrollToBottom) {
        if (autoScrollToBottom && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (messages.isEmpty()) {
            item {
                EmptyChatState(text = emptyStateText)
            }
        } else {
            itemsIndexed(
                items = messages,
                key = { _, message -> message.id }
            ) { _, message ->
                NotebookAiMessageBubble(message = message)
            }
        }
    }
}

@Composable
fun NotebookAiToolChipsRow(
    tools: List<NotebookAiToolChipUi>,
    onToolClick: (NotebookAiToolChipUi) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = tools,
            key = { _, tool -> tool.id }
        ) { _, tool ->
            val chipColors = AssistChipDefaults.assistChipColors(
                containerColor = if (tool.selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                labelColor = if (tool.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )

            AssistChip(
                onClick = { if (tool.enabled) onToolClick(tool) },
                enabled = tool.enabled,
                label = {
                    Text(
                        text = tool.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = tool.icon?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                },
                colors = chipColors,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (tool.selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

@Composable
fun NotebookAiPromptComposer(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Ask Notebook AI",
    sendLabel: String = "Send",
    supportingText: String? = null,
    trailingAction: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextField(
                value = prompt,
                onValueChange = onPromptChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                placeholder = { Text(placeholder) },
                minLines = 2,
                maxLines = 6,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (enabled && prompt.isNotBlank()) onSend(prompt)
                    }
                ),
                trailingIcon = trailingAction?.let { { it() } }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!supportingText.isNullOrBlank()) {
                    Text(
                        text = supportingText,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Button(
                    onClick = { if (prompt.isNotBlank()) onSend(prompt) },
                    enabled = enabled && prompt.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(sendLabel)
                }
            }
        }
    }
}

@Composable
fun NotebookAiProposalCard(
    proposal: NotebookAiProposalCardUi,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    NotebookAiContentCard(
        modifier = modifier,
        title = proposal.title,
        icon = Icons.Filled.Lightbulb,
        accent = MaterialTheme.colorScheme.primary,
        footerLabel = proposal.footerLabel,
        body = {
            Text(
                text = proposal.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = proposal.proposedContentPreview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            proposal.sourcePrompt?.takeIf { it.isNotBlank() }?.let { source ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = source,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept) {
                    Text("Accept")
                }
                OutlinedButton(onClick = onReject) {
                    Text("Reject")
                }
            }
        }
    )
}

@Composable
fun NotebookAiCreatedContentCard(
    content: NotebookAiCreatedContentCardUi,
    onCopy: () -> Unit,
    onInsert: () -> Unit,
    modifier: Modifier = Modifier
) {
    NotebookAiContentCard(
        modifier = modifier,
        title = content.title,
        icon = Icons.Filled.SmartToy,
        accent = MaterialTheme.colorScheme.tertiary,
        footerLabel = content.footerLabel,
        body = {
            content.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = content.createdContentPreview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 260.dp)
                        .padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = Int.MAX_VALUE
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy")
                }
                Button(onClick = onInsert) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Insert")
                }
            }
        }
    )
}

@Composable
private fun NotebookAiMessageBubble(
    message: NotebookAiMessageUi,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == NotebookAiMessageRole.User
    val bubbleColor = when (message.role) {
        NotebookAiMessageRole.User -> MaterialTheme.colorScheme.primary
        NotebookAiMessageRole.Assistant -> MaterialTheme.colorScheme.surface
        NotebookAiMessageRole.Tool -> MaterialTheme.colorScheme.surfaceVariant
        NotebookAiMessageRole.System -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (message.role) {
        NotebookAiMessageRole.User -> MaterialTheme.colorScheme.onPrimary
        NotebookAiMessageRole.Assistant -> MaterialTheme.colorScheme.onSurface
        NotebookAiMessageRole.Tool -> MaterialTheme.colorScheme.onSurfaceVariant
        NotebookAiMessageRole.System -> MaterialTheme.colorScheme.onErrorContainer
    }
    val bubbleBorder = when (message.role) {
        NotebookAiMessageRole.User -> null
        NotebookAiMessageRole.Assistant -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        NotebookAiMessageRole.Tool -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        NotebookAiMessageRole.System -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.24f))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(vertical = 1.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 8.dp,
                bottomEnd = if (isUser) 8.dp else 20.dp
            ),
            color = bubbleColor,
            border = bubbleBorder
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!message.leadingLabel.isNullOrBlank()) {
                    Text(
                        text = message.leadingLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor.copy(alpha = 0.82f)
                    )
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )

                if (message.isPending || !message.timestampLabel.isNullOrBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (message.isPending) {
                            Text(
                                text = "Working",
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor.copy(alpha = 0.72f)
                            )
                        }

                        message.timestampLabel?.takeIf { it.isNotBlank() }?.let { timestamp ->
                            Text(
                                text = timestamp,
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor.copy(alpha = 0.72f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.WorkOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotebookAiContentCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    body: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    footerLabel: String? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = accent.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    footerLabel?.takeIf { it.isNotBlank() }?.let { footer ->
                        Text(
                            text = footer,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                body()
            }
        }
    }
}
