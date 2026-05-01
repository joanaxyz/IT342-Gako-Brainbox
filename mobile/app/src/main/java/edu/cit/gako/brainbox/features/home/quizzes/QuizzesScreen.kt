package edu.cit.gako.brainbox.features.home.quizzes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_ALL
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_SCOPE_CATEGORIES
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_SCOPE_NOTEBOOKS
import edu.cit.gako.brainbox.shared.ui.BrandedSearchField
import edu.cit.gako.brainbox.shared.ui.EmptyStateCard
import edu.cit.gako.brainbox.shared.ui.SimpleHomePage
import edu.cit.gako.brainbox.shared.study.StudyCard
import edu.cit.gako.brainbox.shared.study.StudyFilterControls
import edu.cit.gako.brainbox.shared.study.StudyNotebookFilterModel
import edu.cit.gako.brainbox.shared.study.buildCategoryFilterOptions
import edu.cit.gako.brainbox.shared.study.buildNotebookFilterOptions
import edu.cit.gako.brainbox.shared.study.matchesStudyFilter
import edu.cit.gako.brainbox.shared.study.studyCategoryLabel

@Composable
internal fun QuizzesScreen(
    quizzes: List<QuizSummary>,
    notebooks: List<NotebookSummary>,
    contentPadding: PaddingValues,
    onOpenQuiz: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filterScope by rememberSaveable { mutableStateOf(STUDY_FILTER_SCOPE_NOTEBOOKS) }
    var selectedNotebookId by rememberSaveable { mutableStateOf(STUDY_FILTER_ALL) }
    var selectedCategoryId by rememberSaveable { mutableStateOf(STUDY_FILTER_ALL) }
    val notebooksByUuid = remember(notebooks) {
        notebooks.associate { it.uuid to it.toStudyNotebookFilterModel() }
    }
    val notebookOptions = remember(quizzes, notebooksByUuid) {
        buildNotebookFilterOptions(quizzes.map { it.notebookUuid }, notebooksByUuid)
    }
    val categoryOptions = remember(quizzes, notebooksByUuid) {
        buildCategoryFilterOptions(quizzes.map { it.notebookUuid }, notebooksByUuid)
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
    val filtered = quizzes.filter { quiz ->
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

    SimpleHomePage(
        title = "Quizzes",
        description = "Quiz cards mirror the web treatment, now stacked for thumb-friendly study.",
        contentPadding = contentPadding
    ) {
        BrandedSearchField(query, { query = it }, "Search quizzes")
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
            EmptyStateCard(
                title = if (quizzes.isEmpty()) "No quizzes yet" else "No matching quizzes",
                body = if (query.isNotBlank() || hasActiveFilter) {
                    "Try a different search or filter."
                } else {
                    "Quiz sets created on the web will appear here."
                }
            )
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
                        action = "Start quiz"
                    ) {
                        onOpenQuiz(quiz.uuid)
                    }
                }
            }
        }
    }
}

private fun NotebookSummary.toStudyNotebookFilterModel(): StudyNotebookFilterModel =
    StudyNotebookFilterModel(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName
    )


