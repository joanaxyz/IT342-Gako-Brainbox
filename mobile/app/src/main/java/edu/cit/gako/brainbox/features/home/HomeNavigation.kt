package edu.cit.gako.brainbox.features.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.app.HomeTab
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Cream
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.White

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
            Icon(
                imageVector = homeTabIcon(tab),
                contentDescription = null,
                tint = if (selected) Cream else Ink2,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun homeTabIcon(tab: HomeTab): ImageVector {
    return when (tab) {
        HomeTab.DASHBOARD -> Icons.Rounded.Dashboard
        HomeTab.LIBRARY -> Icons.AutoMirrored.Rounded.LibraryBooks
        HomeTab.QUIZZES -> Icons.Rounded.Quiz
        HomeTab.FLASHCARDS -> Icons.Rounded.Style
        HomeTab.PLAYLISTS -> Icons.Rounded.Headphones
        HomeTab.PROFILE -> Icons.Rounded.AccountCircle
    }
}
