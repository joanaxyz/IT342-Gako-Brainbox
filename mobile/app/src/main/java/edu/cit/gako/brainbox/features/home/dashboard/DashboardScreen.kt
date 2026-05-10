package edu.cit.gako.brainbox.features.home.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.app.HomeData
import edu.cit.gako.brainbox.app.HomeTab
import edu.cit.gako.brainbox.features.playback.ui.PlaybackUiState
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.home.profile.data.dto.UserProfile
import edu.cit.gako.brainbox.shared.study.ContinueLearningCard
import edu.cit.gako.brainbox.shared.ui.EmptyStateCard
import edu.cit.gako.brainbox.shared.study.NotebookCard
import edu.cit.gako.brainbox.shared.study.StudyNotebookCardModel
import edu.cit.gako.brainbox.shared.ui.OutlinedActionButton
import edu.cit.gako.brainbox.shared.ui.SectionHeader
import edu.cit.gako.brainbox.shared.study.StudyCard
import edu.cit.gako.brainbox.shared.ui.joinMeta
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.White

@Composable
internal fun DashboardScreen(
    user: UserProfile?,
    homeData: HomeData,
    playbackState: PlaybackUiState,
    contentPadding: PaddingValues,
    onGoToTab: (HomeTab) -> Unit,
    onCreateNotebook: () -> Unit,
    onOpenNotebook: (String) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onPlayNotebook: (NotebookSummary) -> Unit
) {
    val quizzesWithScores = homeData.quizzes.mapNotNull { it.bestScore }
    val decksWithMastery = homeData.flashcards.mapNotNull { it.bestMastery }
    val avgQuiz = quizzesWithScores.takeIf { it.isNotEmpty() }?.average()?.toInt()
    val avgMastery = decksWithMastery.takeIf { it.isNotEmpty() }?.average()?.toInt()
    val displayName = user?.username?.ifBlank { "there" } ?: "there"

    // Simple static greeting to avoid time API issues during composition
    val timeGreeting = "Welcome"
    val todayLabelText = "to Brainbox"

    // Calculate top performers for study progress
    val topDecks = homeData.flashcards
        .filter { it.attempts > 0 && it.bestMastery != null }
        .sortedByDescending { it.bestMastery }
        .take(3)
        .map { TopPerformer(it.title, it.notebookTitle ?: "Flashcards", it.bestMastery!!) }

    val topQuizzes = homeData.quizzes
        .filter { it.attempts > 0 && it.bestScore != null }
        .sortedByDescending { it.bestScore }
        .take(3)
        .map { TopPerformer(it.title, it.notebookTitle ?: "Quiz", it.bestScore!!) }

    val hasQuizAttempts = homeData.quizzes.any { it.attempts > 0 }
    val hasFlashcardAttempts = homeData.flashcards.any { it.attempts > 0 }
    val showStudyProgress = hasQuizAttempts || hasFlashcardAttempts
    val stats = listOf(
        DashboardStat("Notebooks", homeData.notebooks.size.toString(), Icons.Default.Book),
        DashboardStat("Avg Quiz Score", avgQuiz?.let { "$it%" } ?: "--", Icons.Default.EmojiEvents),
        DashboardStat("Avg Mastery", avgMastery?.let { "$it%" } ?: "--", Icons.Default.Psychology),
        DashboardStat("Flashcard Decks", homeData.flashcards.size.toString(), Icons.Default.AutoAwesome)
    )

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = White,
                border = BorderStroke(1.dp, Border),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(joinMeta(todayLabelText, timeGreeting), style = MaterialTheme.typography.labelMedium, color = Ink3)
                    Text("Ready to learn, $displayName?", style = MaterialTheme.typography.headlineLarge, color = Ink)
                    Text(dashboardSubtitle(homeData), style = MaterialTheme.typography.bodyMedium, color = Ink2)
                    OutlinedActionButton("+ New Notebook") {
                        onCreateNotebook()
                    }
                }
            }
        }
        item {
            StudyStreakCard(
                reviewData = homeData.recentlyReviewed,
                editData = homeData.recentlyEdited
            )
        }
        item {
            ProgressTrendsCard(
                quizData = homeData.quizzes,
                flashcardData = homeData.flashcards,
                recentlyReviewed = homeData.recentlyReviewed,
                recentlyEdited = homeData.recentlyEdited
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                stats.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { stat ->
                            StatCard(modifier = Modifier.weight(1f), stat = stat)
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        if (homeData.recentlyReviewed.isNotEmpty()) {
            item {
                SectionHeader("Recently reviewed", "See library") {
                    onGoToTab(HomeTab.LIBRARY)
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(homeData.recentlyReviewed.take(4)) { notebook ->
                        ContinueLearningCard(
                            notebook = notebook.toStudyNotebookCardModel(),
                            isPlaying = playbackState.notebookId == notebook.uuid && playbackState.isPlaying,
                            isActive = playbackState.notebookId == notebook.uuid && playbackState.isVisible,
                            onPlay = { onPlayNotebook(notebook) },
                            onClick = { onOpenNotebook(notebook.uuid) }
                        )
                    }
                }
            }
        }

        item {
            SectionHeader("Recently edited", "Open library") { onGoToTab(HomeTab.LIBRARY) }
        }
        item {
            if (homeData.recentlyEdited.isEmpty()) {
                EmptyStateCard("No notebooks yet", "Once your notebooks exist on the web, this area becomes the mobile handoff point.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    homeData.recentlyEdited.take(4).forEach { notebook ->
                        NotebookCard(
                            notebook = notebook.toStudyNotebookCardModel(),
                            action = "Open notebook",
                            isPlaying = playbackState.notebookId == notebook.uuid && playbackState.isPlaying,
                            isActive = playbackState.notebookId == notebook.uuid && playbackState.isVisible,
                            onPlay = { onPlayNotebook(notebook) },
                            onClick = { onOpenNotebook(notebook.uuid) }
                        )
                    }
                }
            }
        }

        item {
            SectionHeader("Quizzes", "View all") { onGoToTab(HomeTab.QUIZZES) }
        }
        item {
            if (homeData.quizzes.isEmpty()) {
                EmptyStateCard("No quizzes yet", "Create quizzes on the web and they will land here in the same visual system.")
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(homeData.quizzes.take(4)) { quiz ->
                        StudyCard(
                            title = quiz.title,
                            description = quiz.description,
                            kicker = quiz.difficulty ?: "Quiz",
                            meta = listOf(
                                "${quiz.questionCount} questions",
                                quiz.estimatedTime ?: "Quick run",
                                "${quiz.attempts} attempts"
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
        item {
            SectionHeader("Flashcard decks", "View all") { onGoToTab(HomeTab.FLASHCARDS) }
        }
        item {
            if (homeData.flashcards.isEmpty()) {
                EmptyStateCard("No flashcard decks yet", "Decks you create on the web will show up here with the same warm card treatment.")
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(homeData.flashcards.take(4)) { deck ->
                        StudyCard(
                            title = deck.title,
                            description = deck.description,
                            kicker = deck.notebookTitle ?: "Flashcards",
                            meta = listOf("${deck.cardCount} cards", "${deck.attempts} attempts"),
                            progress = deck.bestMastery,
                            action = "Study deck"
                        ) {
                            onOpenFlashcardDeck(deck.uuid)
                        }
                    }
                }
            }
        }

        if (showStudyProgress) {
            item {
                StudyProgressCard(
                    topDecks = topDecks,
                    topQuizzes = topQuizzes
                )
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, stat: DashboardStat) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(28.dp)
            )
            Text(stat.value, style = MaterialTheme.typography.headlineMedium, color = Ink)
            Text(stat.label, style = MaterialTheme.typography.bodySmall, color = Ink3)
        }
    }
}

private data class DashboardStat(val label: String, val value: String, val icon: ImageVector)

private fun NotebookSummary.toStudyNotebookCardModel(): StudyNotebookCardModel =
    StudyNotebookCardModel(
        uuid = uuid,
        title = title,
        wordCount = wordCount,
        updatedAt = updatedAt,
        lastReviewedAt = lastReviewedAt,
        categoryName = categoryName
    )

private fun dashboardSubtitle(homeData: HomeData): String {
    if (homeData.notebooks.isEmpty()) {
        return "Create your first notebook on the web and it will carry over here."
    }

    val quizAttempts = homeData.quizzes.count { it.attempts > 0 }
    val deckAttempts = homeData.flashcards.count { it.attempts > 0 }
    return if (quizAttempts > 0 || deckAttempts > 0) {
        joinMeta(
            quizAttempts.takeIf { it > 0 }?.let { "$it quizzes attempted" },
            deckAttempts.takeIf { it > 0 }?.let { "$it decks studied" }
        )
    } else {
        "You have ${homeData.notebooks.size} notebooks ready for your next review."
    }
}
