package edu.cit.gako.brainbox.features.home.playlists.data

import edu.cit.gako.brainbox.platform.network.requireData
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistAddNotebookBody
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistCreateRequest
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistReorderBody
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistSummary

class PlaylistRepository(
    private val apiService: PlaylistApiService
) {
    suspend fun getPlaylists(): List<PlaylistSummary> =
        apiService.getPlaylistsEnvelope()
            .requireData("We couldn't load playlists.")

    suspend fun createPlaylist(title: String): PlaylistSummary =
        apiService.createPlaylistEnvelope(PlaylistCreateRequest(title))
            .requireData("Couldn't create playlist.")

    suspend fun deletePlaylist(uuid: String) {
        apiService.deletePlaylistEnvelope(uuid)
    }

    suspend fun addNotebookToPlaylist(playlistUuid: String, notebookUuid: String): PlaylistSummary =
        apiService.addNotebookToPlaylistEnvelope(playlistUuid, PlaylistAddNotebookBody(notebookUuid))
            .requireData("Couldn't add notebook to playlist.")

    suspend fun removeNotebookFromPlaylist(playlistUuid: String, notebookUuid: String): PlaylistSummary =
        apiService.removeNotebookFromPlaylistEnvelope(playlistUuid, notebookUuid)
            .requireData("Couldn't remove notebook from playlist.")

    suspend fun reorderPlaylist(playlistUuid: String, notebookUuids: List<String>): PlaylistSummary =
        apiService.reorderPlaylistEnvelope(playlistUuid, PlaylistReorderBody(notebookUuids))
            .requireData("Couldn't reorder playlist.")

    suspend fun setPlaylistCurrentIndex(playlistUuid: String, index: Int): PlaylistSummary =
        apiService.setPlaylistCurrentIndexEnvelope(playlistUuid, index)
            .requireData("Couldn't update playlist position.")
}
