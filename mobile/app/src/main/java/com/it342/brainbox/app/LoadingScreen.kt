package com.it342.brainbox.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.it342.brainbox.ui.theme.Accent
import com.it342.brainbox.ui.theme.Border
import com.it342.brainbox.ui.theme.Cream
import com.it342.brainbox.ui.theme.Ink
import com.it342.brainbox.ui.theme.Ink3
import com.it342.brainbox.ui.theme.Ink4
import com.it342.brainbox.ui.theme.White
import kotlin.math.pow

private const val LoadingCycleDurationMs = 3000
private val CubeTop = Color(0xFFC4BDB4)
private val CubeMid = Color(0xFF6B6560)
private val CubeDark = Color(0xFF1A1714)

private data class LoaderKeyframe(
    val progress: Float,
    val rotationZ: Float,
    val scaleX: Float,
    val scaleY: Float,
    val shadowScaleX: Float,
    val shadowScaleY: Float,
    val shadowAlpha: Float
)

private data class LoaderVisualState(
    val rotationZ: Float,
    val scaleX: Float,
    val scaleY: Float,
    val shadowScaleX: Float,
    val shadowScaleY: Float,
    val shadowAlpha: Float,
    val turnIndex: Int
)

private val LoaderKeyframes = listOf(
    LoaderKeyframe(0f, 0f, 1f, 1f, 1f, 1f, 0.46f),
    LoaderKeyframe(0.25f, 45f, 0.72f, 1.46f, 0.68f, 0.94f, 0.22f),
    LoaderKeyframe(0.5f, 90f, 1f, 1f, 1f, 1f, 0.46f),
    LoaderKeyframe(0.75f, 135f, 1.46f, 0.72f, 1.38f, 1.06f, 0.68f),
    LoaderKeyframe(1f, 180f, 1f, 1f, 1f, 1f, 0.46f)
)

@Composable
internal fun LoadingScreen() {
    val transition = rememberInfiniteTransition(label = "brainbox-loader")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = LoadingCycleDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "brainbox-loader-progress"
    )
    val loaderState = remember(progress) { interpolateLoaderState(progress) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Accent.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(280f, 260f),
                        radius = 1100f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            LoadingCube(state = loaderState)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = White.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, Border),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Preparing your workspace",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Syncing the familiar BrainBox study flow from the web so your notes, decks, and quizzes are ready on mobile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink3,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Same motion. Same palette. Pocket-sized focus.",
                        style = MaterialTheme.typography.labelMedium,
                        color = Ink4
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingCube(state: LoaderVisualState) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(144.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Accent.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(126.dp)
                    .graphicsLayer {
                        rotationX = -18f
                        rotationZ = state.rotationZ
                        scaleX = state.scaleX
                        scaleY = state.scaleY
                    }
            ) {
                drawCube(turnIndex = state.turnIndex)
            }
            Box(
                modifier = Modifier
                    .width(92.dp)
                    .height(16.dp)
                    .graphicsLayer {
                        scaleX = state.shadowScaleX
                        scaleY = state.shadowScaleY
                        alpha = state.shadowAlpha
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.58f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

private fun DrawScope.drawCube(turnIndex: Int) {
    val top = Path().apply {
        moveTo(size.width * 0.5f, size.height * 0.12f)
        lineTo(size.width * 0.84f, size.height * 0.28f)
        lineTo(size.width * 0.5f, size.height * 0.42f)
        lineTo(size.width * 0.16f, size.height * 0.28f)
        close()
    }
    val left = Path().apply {
        moveTo(size.width * 0.16f, size.height * 0.28f)
        lineTo(size.width * 0.5f, size.height * 0.42f)
        lineTo(size.width * 0.5f, size.height * 0.82f)
        lineTo(size.width * 0.16f, size.height * 0.66f)
        close()
    }
    val right = Path().apply {
        moveTo(size.width * 0.5f, size.height * 0.42f)
        lineTo(size.width * 0.84f, size.height * 0.28f)
        lineTo(size.width * 0.84f, size.height * 0.66f)
        lineTo(size.width * 0.5f, size.height * 0.82f)
        close()
    }

    val strokeWidth = size.minDimension * 0.035f
    val leftColor = if (turnIndex % 2 == 0) CubeMid else CubeDark
    val rightColor = if (turnIndex % 2 == 0) CubeDark else CubeMid

    drawPath(path = top, color = CubeTop)
    drawPath(path = left, color = leftColor)
    drawPath(path = right, color = rightColor)
    drawPath(path = top, color = CubeDark, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    drawPath(path = left, color = CubeDark, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    drawPath(path = right, color = CubeDark, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

private fun interpolateLoaderState(progress: Float): LoaderVisualState {
    val normalized = progress.coerceIn(0f, 1f)
    val startIndex = LoaderKeyframes.indexOfLast { normalized >= it.progress }
        .coerceAtMost(LoaderKeyframes.lastIndex - 1)
        .coerceAtLeast(0)
    val start = LoaderKeyframes[startIndex]
    val end = LoaderKeyframes[startIndex + 1]
    val segmentSpan = (end.progress - start.progress).coerceAtLeast(0.0001f)
    val segmentProgress = ((normalized - start.progress) / segmentSpan).coerceIn(0f, 1f)
    val eased = easeInOutCubic(segmentProgress)

    return LoaderVisualState(
        rotationZ = lerp(start.rotationZ, end.rotationZ, eased),
        scaleX = lerp(start.scaleX, end.scaleX, eased),
        scaleY = lerp(start.scaleY, end.scaleY, eased),
        shadowScaleX = lerp(start.shadowScaleX, end.shadowScaleX, eased),
        shadowScaleY = lerp(start.shadowScaleY, end.shadowScaleY, eased),
        shadowAlpha = lerp(start.shadowAlpha, end.shadowAlpha, eased),
        turnIndex = ((normalized * 4f).toInt()).mod(4)
    )
}

private fun easeInOutCubic(progress: Float): Float {
    val clamped = progress.coerceIn(0f, 1f)
    return if (clamped < 0.5f) {
        4f * clamped.pow(3)
    } else {
        1f - ((-2f * clamped + 2f).pow(3) / 2f)
    }
}
