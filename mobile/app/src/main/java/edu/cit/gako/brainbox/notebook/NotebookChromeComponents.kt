package edu.cit.gako.brainbox.notebook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.network.models.CategoryDetail
import edu.cit.gako.brainbox.network.models.NotebookVersionItem
import edu.cit.gako.brainbox.shared.SyncNoticeBanner
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.ErrorRed
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NotebookHeader(
    uiState: EditorUiState,
    playbackState: PlaybackUiState,
    isOnline: Boolean,
    isOverflowOpen: Boolean,
    onOverflowChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onSave: () -> Unit,
    onPlay: () -> Unit,
    onToggleReview: () -> Unit,
    onToggleAi: () -> Unit,
    onCategory: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onHistory: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
                }
                SaveBadge(uiState.saveState)
                Spacer(modifier = Modifier.weight(1f))
                SmallChromeButton(onPlay, if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (playbackState.isPlaying) "Pause" else "Play")
                SmallChromeButton(onToggleReview, if (uiState.isReviewMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, if (uiState.isReviewMode) "Edit" else "Review")
                SmallChromeButton(onToggleAi, Icons.Filled.AutoAwesome, if (uiState.showAiPanel) "Hide AI" else "AI")
                BoxedOverflowMenu(expanded = isOverflowOpen, onExpandedChange = onOverflowChange, onDelete = onDelete)
            }

            if (uiState.isReviewMode) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(uiState.title, style = MaterialTheme.typography.headlineSmall, color = Ink)
                    Text(
                        "Review mode locks the note content and turns the screen into a study surface with tracked playback.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink2
                    )
                }
            } else {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notebook title") },
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onCategory,
                    label = { Text(uiState.categoryName ?: "Choose category") },
                    leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null) }
                )
                Text(if (isOnline) "Online" else "Offline", style = MaterialTheme.typography.labelMedium, color = if (isOnline) Accent else Ink3)
            }

            if (!uiState.isReviewMode) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeaderActionChip(onSave, Icons.Filled.Save, if (uiState.saveState == EditorSaveState.SAVED) "Saved" else "Save")
                    HeaderActionChip(onImport, Icons.Filled.Upload, "Import")
                    HeaderActionChip(onExport, Icons.Filled.Download, "Export")
                    HeaderActionChip(onHistory, Icons.Filled.History, "History")
                }
            }
        }
    }
}

@Composable
internal fun NotebookInlineNotice(
    uiState: EditorUiState,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    when {
        !uiState.inlineMessage.isNullOrBlank() -> SyncNoticeBanner(
            message = uiState.inlineMessage,
            meta = if (isOnline) null else "Changes will sync when you're back online.",
            modifier = modifier
        )
        !isOnline -> SyncNoticeBanner(
            message = "Offline mode is active. Notebook edits and study progress are queued for sync.",
            modifier = modifier
        )
    }
}

@Composable
internal fun NotebookOutlineCard(outline: List<String>) {
    CardBox("Notebook Outline", "Tap review mode to use these sections as a reading navigator.") {
        outline.forEachIndexed { index, item ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(999.dp), color = AccentBg) {
                    Text("${index + 1}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = Accent)
                }
                Text(item, style = MaterialTheme.typography.bodyMedium, color = Ink2)
            }
        }
    }
}

@Composable
internal fun NotebookBottomPlaybar(
    playbackState: PlaybackUiState,
    onTogglePlayback: () -> Unit,
    onReplay: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        color = White,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Border),
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onTogglePlayback) {
                Icon(if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null, tint = Accent)
            }
            IconButton(onClick = onReplay, enabled = playbackState.isVisible) {
                Icon(Icons.Filled.Replay, contentDescription = null, tint = Ink2)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(playbackState.notebookTitle.ifBlank { "Notebook audio" }, style = MaterialTheme.typography.titleMedium, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${if (playbackState.isPlaying) "Playing" else "Paused"} | ${playbackState.currentChunkIndex + 1}/${playbackState.totalChunks.coerceAtLeast(1)} | ${playbackState.speechRate}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink2
                )
            }
            IconButton(onClick = onStop) {
                Icon(Icons.Filled.Stop, contentDescription = null, tint = Ink2)
            }
        }
    }
}

@Composable
internal fun NotebookEditorDialogs(
    uiState: EditorUiState,
    onCloseCategorySheet: () -> Unit,
    onSelectCategory: (CategoryDetail?) -> Unit,
    onDismissHistory: () -> Unit,
    onRestoreVersion: (NotebookVersionItem) -> Unit,
    onDelete: () -> Unit,
    onCloseDeleteDialog: () -> Unit,
    onDismissCloseDialog: () -> Unit,
    onSaveAndClose: () -> Unit,
    onDiscardAndClose: () -> Unit,
    onDismissConflict: () -> Unit,
    onUseConflictedNotebook: () -> Unit
) {
    if (uiState.showCategorySheet) CategoryPickerDialog(uiState.categories, uiState.categoryId, onCloseCategorySheet, onSelectCategory)
    if (uiState.isHistoryVisible) VersionHistoryDialog(uiState.versions, onDismissHistory, onRestoreVersion)
    if (uiState.showDeleteDialog) DeleteNotebookDialog(onDelete, onCloseDeleteDialog)
    if (uiState.showExitConfirmDialog) ExitConfirmDialog(onDismissCloseDialog, onSaveAndClose, onDiscardAndClose)
    uiState.conflictedNotebook?.let { ConflictDialog(onDismissConflict, onUseConflictedNotebook) }
}

@Composable
internal fun LoadingNotebookEditor() {
    Surface(modifier = Modifier.padding(top = 120.dp, start = 24.dp, end = 24.dp), shape = RoundedCornerShape(24.dp), color = White, border = BorderStroke(1.dp, Border)) {
        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(color = Accent)
            Text("Loading notebook", style = MaterialTheme.typography.titleLarge, color = Ink)
        }
    }
}

@Composable
internal fun NotebookEditorError(message: String, onRetry: () -> Unit, onClose: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(top = 120.dp, start = 24.dp, end = 24.dp), shape = RoundedCornerShape(24.dp), color = White, border = BorderStroke(1.dp, Border)) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Editor unavailable", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Ink2)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onRetry, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White)) { Text("Retry") }
                OutlinedButton(onClose) { Text("Close") }
            }
        }
    }
}

@Composable
internal fun CardBox(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), color = White, border = BorderStroke(1.dp, Border)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = Ink)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Ink3) }
            }
            content()
        }
    }
}

@Composable
private fun SmallChromeButton(onClick: () -> Unit, icon: ImageVector, label: String) {
    Surface(modifier = Modifier.padding(start = 4.dp).clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), color = AccentBg, border = BorderStroke(1.dp, Border)) {
        Icon(icon, contentDescription = label, modifier = Modifier.padding(10.dp), tint = Accent)
    }
}

@Composable
private fun HeaderActionChip(onClick: () -> Unit, icon: ImageVector, label: String) {
    Surface(modifier = Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), color = AccentBg, border = BorderStroke(1.dp, Border)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = Ink)
        }
    }
}

@Composable
private fun SaveBadge(state: EditorSaveState) {
    val badge = when (state) {
        EditorSaveState.SAVED -> Triple("Saved", AccentBg, Accent)
        EditorSaveState.UNSAVED -> Triple("Unsaved", Color(0xFFF5F0E8), Ink2)
        EditorSaveState.SAVING -> Triple("Saving", Color(0xFFF5F0E8), Accent)
        EditorSaveState.ERROR -> Triple("Needs attention", Color(0xFFFEE2E2), ErrorRed)
    }
    Surface(shape = RoundedCornerShape(14.dp), color = badge.second) {
        Text(badge.first, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = badge.third)
    }
}

@Composable
private fun BoxedOverflowMenu(expanded: Boolean, onExpandedChange: (Boolean) -> Unit, onDelete: () -> Unit) {
    Box {
        IconButton(onClick = { onExpandedChange(!expanded) }) {
            Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Ink)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            DropdownMenuItem(
                text = { Text("Delete notebook") },
                leadingIcon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null) },
                onClick = {
                    onExpandedChange(false)
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun CategoryPickerDialog(categories: List<CategoryDetail>, selectedId: Long?, onDismiss: () -> Unit, onSelect: (CategoryDetail?) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Choose category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onSelect(null) }) { Text(if (selectedId == null) "No category selected" else "Clear category") }
                categories.forEach { category ->
                    Surface(modifier = Modifier.fillMaxWidth().clickable { onSelect(category) }, shape = RoundedCornerShape(16.dp), color = if (selectedId == category.id) AccentBg else White, border = BorderStroke(1.dp, Border)) {
                        Text(category.name, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), color = Ink)
                    }
                }
            }
        }
    )
}

@Composable
private fun VersionHistoryDialog(versions: List<NotebookVersionItem>, onDismiss: () -> Unit, onRestore: (NotebookVersionItem) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Version history", style = MaterialTheme.typography.headlineSmall, color = Ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (versions.isEmpty()) {
                    Text("No saved versions yet.", style = MaterialTheme.typography.bodyMedium, color = Ink3)
                } else {
                    versions.take(8).forEach { version ->
                        Surface(modifier = Modifier.fillMaxWidth().clickable { onRestore(version) }, shape = RoundedCornerShape(18.dp), color = White, border = BorderStroke(1.dp, Border)) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(version.version ?: "Saved snapshot", style = MaterialTheme.typography.titleMedium, color = Ink)
                                Text(version.content.orEmpty().htmlToPreviewText(), style = MaterialTheme.typography.bodySmall, color = Ink2)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun DeleteNotebookDialog(onDelete: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onDelete, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = White)) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Delete notebook") },
        text = { Text("If you're offline, the delete will sync later.") }
    )
}

@Composable
private fun ExitConfirmDialog(onDismiss: () -> Unit, onSaveAndClose: () -> Unit, onDiscard: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onSaveAndClose) { Text("Save and close") } },
        dismissButton = { TextButton(onClick = onDiscard) { Text("Discard") } },
        title = { Text("Unsaved changes") },
        text = { Text("Save this notebook before leaving, or discard the current draft.") }
    )
}

@Composable
private fun ConflictDialog(onDismiss: () -> Unit, onUseLatest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onUseLatest) { Text("Use latest") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Keep editing") } },
        title = { Text("Conflict detected") },
        text = { Text("This notebook changed on another device before the last save finished.") }
    )
}
