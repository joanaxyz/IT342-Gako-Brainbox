package edu.cit.gako.brainbox.features.home.dashboard

import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ProgressTrendsCardTest {
    @Test
    fun quizTrendUsesTypedSummaryScores() {
        val points = generateChartData(
            trend = TrendType.QUIZ_PERFORMANCE,
            quizData = listOf(
                QuizSummary(uuid = "quiz-1", title = "Biology", attempts = 2, bestScore = 80),
                QuizSummary(uuid = "quiz-2", title = "History", attempts = 1, bestScore = 100),
                QuizSummary(uuid = "quiz-3", title = "Draft", attempts = 0, bestScore = 20)
            ),
            flashcardData = emptyList(),
            recentlyReviewed = emptyList(),
            recentlyEdited = emptyList(),
            today = LocalDate.of(2026, 5, 27),
            zoneId = ZoneId.of("UTC")
        )

        assertEquals(7, points.size)
        assertEquals("Wed", points.last().label)
        points.forEach { point ->
            assertEquals(90f, point.value, 0.001f)
        }
    }

    @Test
    fun reviewTrendBucketsNotebookReviewsByDay() {
        val points = generateChartData(
            trend = TrendType.REVIEW_ACTIVITY,
            quizData = emptyList(),
            flashcardData = emptyList(),
            recentlyReviewed = listOf(
                NotebookSummary(uuid = "today", title = "Today", lastReviewedAt = "2026-05-27T01:00:00Z"),
                NotebookSummary(uuid = "monday", title = "Monday", lastReviewedAt = "2026-05-25T12:00:00Z"),
                NotebookSummary(uuid = "old", title = "Old", lastReviewedAt = "2026-05-20T12:00:00Z"),
                NotebookSummary(uuid = "invalid", title = "Invalid", lastReviewedAt = "recently")
            ),
            recentlyEdited = emptyList(),
            today = LocalDate.of(2026, 5, 27),
            zoneId = ZoneId.of("UTC")
        )

        assertEquals(listOf("Thu", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed"), points.map { it.label })
        assertEquals(listOf(0f, 0f, 0f, 0f, 1f, 0f, 1f), points.map { it.value })
    }

    @Test
    fun emptyTrendDataStaysAtZero() {
        val points = generateChartData(
            trend = TrendType.FLASHCARD_MASTERY,
            quizData = emptyList(),
            flashcardData = emptyList(),
            recentlyReviewed = emptyList(),
            recentlyEdited = emptyList(),
            today = LocalDate.of(2026, 5, 27),
            zoneId = ZoneId.of("UTC")
        )

        assertTrue(points.all { it.value == 0f })
    }
}
