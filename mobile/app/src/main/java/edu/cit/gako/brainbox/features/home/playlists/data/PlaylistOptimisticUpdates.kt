package edu.cit.gako.brainbox.features.home.playlists.data

import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistSummary

internal fun List<PlaylistSummary>.withOptimisticPlaylist(playlist: PlaylistSummary): List<PlaylistSummary> =
    if (any { it.uuid == playlist.uuid }) {
        map { currentPlaylist -> if (currentPlaylist.uuid == playlist.uuid) playlist else currentPlaylist }
    } else {
        this + playlist
    }

internal fun List<PlaylistSummary>.replaceOptimisticPlaylist(
    temporaryUuid: String,
    playlist: PlaylistSummary
): List<PlaylistSummary> =
    map { currentPlaylist -> if (currentPlaylist.uuid == temporaryUuid) playlist else currentPlaylist }

internal fun List<PlaylistSummary>.withoutPlaylist(playlistUuid: String): List<PlaylistSummary> =
    filterNot { it.uuid == playlistUuid }

internal fun List<PlaylistSummary>.withNotebookInPlaylist(
    playlistUuid: String,
    notebook: NotebookSummary
): List<PlaylistSummary> =
    map { playlist ->
        if (playlist.uuid != playlistUuid || playlist.queue.any { it.uuid == notebook.uuid }) {
            playlist
        } else {
            playlist.copy(queue = playlist.queue + notebook)
        }
    }

internal fun List<PlaylistSummary>.withoutNotebookInPlaylist(
    playlistUuid: String,
    notebookUuid: String
): List<PlaylistSummary> =
    map { playlist ->
        if (playlist.uuid != playlistUuid) {
            playlist
        } else {
            val nextQueue = playlist.queue.filterNot { it.uuid == notebookUuid }
            playlist.copy(
                queue = nextQueue,
                currentIndex = normalizePlaylistIndex(playlist.currentIndex, nextQueue.size)
            )
        }
    }

internal fun List<PlaylistSummary>.withPlaylistQueueOrder(
    playlistUuid: String,
    notebookUuids: List<String>
): List<PlaylistSummary> =
    map { playlist ->
        if (playlist.uuid != playlistUuid) {
            playlist
        } else {
            val queuedByUuid = playlist.queue.associateBy { it.uuid }
            val nextQueue = notebookUuids.mapNotNull { queuedByUuid[it] }
            playlist.copy(
                queue = nextQueue,
                currentIndex = normalizePlaylistIndex(playlist.currentIndex, nextQueue.size)
            )
        }
    }

private fun normalizePlaylistIndex(currentIndex: Int, queueSize: Int): Int =
    if (queueSize <= 0) 0 else currentIndex.coerceIn(0, queueSize - 1)
