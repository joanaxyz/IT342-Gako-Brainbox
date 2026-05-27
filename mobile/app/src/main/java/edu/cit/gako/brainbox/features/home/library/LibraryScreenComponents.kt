package edu.cit.gako.brainbox.features.home.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.shared.ui.BrandedSearchField
import edu.cit.gako.brainbox.shared.ui.EmptyStateCard
import edu.cit.gako.brainbox.shared.study.NotebookCard
import edu.cit.gako.brainbox.shared.study.StudyNotebookCardModel
import edu.cit.gako.brainbox.shared.ui.SimpleHomePage
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.AccentBg
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Cream2
import edu.cit.gako.brainbox.shared.ui.theme.ErrorRed
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.White
import kotlinx.coroutines.launch

@Composable
internal fun CategoryManagerPanel(
    categories: List<CategoryDetail>,
    totalCategoryCount: Int,
    notebookCount: Int,
    uncategorizedCount: Int,
    categoryCounts: Map<Long, Int>,
    selectedCategoryId: String,
    query: String,
    sortBy: String,
    sortAsc: Boolean,
    onQueryChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onCreateCategory: () -> Unit,
    onEditCategory: (CategoryDetail) -> Unit,
    onDeleteCategory: (CategoryDetail) -> Unit,
    onSortName: () -> Unit,
    onSortCount: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Categories", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text(
                        formatCount(totalCategoryCount, "category", "categories"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                }
                Surface(
                    onClick = onCreateCategory,
                    shape = CircleShape,
                    color = Accent,
                    contentColor = White
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New category",
                        modifier = Modifier.padding(8.dp).size(18.dp)
                    )
                }
            }

            BrandedSearchField(query, onQueryChange, "Search categories")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortPill(
                    label = "Name",
                    selected = sortBy == SORT_CATEGORY_NAME,
                    ascending = sortAsc,
                    onClick = onSortName
                )
                SortPill(
                    label = "Count",
                    selected = sortBy == SORT_CATEGORY_COUNT,
                    ascending = sortAsc,
                    onClick = onSortCount
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryRow(
                    title = "All notebooks",
                    subtitle = formatCount(notebookCount, "notebook"),
                    selected = selectedCategoryId == ALL_CATEGORIES,
                    onClick = { onSelectCategory(ALL_CATEGORIES) }
                )
                CategoryRow(
                    title = "Uncategorized",
                    subtitle = formatCount(uncategorizedCount, "notebook"),
                    selected = selectedCategoryId == UNCATEGORIZED,
                    onClick = { onSelectCategory(UNCATEGORIZED) }
                )
                categories.forEach { category ->
                    CategoryRow(
                        title = category.name,
                        subtitle = formatCount(categoryCounts[category.id] ?: 0, "notebook"),
                        selected = selectedCategoryId == category.id.toString(),
                        canDelete = true,
                        onClick = { onSelectCategory(category.id.toString()) },
                        onEdit = { onEditCategory(category) },
                        onDelete = { onDeleteCategory(category) }
                    )
                }
            }

            if (totalCategoryCount == 0) {
                Text(
                    "No categories yet. Create one for subjects, semesters, projects, or any system that works for you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
            } else if (categories.isEmpty() && query.isNotBlank()) {
                Text(
                    "No categories match \"$query\".",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    canDelete: Boolean = false,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) AccentBg else White,
        border = BorderStroke(1.dp, if (selected) Accent.copy(alpha = 0.3f) else Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (title == "All notebooks") Icons.AutoMirrored.Filled.LibraryBooks else Icons.Filled.Folder,
                contentDescription = null,
                tint = if (selected) Accent else Ink3,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) Accent else Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Ink3)
            }
            if (canDelete && onEdit != null) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit category",
                        tint = Ink3,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (canDelete && onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete category",
                        tint = ErrorRed.copy(alpha = 0.75f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SortPill(
    label: String,
    selected: Boolean,
    ascending: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) AccentBg else Cream2,
        border = BorderStroke(1.dp, if (selected) Accent.copy(alpha = 0.28f) else Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    !selected -> Icons.AutoMirrored.Filled.Sort
                    ascending -> Icons.Filled.KeyboardArrowUp
                    else -> Icons.Filled.KeyboardArrowDown
                },
                contentDescription = null,
                tint = if (selected) Accent else Ink3,
                modifier = Modifier.size(15.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Accent else Ink2
            )
        }
    }
}

@Composable
internal fun LibraryNotebookToolbar(
    selectedCategoryLabel: String,
    selectedCategoryCount: Int,
    query: String,
    sortBy: String,
    sortAsc: Boolean,
    onQueryChange: (String) -> Unit,
    onSort: (String) -> Unit,
    onShowAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(selectedCategoryLabel, style = MaterialTheme.typography.titleLarge, color = Ink)
                Text(formatCount(selectedCategoryCount, "notebook"), style = MaterialTheme.typography.bodySmall, color = Ink3)
            }
            if (selectedCategoryLabel != "All notebooks") {
                TextButton(onClick = onShowAll) {
                    Text("Show all", color = Accent)
                }
            }
        }
        BrandedSearchField(query, onQueryChange, "Search notebooks or categories")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortPill(
                label = "Date",
                selected = sortBy == SORT_UPDATED_AT,
                ascending = sortAsc,
                onClick = { onSort(SORT_UPDATED_AT) }
            )
            SortPill(
                label = "Title",
                selected = sortBy == SORT_TITLE,
                ascending = sortAsc,
                onClick = { onSort(SORT_TITLE) }
            )
            SortPill(
                label = "Words",
                selected = sortBy == SORT_WORDS,
                ascending = sortAsc,
                onClick = { onSort(SORT_WORDS) }
            )
        }
    }
}

@Composable
internal fun LibraryNotebookFooter(
    notebook: NotebookSummary,
    isMoving: Boolean,
    onMove: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(color = Border)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Category", style = MaterialTheme.typography.labelSmall, color = Ink3)
                Text(
                    notebook.categoryName ?: "Uncategorized",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FilledTonalButton(
                onClick = onMove,
                enabled = !isMoving,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Cream2,
                    contentColor = Ink2
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text(if (isMoving) "Moving" else "Move", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
internal fun CreateCategoryDialog(
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New category") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category name") },
                placeholder = { Text("Biology notes") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                enabled = name.isNotBlank() && !isBusy
            ) {
                Text(if (isBusy) "Creating" else "Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) { Text("Cancel") }
        }
    )
}

@Composable
internal fun EditCategoryDialog(
    category: CategoryDetail,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by rememberSaveable(category.id) { mutableStateOf(category.name) }
    val trimmedName = name.trim()
    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = { Text("Edit category") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (trimmedName.isNotBlank()) onSave(trimmedName) },
                enabled = trimmedName.isNotBlank() && trimmedName != category.name && !isBusy
            ) {
                Text(if (isBusy) "Saving" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) { Text("Cancel") }
        }
    )
}

@Composable
internal fun MoveNotebookDialog(
    notebook: NotebookSummary,
    categories: List<CategoryDetail>,
    selectedCategoryId: Long?,
    isMoving: Boolean,
    onDismiss: () -> Unit,
    onCreateCategory: () -> Unit,
    onSelectCategory: (CategoryDetail?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Move notebook")
                Text(notebook.title, style = MaterialTheme.typography.bodySmall, color = Ink3)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                CategoryPickRow(
                    title = "Uncategorized",
                    selected = selectedCategoryId == null,
                    enabled = !isMoving,
                    onClick = { onSelectCategory(null) }
                )
                categories.forEach { category ->
                    CategoryPickRow(
                        title = category.name,
                        selected = selectedCategoryId == category.id,
                        enabled = !isMoving,
                        onClick = { onSelectCategory(category) }
                    )
                }
                HorizontalDivider(color = Border)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isMoving, onClick = onCreateCategory)
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                    Text("Create new category", color = Accent, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !isMoving) {
                Text(if (isMoving) "Moving" else "Done")
            }
        }
    )
}

@Composable
private fun CategoryPickRow(
    title: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) Accent else Ink,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun DeleteCategoryDialog(
    target: CategoryDeleteTarget,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    var deleteNotebooks by rememberSaveable(target.category.id) { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Delete \"${target.category.name}\"?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink
                )
                if (target.notebookCount > 0) {
                    Text(
                        "This category contains ${formatCount(target.notebookCount, "notebook")}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                    DeleteModeRow(
                        title = "Move notebooks to Uncategorized",
                        subtitle = "Keep the notebooks in your library.",
                        selected = !deleteNotebooks,
                        enabled = !isBusy,
                        onClick = { deleteNotebooks = false }
                    )
                    DeleteModeRow(
                        title = "Delete the notebooks too",
                        subtitle = "Remove the category and every notebook inside it.",
                        selected = deleteNotebooks,
                        enabled = !isBusy,
                        danger = true,
                        onClick = { deleteNotebooks = true }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(deleteNotebooks) },
                enabled = !isBusy
            ) {
                Text(if (isBusy) "Deleting" else "Delete", color = ErrorRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) { Text("Cancel") }
        }
    )
}

@Composable
private fun DeleteModeRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val tint = when {
        danger && selected -> ErrorRed
        selected -> Accent
        else -> Ink3
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = tint)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Ink3)
        }
    }
}

internal fun mergeCategories(
    categories: List<CategoryDetail>,
    notebooks: List<NotebookSummary>
): List<CategoryDetail> {
    val merged = linkedMapOf<Long, CategoryDetail>()
    categories.forEach { category ->
        merged[category.id] = category
    }
    notebooks.forEach { notebook ->
        val id = notebook.categoryId
        val name = notebook.categoryName
        if (id != null && !name.isNullOrBlank() && !merged.containsKey(id)) {
            merged[id] = CategoryDetail(id = id, name = name)
        }
    }
    return merged.values.sortedBy { it.name.lowercase() }
}

internal fun NotebookSummary.toStudyNotebookCardModel(): StudyNotebookCardModel =
    StudyNotebookCardModel(
        uuid = uuid,
        title = title,
        wordCount = wordCount,
        updatedAt = updatedAt,
        lastReviewedAt = lastReviewedAt,
        categoryName = categoryName
    )

internal fun categoriesFromNotebooks(notebooks: List<NotebookSummary>): List<CategoryDetail> {
    return mergeCategories(emptyList(), notebooks)
}

internal fun NotebookDetail.toSummary(fallback: NotebookSummary): NotebookSummary {
    return NotebookSummary(
        uuid = uuid,
        title = title,
        wordCount = wordCount ?: fallback.wordCount,
        createdAt = createdAt ?: fallback.createdAt,
        updatedAt = updatedAt ?: fallback.updatedAt,
        lastReviewedAt = lastReviewedAt ?: fallback.lastReviewedAt,
        version = version ?: fallback.version,
        categoryId = categoryId,
        categoryName = categoryName
    )
}

internal fun formatCount(count: Int, singular: String, plural: String = "${singular}s"): String {
    return "$count ${if (count == 1) singular else plural}"
}
