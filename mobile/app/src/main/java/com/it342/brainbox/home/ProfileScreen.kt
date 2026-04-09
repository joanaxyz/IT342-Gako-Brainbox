package com.it342.brainbox.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.it342.brainbox.app.HomeData
import com.it342.brainbox.network.models.UserProfile
import com.it342.brainbox.shared.OutlinedActionButton
import com.it342.brainbox.shared.PrimaryActionButton
import com.it342.brainbox.shared.ProfileAvatar
import com.it342.brainbox.shared.ProfileDetailRow
import com.it342.brainbox.shared.formatLongDate
import com.it342.brainbox.shared.joinMeta
import com.it342.brainbox.ui.theme.Border
import com.it342.brainbox.ui.theme.Ink
import com.it342.brainbox.ui.theme.Ink2
import com.it342.brainbox.ui.theme.Ink3
import com.it342.brainbox.ui.theme.White

@Composable
internal fun ProfileScreen(
    user: UserProfile?,
    homeData: HomeData,
    contentPadding: PaddingValues,
    onRefreshHome: () -> Unit,
    onLogout: () -> Unit
) {
    val profile = user ?: UserProfile("BrainBox User", "", null)

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Profile", style = MaterialTheme.typography.headlineLarge, color = Ink)
            Spacer(modifier = Modifier.height(6.dp))
            Text("The same account, reshaped for the mobile shell.", style = MaterialTheme.typography.bodyMedium, color = Ink2)
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                color = White,
                border = BorderStroke(1.dp, Border),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileAvatar(profile.username)
                        Column {
                            Text(profile.username, style = MaterialTheme.typography.titleLarge, color = Ink)
                            Text(
                                text = profile.email.ifBlank { "Email sync is still catching up." },
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink3
                            )
                        }
                    }
                    HorizontalDivider(color = Border)
                    ProfileDetailRow("Joined", profile.createdAt?.let(::formatLongDate) ?: "Recently")
                    ProfileDetailRow("Last sync", homeData.syncedAtLabel ?: "Waiting for first sync")
                    ProfileDetailRow(
                        "Workspace",
                        joinMeta(
                            "${homeData.notebooks.size} notebooks",
                            "${homeData.quizzes.size} quizzes",
                            "${homeData.flashcards.size} decks"
                        )
                    )
                }
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                color = White,
                border = BorderStroke(1.dp, Border)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Home controls", style = MaterialTheme.typography.titleMedium, color = Ink)
                    OutlinedActionButton("Sync now", onClick = onRefreshHome)
                    PrimaryActionButton("Log out", isBusy = false, onClick = onLogout)
                }
            }
        }
    }
}

