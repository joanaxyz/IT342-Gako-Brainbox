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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.White

@Composable
internal fun StudyProgressCard(
    topDecks: List<TopPerformer> = emptyList(),
    topQuizzes: List<TopPerformer> = emptyList()
) {
    if (topDecks.isEmpty() && topQuizzes.isEmpty()) {
        return
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
                text = "Study Progress",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                fontWeight = FontWeight.SemiBold
            )

            if (topDecks.isNotEmpty()) {
                ProgressPanel(
                    icon = Icons.Default.Style,
                    title = "Flashcard Mastery",
                    performers = topDecks
                )
            }

            if (topQuizzes.isNotEmpty()) {
                ProgressPanel(
                    icon = Icons.Default.EmojiEvents,
                    title = "Quiz Best Scores",
                    performers = topQuizzes
                )
            }
        }
    }
}

@Composable
private fun ProgressPanel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    performers: List<TopPerformer>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            performers.forEach { performer ->
                ProgressRow(
                    title = performer.title,
                    source = performer.source,
                    value = performer.value
                )
            }
        }
    }
}

@Composable
private fun ProgressRow(
    title: String,
    source: String,
    value: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = source,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink2
                )
            }
            Text(
                text = "$value%",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFC2410C),
                fontWeight = FontWeight.Bold
            )
        }

        // Progress bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            shape = RoundedCornerShape(3.dp),
            color = Ink2.copy(alpha = 0.2f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(value.toFloat() / 100f)
                    .height(6.dp),
                shape = RoundedCornerShape(3.dp),
                color = Color(0xFFC2410C)
            ) {}
        }
    }
}

data class TopPerformer(
    val title: String,
    val source: String,
    val value: Int
)
