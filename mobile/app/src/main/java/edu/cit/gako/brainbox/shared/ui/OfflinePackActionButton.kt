package edu.cit.gako.brainbox.shared.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.ErrorRed
import edu.cit.gako.brainbox.shared.ui.theme.White

@Composable
internal fun OfflinePackActionButton(
    isOffline: Boolean,
    isWorking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = if (isOffline) Accent.copy(alpha = 0.1f) else White,
        border = BorderStroke(
            width = 1.dp,
            color = if (isOffline) Accent.copy(alpha = 0.35f) else Border
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = !isWorking, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isWorking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Accent
                )
            } else {
                Icon(
                    imageVector = if (isOffline) Icons.Filled.Delete else Icons.Filled.ArrowDownward,
                    contentDescription = if (isOffline) "Remove offline" else "Download for offline",
                    tint = if (isOffline) ErrorRed else Accent
                )
            }
        }
    }
}
