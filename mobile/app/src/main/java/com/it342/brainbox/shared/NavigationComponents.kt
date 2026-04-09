package com.it342.brainbox.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.it342.brainbox.app.HomeTab
import com.it342.brainbox.ui.theme.Border
import com.it342.brainbox.ui.theme.Cream
import com.it342.brainbox.ui.theme.Ink
import com.it342.brainbox.ui.theme.Ink2
import com.it342.brainbox.ui.theme.White

@Composable
internal fun BottomNavIconButton(
    tab: HomeTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Ink else White,
        border = BorderStroke(1.dp, if (selected) Ink else Border),
        shadowElevation = if (selected) 0.dp else 2.dp,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics(mergeDescendants = true) {
                    contentDescription = tab.label
                    role = Role.Tab
                    this.selected = selected
                },
            contentAlignment = Alignment.Center
        ) {
            HomeTabIcon(
                tab = tab,
                tint = if (selected) Cream else Ink2,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HomeTabIcon(
    tab: HomeTab,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = size.minDimension * 0.085f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        when (tab) {
            HomeTab.DASHBOARD -> {
                val cell = size.minDimension * 0.28f
                val gap = size.minDimension * 0.12f
                val start = size.minDimension * 0.13f
                val corners = CornerRadius(size.minDimension * 0.06f)
                listOf(
                    Offset(start, start),
                    Offset(start + cell + gap, start),
                    Offset(start, start + cell + gap),
                    Offset(start + cell + gap, start + cell + gap)
                ).forEach { offset ->
                    drawRoundRect(
                        color = tint,
                        topLeft = offset,
                        size = Size(cell, cell),
                        cornerRadius = corners,
                        style = stroke
                    )
                }
            }

            HomeTab.LIBRARY -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.22f, h * 0.12f),
                    size = Size(w * 0.56f, h * 0.76f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = stroke
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.38f, h * 0.16f),
                    end = Offset(w * 0.38f, h * 0.84f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.26f, h * 0.78f),
                    end = Offset(w * 0.74f, h * 0.78f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            HomeTab.QUIZZES -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.36f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = stroke
                )
                val questionMark = Path().apply {
                    moveTo(w * 0.41f, h * 0.36f)
                    cubicTo(w * 0.41f, h * 0.26f, w * 0.49f, h * 0.22f, w * 0.56f, h * 0.22f)
                    cubicTo(w * 0.66f, h * 0.22f, w * 0.72f, h * 0.29f, w * 0.72f, h * 0.38f)
                    cubicTo(w * 0.72f, h * 0.46f, w * 0.67f, h * 0.52f, w * 0.58f, h * 0.57f)
                    lineTo(w * 0.58f, h * 0.62f)
                }
                drawPath(path = questionMark, color = tint, style = stroke)
                drawCircle(
                    color = tint,
                    radius = stroke.width * 0.6f,
                    center = Offset(w * 0.58f, h * 0.75f)
                )
            }

            HomeTab.FLASHCARDS -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.14f, h * 0.2f),
                    size = Size(w * 0.72f, h * 0.6f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = stroke
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.36f, h * 0.2f),
                    end = Offset(w * 0.36f, h * 0.8f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            HomeTab.PLAYLISTS -> {
                drawLine(
                    color = tint,
                    start = Offset(w * 0.18f, h * 0.28f),
                    end = Offset(w * 0.68f, h * 0.28f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.18f, h * 0.5f),
                    end = Offset(w * 0.68f, h * 0.5f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.18f, h * 0.72f),
                    end = Offset(w * 0.56f, h * 0.72f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                val playIcon = Path().apply {
                    moveTo(w * 0.68f, h * 0.42f)
                    lineTo(w * 0.86f, h * 0.52f)
                    lineTo(w * 0.68f, h * 0.62f)
                    close()
                }
                drawPath(path = playIcon, color = tint, style = Fill)
            }

            HomeTab.PROFILE -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.15f,
                    center = Offset(w * 0.5f, h * 0.33f),
                    style = stroke
                )
                drawArc(
                    color = tint,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(w * 0.22f, h * 0.44f),
                    size = Size(w * 0.56f, h * 0.34f),
                    style = stroke
                )
            }
        }
    }
}
