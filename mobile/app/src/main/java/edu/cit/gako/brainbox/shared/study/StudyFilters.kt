package edu.cit.gako.brainbox.shared.study

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.AccentBg
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import java.util.Locale

internal const val STUDY_FILTER_SCOPE_NOTEBOOKS = "notebooks"
internal const val STUDY_FILTER_SCOPE_CATEGORIES = "categories"
internal const val STUDY_FILTER_ALL = "all"
internal const val STUDY_FILTER_STANDALONE = "standalone"
internal const val STUDY_FILTER_UNCATEGORIZED = "uncategorized"

internal data class StudyFilterOption(
    val key: String,
    val label: String
)

internal data class StudyNotebookFilterModel(
    val uuid: String,
    val title: String,
    val categoryId: Long? = null,
    val categoryName: String? = null
)

internal fun buildNotebookFilterOptions(
    notebookUuids: List<String?>,
    notebooksByUuid: Map<String, StudyNotebookFilterModel>
): List<StudyFilterOption> {
    val options = mutableListOf(StudyFilterOption(STUDY_FILTER_ALL, "All"))
    val seenNotebookUuids = linkedSetOf<String>()
    var hasStandalone = false

    notebookUuids.forEach { notebookUuid ->
        if (notebookUuid.isNullOrBlank()) {
            hasStandalone = true
            return@forEach
        }

        if (seenNotebookUuids.add(notebookUuid)) {
            options += StudyFilterOption(
                key = notebookUuid,
                label = notebooksByUuid[notebookUuid]?.title ?: "Notebook"
            )
        }
    }

    if (hasStandalone) {
        options += StudyFilterOption(STUDY_FILTER_STANDALONE, "Standalone")
    }

    return options
}

internal fun buildCategoryFilterOptions(
    notebookUuids: List<String?>,
    notebooksByUuid: Map<String, StudyNotebookFilterModel>
): List<StudyFilterOption> {
    val categories = linkedMapOf<String, String>()
    var hasUncategorized = false
    var hasStandalone = false

    notebookUuids.forEach { notebookUuid ->
        if (notebookUuid.isNullOrBlank()) {
            hasStandalone = true
            return@forEach
        }

        val notebook = notebooksByUuid[notebookUuid]
        val categoryId = notebook?.categoryId
        if (categoryId == null) {
            hasUncategorized = true
        } else {
            categories[categoryId.toString()] = notebook.categoryName ?: "Category"
        }
    }

    return buildList {
        add(StudyFilterOption(STUDY_FILTER_ALL, "All"))
        categories.entries
            .sortedBy { it.value.lowercase(Locale.ENGLISH) }
            .forEach { add(StudyFilterOption(it.key, it.value)) }
        if (hasUncategorized) {
            add(StudyFilterOption(STUDY_FILTER_UNCATEGORIZED, "Uncategorized"))
        }
        if (hasStandalone) {
            add(StudyFilterOption(STUDY_FILTER_STANDALONE, "Standalone"))
        }
    }
}

internal fun matchesStudyFilter(
    notebookUuid: String?,
    notebooksByUuid: Map<String, StudyNotebookFilterModel>,
    filterScope: String,
    selectedNotebookId: String,
    selectedCategoryId: String
): Boolean {
    if (filterScope == STUDY_FILTER_SCOPE_CATEGORIES) {
        return when (selectedCategoryId) {
            STUDY_FILTER_ALL -> true
            STUDY_FILTER_STANDALONE -> notebookUuid.isNullOrBlank()
            STUDY_FILTER_UNCATEGORIZED -> !notebookUuid.isNullOrBlank() &&
                notebooksByUuid[notebookUuid]?.categoryId == null
            else -> !notebookUuid.isNullOrBlank() &&
                notebooksByUuid[notebookUuid]?.categoryId?.toString() == selectedCategoryId
        }
    }

    return when (selectedNotebookId) {
        STUDY_FILTER_ALL -> true
        STUDY_FILTER_STANDALONE -> notebookUuid.isNullOrBlank()
        else -> notebookUuid == selectedNotebookId
    }
}

internal fun studyCategoryLabel(
    notebookUuid: String?,
    notebooksByUuid: Map<String, StudyNotebookFilterModel>
): String? {
    if (notebookUuid.isNullOrBlank()) {
        return "Standalone"
    }

    val notebook = notebooksByUuid[notebookUuid] ?: return null
    return notebook.categoryName ?: "Uncategorized"
}

@Composable
internal fun StudyFilterControls(
    filterScope: String,
    selectedNotebookId: String,
    selectedCategoryId: String,
    notebookOptions: List<StudyFilterOption>,
    categoryOptions: List<StudyFilterOption>,
    onFilterScopeChange: (String) -> Unit,
    onNotebookFilterChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit
) {
    val activeOptions = if (filterScope == STUDY_FILTER_SCOPE_CATEGORIES) {
        categoryOptions
    } else {
        notebookOptions
    }
    val selectedOption = if (filterScope == STUDY_FILTER_SCOPE_CATEGORIES) {
        selectedCategoryId
    } else {
        selectedNotebookId
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Filter by", style = MaterialTheme.typography.labelSmall, color = Ink3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StudyFilterChip(
                label = "Notebooks",
                selected = filterScope == STUDY_FILTER_SCOPE_NOTEBOOKS,
                onClick = { onFilterScopeChange(STUDY_FILTER_SCOPE_NOTEBOOKS) }
            )
            StudyFilterChip(
                label = "Categories",
                selected = filterScope == STUDY_FILTER_SCOPE_CATEGORIES,
                onClick = { onFilterScopeChange(STUDY_FILTER_SCOPE_CATEGORIES) }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            activeOptions.forEach { option ->
                StudyFilterChip(
                    label = option.label,
                    selected = selectedOption == option.key,
                    onClick = {
                        if (filterScope == STUDY_FILTER_SCOPE_CATEGORIES) {
                            onCategoryFilterChange(option.key)
                        } else {
                            onNotebookFilterChange(option.key)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StudyFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentBg,
            selectedLabelColor = Accent
        )
    )
}
