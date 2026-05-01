package edu.cit.gako.brainbox.features.home.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import edu.cit.gako.brainbox.app.HomeData
import edu.cit.gako.brainbox.features.home.profile.data.dto.UserProfile
import edu.cit.gako.brainbox.shared.ui.PrimaryActionButton
import edu.cit.gako.brainbox.shared.ui.ProfileAvatar
import edu.cit.gako.brainbox.shared.ui.ProfileDetailRow
import edu.cit.gako.brainbox.shared.ui.formatLongDate
import edu.cit.gako.brainbox.shared.ui.joinMeta
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.White

@Composable
internal fun ProfileScreen(
    user: UserProfile?,
    homeData: HomeData,
    contentPadding: PaddingValues,
    onLogout: () -> Unit
) {
    val profile = user ?: UserProfile("BrainBox User", "", null)

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
                            Text(profile.email.ifBlank { "Email is not available yet." }, style = MaterialTheme.typography.bodySmall, color = Ink3)
                        }
                    }
                    HorizontalDivider(color = Border)
                    ProfileDetailRow("Joined", profile.createdAt?.let(::formatLongDate) ?: "Recently")
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
                    Text("Account", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Text(
                        "Mobile keeps your workspace current whenever it can reach the server.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                    PrimaryActionButton("Log out", isBusy = false, onClick = onLogout)
                }
            }
        }
    }
}


