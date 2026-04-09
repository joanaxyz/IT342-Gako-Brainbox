package edu.cit.gako.brainbox.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.home.SimpleHomePage
import edu.cit.gako.brainbox.network.models.QuizSummary
import edu.cit.gako.brainbox.shared.BrandedSearchField
import edu.cit.gako.brainbox.shared.EmptyStateCard
import edu.cit.gako.brainbox.shared.StudyCard

@Composable
internal fun QuizzesScreen(
    quizzes: List<QuizSummary>,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onOpenQuiz: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = quizzes.filter {
        query.isBlank() ||
            it.title.contains(other = query, ignoreCase = true) ||
            (it.notebookTitle ?: "").contains(other = query, ignoreCase = true)
    }

    SimpleHomePage(
        title = "Quizzes",
        description = "Quiz cards mirror the web treatment, now stacked for thumb-friendly study.",
        syncNotice = syncNotice,
        syncedAtLabel = syncedAtLabel,
        contentPadding = contentPadding
    ) {
        BrandedSearchField(query, { query = it }, "Search quizzes")
        Spacer(modifier = Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            EmptyStateCard("No quizzes yet", "Quiz sets created on the web will appear here once the API sync completes.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.forEach { quiz ->
                    StudyCard(
                        title = quiz.title,
                        description = quiz.description,
                        kicker = quiz.difficulty ?: "Quiz",
                        meta = listOf(
                            "${quiz.questionCount} questions",
                            quiz.estimatedTime ?: "Quick run",
                            quiz.notebookTitle ?: "BrainBox"
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


