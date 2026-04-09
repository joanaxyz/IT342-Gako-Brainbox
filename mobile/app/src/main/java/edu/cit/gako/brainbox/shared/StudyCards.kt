package edu.cit.gako.brainbox.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.network.models.PlaylistSummary
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream3
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White
import java.util.Locale

@Composable
internal fun ContinueLearningCard(notebook: NotebookSummary, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.widthIn(min = 280.dp, max = 320.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 8.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TokenBadge("CL")
                Column {
                    Text(
                        notebook.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(notebook.categoryName ?: "Notebook", style = MaterialTheme.typography.bodySmall, color = Ink3)
                }
            }
            Text(
                text = notebook.lastReviewedAt?.let { "Last reviewed ${formatShortDate(it)}" }
                    ?: "Resume where you left off",
                style = MaterialTheme.typography.bodySmall,
                color = Ink2
            )
            HorizontalDivider(color = Border)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${notebook.wordCount ?: 0} words", style = MaterialTheme.typography.labelMedium, color = Ink3)
                Text("Resume", style = MaterialTheme.typography.labelLarge, color = Accent)
            }
        }
    }
}

@Composable
internal fun StudyCard(
    title: String,
    description: String?,
    kicker: String,
    meta: List<String>,
    progress: Int?,
    action: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.widthIn(min = 280.dp, max = 320.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 8.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TokenBadge(kicker.take(2).uppercase(Locale.ENGLISH))
                Text(
                    kicker,
                    style = MaterialTheme.typography.labelMedium,
                    color = Ink3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(title, style = MaterialTheme.typography.titleLarge, color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!description.isNullOrBlank()) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = Ink2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(joinMeta(*meta.toTypedArray()), style = MaterialTheme.typography.bodySmall, color = Ink3)
            progress?.let {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { it / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp)),
                        color = progressColor(it),
                        trackColor = Cream3
                    )
                    Text("$it%", style = MaterialTheme.typography.labelMedium, color = progressColor(it))
                }
            }
            HorizontalDivider(color = Border)
            Text(action, style = MaterialTheme.typography.labelLarge, color = Accent)
        }
    }
}

@Composable
internal fun NotebookCard(notebook: NotebookSummary, action: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 6.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TokenBadge("NB")
                Text(notebook.categoryName ?: "Notebook", style = MaterialTheme.typography.bodySmall, color = Ink3)
            }
            Text(notebook.title, style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(
                text = joinMeta(
                    "${notebook.wordCount ?: 0} words",
                    notebook.updatedAt?.let(::formatShortDate) ?: "Just now"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Ink3
            )
            HorizontalDivider(color = Border)
            Text(action, style = MaterialTheme.typography.labelLarge, color = Accent)
        }
    }
}

@Composable
internal fun PlaylistCard(playlist: PlaylistSummary, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 6.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TokenBadge("PL")
                Text("Study playlist", style = MaterialTheme.typography.labelMedium, color = Ink3)
            }
            Text(playlist.title, style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(
                joinMeta("${playlist.queue.size} tracks", "Current index ${playlist.currentIndex + 1}"),
                style = MaterialTheme.typography.bodySmall,
                color = Ink3
            )
            HorizontalDivider(color = Border)
            Text("Play playlist", style = MaterialTheme.typography.labelLarge, color = Accent)
        }
    }
}

