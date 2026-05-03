package edu.cit.gako.brainbox.features.home.quizzes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.features.home.quizzes.data.QuizRepository
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizCreateRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizDetail
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_ALL
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_SCOPE_CATEGORIES
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_SCOPE_NOTEBOOKS
import edu.cit.gako.brainbox.shared.study.StudyCard
import edu.cit.gako.brainbox.shared.study.StudyFilterControls
import edu.cit.gako.brainbox.shared.study.StudyNotebookFilterModel
import edu.cit.gako.brainbox.shared.study.buildCategoryFilterOptions
import edu.cit.gako.brainbox.shared.study.buildNotebookFilterOptions
import edu.cit.gako.brainbox.shared.study.matchesStudyFilter
import edu.cit.gako.brainbox.shared.study.studyCategoryLabel
import edu.cit.gako.brainbox.shared.ui.BrandedSearchField
import edu.cit.gako.brainbox.shared.ui.EmptyStateCard
import edu.cit.gako.brainbox.shared.ui.NoticeBanner
import edu.cit.gako.brainbox.shared.ui.SimpleHomePage
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.AccentBg
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.ErrorRed
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.White
import java.util.Locale
import kotlinx.coroutines.launch

private const val QUIZ_SORT_UPDATED_AT = "updatedAt"
private const val QUIZ_SORT_TITLE = "title"
private const val QUIZ_SORT_QUESTION_COUNT = "questionCount"

private val QuizSortOptions = listOf(
    StudySortOption(QUIZ_SORT_UPDATED_AT, "Updated"),
    StudySortOption(QUIZ_SORT_TITLE, "Title"),
    StudySortOption(QUIZ_SORT_QUESTION_COUNT, "Questions")
)

@Composable
internal fun QuizzesScreen(
    repository: QuizRepository,
    quizzes: List<QuizSummary>,
    notebooks: List<NotebookSummary>,
    contentPadding: PaddingValues,
    onFeatureRequest: (String) -> Unit,
    onOpenQuiz: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var localQuizzes by remember(quizzes) { mutableStateOf(quizzes) }
    var query by rememberSaveable { mutableStateOf("") }
    var filterScope by rememberSaveable { mutableStateOf(STUDY_FILTER_SCOPE_NOTEBOOKS) }
    var selectedNotebookId by rememberSaveable { mutableStateOf(STUDY_FILTER_ALL) }
    var selectedCategoryId by rememberSaveable { mutableStateOf(STUDY_FILTER_ALL) }
    var sortBy by rememberSaveable { mutableStateOf(QUIZ_SORT_UPDATED_AT) }
    var sortAscending by rememberSaveable { mutableStateOf(false) }
    var loadingEditUuid by rememberSaveable { mutableStateOf<String?>(null) }
    var editorMode by remember { mutableStateOf<QuizComposerMode?>(null) }
    var editingQuiz by remember { mutableStateOf<QuizDetail?>(null) }
    var deleteTarget by remember { mutableStateOf<QuizDeleteTarget?>(null) }
    var mutationBusy by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    val notebooksByUuid = remember(notebooks) {
        notebooks.associate { it.uuid to it.toStudyNotebookFilterModel() }
    }
    val notebookOptions = remember(localQuizzes, notebooksByUuid) {
        buildNotebookFilterOptions(localQuizzes.map { it.notebookUuid }, notebooksByUuid)
    }
    val categoryOptions = remember(localQuizzes, notebooksByUuid) {
        buildCategoryFilterOptions(localQuizzes.map { it.notebookUuid }, notebooksByUuid)
    }

    LaunchedEffect(notebookOptions, selectedNotebookId) {
        if (notebookOptions.none { it.key == selectedNotebookId }) {
            selectedNotebookId = STUDY_FILTER_ALL
        }
    }
    LaunchedEffect(categoryOptions, selectedCategoryId) {
        if (categoryOptions.none { it.key == selectedCategoryId }) {
            selectedCategoryId = STUDY_FILTER_ALL
        }
    }

    val hasActiveFilter = if (filterScope == STUDY_FILTER_SCOPE_CATEGORIES) {
        selectedCategoryId != STUDY_FILTER_ALL
    } else {
        selectedNotebookId != STUDY_FILTER_ALL
    }
    val filtered = remember(
        localQuizzes,
        notebooksByUuid,
        query,
        filterScope,
        selectedNotebookId,
        selectedCategoryId,
        sortBy,
        sortAscending
    ) {
        val filteredItems = localQuizzes.filter { quiz ->
            val categoryLabel = studyCategoryLabel(quiz.notebookUuid, notebooksByUuid).orEmpty()
            val matchesQuery = query.isBlank() ||
                quiz.title.contains(other = query, ignoreCase = true) ||
                (quiz.notebookTitle ?: "").contains(other = query, ignoreCase = true) ||
                categoryLabel.contains(other = query, ignoreCase = true)

            matchesQuery && matchesStudyFilter(
                notebookUuid = quiz.notebookUuid,
                notebooksByUuid = notebooksByUuid,
                filterScope = filterScope,
                selectedNotebookId = selectedNotebookId,
                selectedCategoryId = selectedCategoryId
            )
        }

        val sorted = when (sortBy) {
            QUIZ_SORT_TITLE -> filteredItems.sortedBy { it.title.lowercase(Locale.ENGLISH) }
            QUIZ_SORT_QUESTION_COUNT -> filteredItems.sortedBy { it.questionCount }
            else -> filteredItems.sortedBy { it.updatedAt.orEmpty() }
        }

        if (sortAscending) sorted else sorted.asReversed()
    }

    fun upsertQuiz(quiz: QuizDetail) {
        val summary = quiz.toSummary()
        localQuizzes = listOf(summary) + localQuizzes.filter { it.uuid != summary.uuid }
    }

    fun submitQuiz(payload: QuizCreateRequest) {
        val mode = editorMode ?: return
        val targetUuid = editingQuiz?.uuid
        mutationBusy = true
        scope.launch {
            runCatching {
                if (mode == QuizComposerMode.CREATE) {
                    repository.createQuiz(payload)
                } else {
                    repository.updateQuiz(targetUuid.orEmpty(), payload)
                }
            }.onSuccess { saved ->
                upsertQuiz(saved)
                editorMode = null
                editingQuiz = null
                message = if (mode == QuizComposerMode.CREATE) {
                    "\"${saved.title}\" created."
                } else {
                    "\"${saved.title}\" saved."
                }
            }.onFailure {
                onFeatureRequest(
                    if (mode == QuizComposerMode.CREATE) "Couldn't create quiz." else "Couldn't save quiz."
                )
            }
            mutationBusy = false
        }
    }

    fun openEditorForQuiz(quiz: QuizSummary) {
        loadingEditUuid = quiz.uuid
        scope.launch {
            runCatching { repository.getQuiz(quiz.uuid) }
                .onSuccess {
                    editingQuiz = it
                    editorMode = QuizComposerMode.EDIT
                }
                .onFailure {
                    onFeatureRequest("Couldn't open quiz editor.")
                }
            loadingEditUuid = null
        }
    }

    fun deleteQuiz(target: QuizDeleteTarget) {
        val previousQuizzes = localQuizzes
        localQuizzes = localQuizzes.filter { it.uuid != target.uuid }
        deleteTarget = null
        if (target.closeEditorOnSuccess) {
            editorMode = null
            editingQuiz = null
        }
        mutationBusy = true
        scope.launch {
            runCatching { repository.deleteQuiz(target.uuid) }
                .onSuccess {
                    message = "\"${target.title}\" deleted."
                }
                .onFailure {
                    localQuizzes = previousQuizzes
                    onFeatureRequest("Couldn't delete quiz.")
                }
            mutationBusy = false
        }
    }

    SimpleHomePage(
        title = "Quizzes",
        description = "Create, edit, sort, and run the same quiz sets you use on web.",
        contentPadding = contentPadding
    ) {
        message?.let {
            NoticeBanner(it, "Quiz changes sync with your web workspace.")
            Spacer(modifier = Modifier.height(16.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${localQuizzes.size} quizzes", style = MaterialTheme.typography.titleMedium, color = Ink)
                Text("Multiple choice and true / false", style = MaterialTheme.typography.bodySmall, color = Ink3)
            }
            Button(
                onClick = {
                    editingQuiz = null
                    editorMode = QuizComposerMode.CREATE
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Create")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        BrandedSearchField(query, { query = it }, "Search quizzes")
        Spacer(modifier = Modifier.height(16.dp))
        StudySortControls(
            options = QuizSortOptions,
            sortBy = sortBy,
            sortAscending = sortAscending,
            onSortChange = { nextSort ->
                if (sortBy == nextSort) {
                    sortAscending = !sortAscending
                } else {
                    sortBy = nextSort
                    sortAscending = nextSort == QUIZ_SORT_TITLE
                }
            },
            onToggleDirection = { sortAscending = !sortAscending }
        )
        Spacer(modifier = Modifier.height(16.dp))
        StudyFilterControls(
            filterScope = filterScope,
            selectedNotebookId = selectedNotebookId,
            selectedCategoryId = selectedCategoryId,
            notebookOptions = notebookOptions,
            categoryOptions = categoryOptions,
            onFilterScopeChange = { filterScope = it },
            onNotebookFilterChange = { selectedNotebookId = it },
            onCategoryFilterChange = { selectedCategoryId = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EmptyStateCard(
                    title = if (localQuizzes.isEmpty()) "No quizzes yet" else "No matching quizzes",
                    body = if (query.isNotBlank() || hasActiveFilter) {
                        "Try a different search or filter."
                    } else {
                        "Create your first quiz here or on the web."
                    }
                )
                if (localQuizzes.isEmpty() && query.isBlank() && !hasActiveFilter) {
                    OutlinedButton(
                        onClick = {
                            editingQuiz = null
                            editorMode = QuizComposerMode.CREATE
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Border),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = Ink2),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("Create quiz")
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.forEach { quiz ->
                    val packNotebookUuid = quiz.notebookUuid
                    val categoryLabel = studyCategoryLabel(packNotebookUuid, notebooksByUuid)
                        ?.takeUnless { it == "Standalone" }
                    StudyCard(
                        title = quiz.title,
                        description = quiz.description,
                        kicker = quiz.difficulty ?: "Quiz",
                        meta = listOfNotNull(
                            "${quiz.questionCount} questions",
                            quiz.estimatedTime ?: "Quick run",
                            quiz.notebookTitle ?: "Standalone",
                            categoryLabel
                        ),
                        progress = quiz.bestScore,
                        action = "Start quiz",
                        footer = {
                            QuizCardActions(
                                isOpeningEditor = loadingEditUuid == quiz.uuid,
                                onEdit = { openEditorForQuiz(quiz) },
                                onDelete = {
                                    deleteTarget = QuizDeleteTarget(
                                        uuid = quiz.uuid,
                                        title = quiz.title,
                                        closeEditorOnSuccess = false
                                    )
                                }
                            )
                        }
                    ) {
                        onOpenQuiz(quiz.uuid)
                    }
                }
            }
        }
    }

    editorMode?.let { mode ->
        QuizComposerSheet(
            mode = mode,
            initialQuiz = editingQuiz,
            notebooks = notebooks,
            isBusy = mutationBusy,
            onDismiss = {
                if (!mutationBusy) {
                    editorMode = null
                    editingQuiz = null
                }
            },
            onSubmit = ::submitQuiz,
            onDelete = { quiz ->
                deleteTarget = QuizDeleteTarget(
                    uuid = quiz.uuid,
                    title = quiz.title,
                    closeEditorOnSuccess = true
                )
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!mutationBusy) deleteTarget = null },
            confirmButton = {
                TextButton(
                    onClick = { deleteQuiz(target) },
                    enabled = !mutationBusy
                ) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteTarget = null },
                    enabled = !mutationBusy
                ) {
                    Text("Cancel", color = Ink2)
                }
            },
            title = { Text("Delete quiz?", color = Ink) },
            text = { Text("\"${target.title}\" will be removed from mobile and web.", color = Ink3) },
            containerColor = White
        )
    }
}

@Composable
private fun QuizCardActions(
    isOpeningEditor: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onEdit,
            enabled = !isOpeningEditor,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null, tint = Accent)
            Text(if (isOpeningEditor) "Opening" else "Edit", color = Accent)
        }
        TextButton(
            onClick = onDelete,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, tint = ErrorRed)
            Text("Delete", color = ErrorRed)
        }
    }
}

@Composable
private fun StudySortControls(
    options: List<StudySortOption>,
    sortBy: String,
    sortAscending: Boolean,
    onSortChange: (String) -> Unit,
    onToggleDirection: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Sort by", style = MaterialTheme.typography.labelSmall, color = Ink3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = sortBy == option.key,
                    onClick = { onSortChange(option.key) },
                    label = { Text(option.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBg,
                        selectedLabelColor = Accent
                    )
                )
            }
            FilterChip(
                selected = sortAscending,
                onClick = onToggleDirection,
                label = { Text(if (sortAscending) "Asc" else "Desc") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentBg,
                    selectedLabelColor = Accent
                )
            )
        }
    }
}

private data class StudySortOption(
    val key: String,
    val label: String
)

private data class QuizDeleteTarget(
    val uuid: String,
    val title: String,
    val closeEditorOnSuccess: Boolean
)

private fun QuizDetail.toSummary(): QuizSummary =
    QuizSummary(
        uuid = uuid,
        title = title,
        description = description,
        difficulty = difficulty,
        notebookUuid = notebookUuid,
        notebookTitle = notebookTitle,
        questionCount = questionCount,
        estimatedTime = estimatedTime,
        bestScore = bestScore,
        attempts = attempts,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

private fun NotebookSummary.toStudyNotebookFilterModel(): StudyNotebookFilterModel =
    StudyNotebookFilterModel(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName
    )
