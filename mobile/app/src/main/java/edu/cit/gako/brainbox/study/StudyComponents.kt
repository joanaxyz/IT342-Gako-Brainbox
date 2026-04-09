package edu.cit.gako.brainbox.study

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.shared.EmptyStateCard
import edu.cit.gako.brainbox.shared.OutlinedActionButton
import edu.cit.gako.brainbox.shared.TokenBadge
import edu.cit.gako.brainbox.shared.progressColor
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream
import edu.cit.gako.brainbox.ui.theme.Cream3
import edu.cit.gako.brainbox.ui.theme.ErrorRed
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.SuccessGreen
import edu.cit.gako.brainbox.ui.theme.White

@Composable
internal fun StudyUnavailableScreen(
    badge: String,
    title: String,
    body: String,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StudySessionHeader(
            badge = badge,
            title = title,
            subtitle = "This study item needs more content before it can run on mobile.",
            onExit = onExit
        )
        EmptyStateCard(title, body)
        OutlinedActionButton("Back", onClick = onExit)
    }
}

@Composable
internal fun StudySessionHeader(
    badge: String,
    title: String,
    subtitle: String,
    onExit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TokenBadge(badge)
        TextButton(onClick = onExit) {
            Text("Exit", color = Accent, style = MaterialTheme.typography.labelLarge)
        }
    }
    Text(title, style = MaterialTheme.typography.headlineLarge, color = Ink)
    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Ink2)
}

@Composable
internal fun StudyProgressCard(
    progress: Float,
    progressLabel: String,
    helper: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Progress", style = MaterialTheme.typography.titleMedium, color = Ink)
                Text(progressLabel, style = MaterialTheme.typography.labelLarge, color = Ink3)
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = Accent,
                trackColor = Cream3
            )
            Text(helper, style = MaterialTheme.typography.bodySmall, color = Ink3)
        }
    }
}

@Composable
internal fun StudyResultHero(
    percentage: Int,
    title: String,
    subtitle: String
) {
    val tone = progressColor(percentage)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(96.dp),
                    color = Cream3,
                    strokeWidth = 8.dp
                )
                CircularProgressIndicator(
                    progress = { (percentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.size(96.dp),
                    color = tone,
                    strokeWidth = 8.dp
                )
                Text("$percentage%", style = MaterialTheme.typography.headlineSmall, color = tone)
            }
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Ink)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Ink2, textAlign = TextAlign.Center)
        }
    }
}

@Composable
internal fun QuizOptionCard(
    label: String,
    option: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isIncorrectSelection: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isCorrect -> SuccessGreen.copy(alpha = 0.35f)
        isIncorrectSelection -> ErrorRed.copy(alpha = 0.3f)
        isSelected -> Accent.copy(alpha = 0.35f)
        else -> Border
    }
    val containerColor = when {
        isCorrect -> SuccessGreen.copy(alpha = 0.12f)
        isIncorrectSelection -> ErrorRed.copy(alpha = 0.1f)
        isSelected -> Accent.copy(alpha = 0.08f)
        else -> White
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isSelected && enabled) 4.dp else 0.dp,
        onClick = {
            if (enabled) {
                onClick()
            }
        }
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TokenBadge(label)
            Text(
                text = option,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = Ink
            )
            if (isCorrect) {
                Text("Correct", style = MaterialTheme.typography.labelMedium, color = SuccessGreen)
            } else if (isIncorrectSelection) {
                Text("Selected", style = MaterialTheme.typography.labelMedium, color = ErrorRed)
            }
        }
    }
}

