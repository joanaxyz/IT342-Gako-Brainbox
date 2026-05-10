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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.White

data class StreakData(
    val current: Int,
    val longest: Int,
    val totalDays: Int,
    val consistency: Int
)

@Composable
internal fun StudyStreakCard(
    reviewData: List<NotebookSummary> = emptyList(),
    editData: List<NotebookSummary> = emptyList()
) {
    val streakData = calculateStreakData(reviewData, editData)
    val streakColor = getStreakColor(streakData.current)
    val streakIcon = getStreakIcon(streakData.current, streakColor)

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Study Streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold
                )
                streakIcon
            }

            // Main streak display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = streakData.current.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontSize = 48.sp,
                    color = streakColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "days",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink2
                )
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StreakStat(label = "longest", value = streakData.longest.toString())
                StreakStat(label = "total", value = streakData.totalDays.toString())
                StreakStat(label = "consistent", value = "${streakData.consistency}%")
            }

            // Weekly goal progress
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Weekly Goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Progress track
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Ink3.copy(alpha = 0.3f)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(
                                    fraction = (streakData.current.toFloat() / 7f).coerceAtMost(1f)
                                )
                                .height(8.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = streakColor
                        ) {}
                    }
                    Text(
                        text = "${streakData.current}/7 days this week",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink2
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakStat(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = value,
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

@Composable
private fun getStreakIcon(streak: Int, color: Color): ImageVector {
    return when {
        streak >= 30 -> Icons.Default.LocalFireDepartment
        streak >= 14 -> Icons.Default.Star
        streak >= 7 -> Icons.Default.Star
        streak >= 3 -> Icons.Default.Star
        else -> Icons.Default.Book
    }
}

private fun getStreakColor(streak: Int): Color {
    return when {
        streak >= 30 -> Color(0xFFC2410C) // accent-dark
        streak >= 14 -> Color(0xFFEA580C) // accent
        streak >= 7 -> Color(0xFFF97316) // accent-2
        streak >= 3 -> Color(0xFF57534E) // ink-2
        else -> Color(0xFF78716C) // ink-3
    }
}

private fun calculateStreakData(
    reviewData: List<NotebookSummary>,
    editData: List<NotebookSummary>
): StreakData {
    // Simplified streak calculation based on activity count
    val totalActivities = reviewData.size + editData.size
    val currentStreak = if (totalActivities > 0) minOf(totalActivities, 7) else 0
    val longestStreak = if (totalActivities > 0) minOf(totalActivities, 30) else 0
    val totalDays = if (totalActivities > 0) minOf(totalActivities, 30) else 0
    val consistency = if (totalActivities > 0) minOf((totalActivities * 3), 100) else 0

    return StreakData(
        current = currentStreak,
        longest = longestStreak,
        totalDays = totalDays,
        consistency = consistency
    )
}
