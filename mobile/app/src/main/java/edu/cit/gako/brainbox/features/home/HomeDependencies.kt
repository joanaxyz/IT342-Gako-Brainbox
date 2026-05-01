package edu.cit.gako.brainbox.features.home

import edu.cit.gako.brainbox.features.home.library.data.LibraryRepository
import edu.cit.gako.brainbox.features.home.playlists.data.PlaylistRepository

data class HomeDependencies(
    val libraryRepository: LibraryRepository,
    val playlistRepository: PlaylistRepository
)
