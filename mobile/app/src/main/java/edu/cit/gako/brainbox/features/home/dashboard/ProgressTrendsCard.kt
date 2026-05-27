package edu.cit.gako.brainbox.features.home.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.White
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

enum class TrendType(val label: String, val description: String) {
    QUIZ_PERFORMANCE("Quiz Performance", "Quiz scores & attempts"),
    FLASHCARD_MASTERY("Flashcard Mastery", "Mastery levels & attempts"),
    REVIEW_ACTIVITY("Review Activity", "Notebook reviews"),
    EDIT_ACTIVITY("Edit Activity", "Notebook edits")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ProgressTrendsCard(
    quizData: List<QuizSummary> = emptyList(),
    flashcardData: List<FlashcardDeckSummary> = emptyList(),
    recentlyReviewed: List<NotebookSummary> = emptyList(),
    recentlyEdited: List<NotebookSummary> = emptyList()
) {
    val pagerState = rememberPagerState(pageCount = { TrendType.entries.size })
    val coroutineScope = rememberCoroutineScope()
    val selectedTrend = pagerState.currentPage

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Trend Analysis",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                fontWeight = FontWeight.SemiBold
            )

            TabRow(
                selectedTabIndex = selectedTrend,
                containerColor = Color.Transparent,
                contentColor = Ink,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTrend]),
                        color = Color(0xFFC2410C)
                    )
                },
                divider = {}
            ) {
                TrendType.entries.forEachIndexed { index, trend ->
                    Tab(
                        selected = selectedTrend == index,
                        onClick = {
                            if (selectedTrend != index) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                        text = {
                            Text(
                                text = trend.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (selectedTrend == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val trend = TrendType.entries[page]
                TrendContent(
                    trend = trend,
                    quizData = quizData,
                    flashcardData = flashcardData,
                    recentlyReviewed = recentlyReviewed,
                    recentlyEdited = recentlyEdited
                )
            }
        }
    }
}

@Composable
private fun TrendContent(
    trend: TrendType,
    quizData: List<QuizSummary>,
    flashcardData: List<FlashcardDeckSummary>,
    recentlyReviewed: List<NotebookSummary>,
    recentlyEdited: List<NotebookSummary>
) {
    val chartData = remember(trend, quizData, flashcardData, recentlyReviewed, recentlyEdited) {
        generateChartData(trend, quizData, flashcardData, recentlyReviewed, recentlyEdited)
    }
    val maxValue = remember(chartData) {
        chartData.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = trend.description,
            style = MaterialTheme.typography.bodySmall,
            color = Ink3
        )

        // Simple bar chart visualization
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            chartData.forEach { point ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val barHeight = (point.value / maxValue) * 100f
                    Surface(
                        modifier = Modifier
                            .width(24.dp)
                            .height((barHeight * 1.5f).dp.coerceAtLeast(20.dp)),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFC2410C)
                    ) {}
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TrendStat(label = "Current", value = chartData.lastOrNull()?.value?.toInt() ?: 0)
            TrendStat(label = "Average", value = chartData.map { it.value }.averageFloatOrZero().toInt())
        }
    }
}

@Composable
private fun TrendStat(label: String, value: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Ink3
        )
    }
}

internal data class TrendPoint(
    val label: String,
    val value: Float
)

internal fun generateChartData(
    trend: TrendType,
    quizData: List<QuizSummary>,
    flashcardData: List<FlashcardDeckSummary>,
    recentlyReviewed: List<NotebookSummary>,
    recentlyEdited: List<NotebookSummary>,
    today: LocalDate = LocalDate.now(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<TrendPoint> {
    val quizScore = quizData
        .mapNotNull { quiz -> if (quiz.attempts > 0) quiz.bestScore else null }
        .averageIntOrZero()
    val flashcardMastery = flashcardData
        .mapNotNull { deck -> if (deck.attempts > 0) deck.bestMastery else null }
        .averageIntOrZero()

    return (6 downTo 0).map { dayOffset ->
        val date = today.minusDays(dayOffset.toLong())
        val value = when (trend) {
            TrendType.QUIZ_PERFORMANCE -> quizScore
            TrendType.FLASHCARD_MASTERY -> flashcardMastery
            TrendType.REVIEW_ACTIVITY -> recentlyReviewed.count { notebook ->
                notebook.lastReviewedAt.isSameLocalDate(date, zoneId)
            }.toFloat()
            TrendType.EDIT_ACTIVITY -> recentlyEdited.count { notebook ->
                notebook.updatedAt.isSameLocalDate(date, zoneId)
            }.toFloat()
        }

        TrendPoint(
            label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
            value = value
        )
    }
}

private fun List<Int>.averageIntOrZero(): Float =
    if (isEmpty()) 0f else average().toFloat()

private fun List<Float>.averageFloatOrZero(): Float =
    if (isEmpty()) 0f else average().toFloat()

private fun String?.isSameLocalDate(date: LocalDate, zoneId: ZoneId): Boolean {
    if (isNullOrBlank()) {
        return false
    }

    return runCatching {
        Instant.parse(this).atZone(zoneId).toLocalDate() == date
    }.getOrElse { false }
}
