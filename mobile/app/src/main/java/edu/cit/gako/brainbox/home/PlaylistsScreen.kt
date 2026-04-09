package edu.cit.gako.brainbox.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.home.SimpleHomePage
import edu.cit.gako.brainbox.network.models.PlaylistSummary
import edu.cit.gako.brainbox.shared.BrandedSearchField
import edu.cit.gako.brainbox.shared.EmptyStateCard
import edu.cit.gako.brainbox.shared.PlaylistCard

@Composable
internal fun PlaylistsScreen(
    playlists: List<PlaylistSummary>,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onFeatureRequest: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = playlists.filter {
        query.isBlank() || it.title.contains(other = query, ignoreCase = true)
    }

    SimpleHomePage(
        title = "Playlists",
        description = "Study playlists stay warm and tactile on mobile, with the same soft surfaces from the web.",
        syncNotice = syncNotice,
        syncedAtLabel = syncedAtLabel,
        contentPadding = contentPadding
    ) {
        BrandedSearchField(query, { query = it }, "Search playlists")
        Spacer(modifier = Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            EmptyStateCard("No playlists yet", "Once playlists are available in the API, they'll land here in a mobile-first layout.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.forEach { playlist ->
                    PlaylistCard(playlist) {
                        onFeatureRequest("Playlist playback is still being wired into mobile.")
                    }
                }
            }
        }
    }
}


