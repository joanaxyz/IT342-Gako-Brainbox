package edu.cit.gako.brainbox.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream2
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.White
import java.util.Locale

@Composable
internal fun BrainBoxPlaybar(
    title: String,
    supportingText: String,
    eyebrow: String,
    progressText: String,
    progress: Float,
    isPlaying: Boolean,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    artworkLabel: String = title.trim().take(1).uppercase(Locale.ENGLISH).ifBlank { "B" },
    artworkIcon: ImageVector = Icons.AutoMirrored.Filled.MenuBook,
    artworkBrush: Brush = SolidColor(AccentBg),
    artworkContentTint: Color = Accent,
    onOpen: (() -> Unit)? = null,
    leadingActions: @Composable RowScope.() -> Unit = {},
    trailingActions: @Composable RowScope.() -> Unit = {}
) {
    val progressFraction = progress.coerceIn(0f, 1f)
    val infoModifier = if (onOpen != null) {
        Modifier.clickable(onClick = onOpen)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 18.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = eyebrow.uppercase(Locale.ENGLISH),
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink3,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(Cream2, RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .background(Accent, RoundedCornerShape(999.dp))
                    )
                }
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.labelMedium,
                    color = Ink2,
                    maxLines = 1
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlaybarArtwork(
                    label = artworkLabel,
                    icon = artworkIcon,
                    brush = artworkBrush,
                    tint = artworkContentTint
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(infoModifier),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    content = {
                        leadingActions()
                        PlaybarPrimaryButton(
                            isPlaying = isPlaying,
                            enabled = primaryEnabled,
                            onClick = onPrimaryAction
                        )
                        trailingActions()
                    }
                )
            }
        }
    }
}

@Composable
internal fun BrainBoxPlaybarIconAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasized: Boolean = false,
    rotation: Float = 0f
) {
    PlaybarActionSurface(
        onClick = onClick,
        enabled = enabled,
        emphasized = emphasized,
        modifier = modifier,
        shape = CircleShape
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled) {
                if (emphasized) White else Ink2
            } else {
                Ink3
            },
            modifier = Modifier
                .size(18.dp)
                .rotate(rotation)
        )
    }
}

@Composable
internal fun BrainBoxPlaybarPillAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasized: Boolean = false
) {
    PlaybarActionSurface(
        onClick = onClick,
        enabled = enabled,
        emphasized = emphasized,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) {
                if (emphasized) White else Ink
            } else {
                Ink3
            }
        )
    }
}

@Composable
private fun PlaybarArtwork(
    label: String,
    icon: ImageVector,
    brush: Brush,
    tint: Color = White
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(brush, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label.take(2),
                style = MaterialTheme.typography.titleLarge,
                color = tint,
                maxLines = 1
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun PlaybarPrimaryButton(
    isPlaying: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = Ink,
        shadowElevation = if (enabled) 8.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause playback" else "Start playback",
                tint = White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PlaybarActionSurface(
    onClick: () -> Unit,
    enabled: Boolean,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = shape,
        color = if (emphasized) Ink else White,
        border = BorderStroke(1.dp, if (emphasized) Ink else Border)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
