package edu.cit.gako.brainbox.features.home

import edu.cit.gako.brainbox.features.home.flashcards.data.FlashcardRepository
import edu.cit.gako.brainbox.features.home.library.data.LibraryRepository
import edu.cit.gako.brainbox.features.home.playlists.data.PlaylistRepository
import edu.cit.gako.brainbox.features.home.quizzes.data.QuizRepository

data class HomeDependencies(
    val libraryRepository: LibraryRepository,
    val playlistRepository: PlaylistRepository,
    val quizRepository: QuizRepository,
    val flashcardRepository: FlashcardRepository
)
