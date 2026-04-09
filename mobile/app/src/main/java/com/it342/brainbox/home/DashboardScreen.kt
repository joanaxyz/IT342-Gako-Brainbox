package com.it342.brainbox.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.it342.brainbox.app.HomeData
import com.it342.brainbox.app.HomeTab
import com.it342.brainbox.network.models.UserProfile
import com.it342.brainbox.shared.ContinueLearningCard
import com.it342.brainbox.shared.EmptyStateCard
import com.it342.brainbox.shared.NotebookCard
import com.it342.brainbox.shared.OutlinedActionButton
import com.it342.brainbox.shared.SectionHeader
import com.it342.brainbox.shared.StudyCard
import com.it342.brainbox.shared.SyncNoticeBanner
import com.it342.brainbox.shared.TokenBadge
import com.it342.brainbox.shared.joinMeta
import com.it342.brainbox.ui.theme.Border
import com.it342.brainbox.ui.theme.Ink
import com.it342.brainbox.ui.theme.Ink2
import com.it342.brainbox.ui.theme.Ink3
import com.it342.brainbox.ui.theme.White
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun DashboardScreen(
    user: UserProfile?,
    homeData: HomeData,
    contentPadding: PaddingValues,
    onGoToTab: (HomeTab) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onFeatureRequest: (String) -> Unit
) {
    val quizzesWithScores = homeData.quizzes.mapNotNull { it.bestScore }
    val decksWithMastery = homeData.flashcards.mapNotNull { it.bestMastery }
    val avgQuiz = quizzesWithScores.takeIf { it.isNotEmpty() }?.average()?.toInt()
    val avgMastery = decksWithMastery.takeIf { it.isNotEmpty() }?.average()?.toInt()
    val displayName = user?.username?.ifBlank { "there" } ?: "there"
    val stats = listOf(
        DashboardStat("Notebooks", homeData.notebooks.size.toString(), "NB"),
        DashboardStat("Avg Quiz Score", avgQuiz?.let { "$it%" } ?: "--", "QZ"),
        DashboardStat("Avg Mastery", avgMastery?.let { "$it%" } ?: "--", "MS"),
        DashboardStat("Decks", homeData.flashcards.size.toString(), "FC")
    )

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            if (!homeData.syncNotice.isNullOrBlank()) {
                SyncNoticeBanner(homeData.syncNotice, homeData.syncedAtLabel)
                Spacer(modifier = Modifier.height(16.dp))
            }
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
                    Text(joinMeta(todayLabel(), timeGreeting()), style = MaterialTheme.typography.labelMedium, color = Ink3)
                    Text("Ready to learn, $displayName?", style = MaterialTheme.typography.headlineLarge, color = Ink)
                    Text(dashboardSubtitle(homeData), style = MaterialTheme.typography.bodyMedium, color = Ink2)
                    OutlinedActionButton("+ New Notebook") {
                        onFeatureRequest("Notebook creation is the next mobile workflow to wire up.")
                    }
                }
            }
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
                SectionHeader("Continue learning", "See library") {
                    onGoToTab(HomeTab.LIBRARY)
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(homeData.recentlyReviewed.take(4)) { notebook ->
                        ContinueLearningCard(notebook) {
                            onFeatureRequest("Notebook playback and editing are next on mobile.")
                        }
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
        item {
            SectionHeader("Recently edited", "Open library") { onGoToTab(HomeTab.LIBRARY) }
        }
        item {
            if (homeData.recentlyEdited.isEmpty()) {
                EmptyStateCard("No notebooks yet", "Once your notebooks exist on the web, this area becomes the mobile handoff point.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    homeData.recentlyEdited.take(4).forEach { notebook ->
                        NotebookCard(notebook, "Open notebook") {
                            onFeatureRequest("Notebook reading and editing are next in line for mobile.")
                        }
                    }
                }
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
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TokenBadge(stat.badge)
            Text(stat.value, style = MaterialTheme.typography.headlineMedium, color = Ink)
            Text(stat.label, style = MaterialTheme.typography.bodySmall, color = Ink3)
        }
    }
}

private data class DashboardStat(val label: String, val value: String, val badge: String)

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
        "${homeData.notebooks.size} notebooks ready for your next review."
    }
}

private fun timeGreeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}

private fun todayLabel(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH))

