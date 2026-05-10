package edu.cit.gako.brainbox.features.home.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.White
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi

enum class TrendType(val label: String, val description: String) {
    QUIZ_PERFORMANCE("Quiz Performance", "Quiz scores & attempts"),
    FLASHCARD_MASTERY("Flashcard Mastery", "Mastery levels & attempts"),
    REVIEW_ACTIVITY("Review Activity", "Notebook reviews"),
    EDIT_ACTIVITY("Edit Activity", "Notebook edits")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ProgressTrendsCard(
    quizData: List<Any> = emptyList(),
    flashcardData: List<Any> = emptyList(),
    recentlyReviewed: List<NotebookSummary> = emptyList(),
    recentlyEdited: List<NotebookSummary> = emptyList()
) {
    var selectedTrend by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { TrendType.entries.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedTrend) {
        pagerState.animateScrollToPage(selectedTrend)
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != selectedTrend) {
            selectedTrend = pagerState.currentPage
        }
    }

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
                            selectedTrend = index
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
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
    quizData: List<Any>,
    flashcardData: List<Any>,
    recentlyReviewed: List<NotebookSummary>,
    recentlyEdited: List<NotebookSummary>
) {
    val chartData = remember(trend, quizData, flashcardData, recentlyReviewed, recentlyEdited) {
        generateChartData(trend, quizData, flashcardData, recentlyReviewed, recentlyEdited)
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
            chartData.forEachIndexed { index, value ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val barHeight = (value / (chartData.maxOrNull() ?: 1f)) * 100f
                    Surface(
                        modifier = Modifier
                            .width(24.dp)
                            .height((barHeight * 1.5f).dp.coerceAtLeast(20.dp)),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFC2410C)
                    ) {}
                    Text(
                        text = getDayLabel(index),
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
            TrendStat(label = "Current", value = chartData.lastOrNull()?.toInt() ?: 0)
            TrendStat(label = "Average", value = chartData.average().toInt())
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

private fun generateChartData(
    trend: TrendType,
    quizData: List<Any>,
    flashcardData: List<Any>,
    recentlyReviewed: List<NotebookSummary>,
    recentlyEdited: List<NotebookSummary>
): List<Float> {
    val data = mutableListOf<Float>()

    // Generate simplified 7-day trend data
    for (i in 6 downTo 0) {
        var value = 0f

        when (trend) {
            TrendType.QUIZ_PERFORMANCE -> {
                value = if (quizData.isNotEmpty()) {
                    try {
                        val scores = quizData.mapNotNull { item ->
                            try {
                                (item as? Map<*, *>)?.get("bestScore") as? Int
                            } catch (e: Exception) { null }
                        }
                        if (scores.isNotEmpty()) scores.average().toFloat() else 0f
                    } catch (e: Exception) { 0f }
                } else 0f
            }
            TrendType.FLASHCARD_MASTERY -> {
                value = if (flashcardData.isNotEmpty()) {
                    try {
                        val mastery = flashcardData.mapNotNull { item ->
                            try {
                                (item as? Map<*, *>)?.get("bestMastery") as? Int
                            } catch (e: Exception) { null }
                        }
                        if (mastery.isNotEmpty()) mastery.average().toFloat() else 0f
                    } catch (e: Exception) { 0f }
                } else 0f
            }
            TrendType.REVIEW_ACTIVITY -> {
                // Simplified: distribute review count across days
                value = if (recentlyReviewed.isNotEmpty()) {
                    (recentlyReviewed.size.toFloat() / 7f) * (1f + (6 - i) * 0.1f)
                } else 0f
            }
            TrendType.EDIT_ACTIVITY -> {
                // Simplified: distribute edit count across days
                value = if (recentlyEdited.isNotEmpty()) {
                    (recentlyEdited.size.toFloat() / 7f) * (1f + (6 - i) * 0.1f)
                } else 0f
            }
        }
        data.add(value)
    }

    return data
}

private fun getDayLabel(index: Int): String {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return days[index % days.size]
}
