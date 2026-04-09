package edu.cit.gako.brainbox.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.ui.theme.Cream2
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.White

@Composable
internal fun TokenBadge(text: String, selected: Boolean = false) {
    Surface(shape = RoundedCornerShape(10.dp), color = if (selected) White.copy(alpha = 0.12f) else Cream2) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) White else Ink2
        )
    }
}

@Composable
internal fun ProfileAvatar(username: String) {
    val initials = username.take(2).uppercase().ifBlank { "BB" }
    Surface(modifier = Modifier.size(52.dp), shape = androidx.compose.foundation.shape.CircleShape, color = Cream2) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                initials,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                color = Ink
            )
        }
    }
}

