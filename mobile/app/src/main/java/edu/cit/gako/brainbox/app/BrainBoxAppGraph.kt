package edu.cit.gako.brainbox.app

import android.content.Context
import edu.cit.gako.brainbox.features.auth.data.AuthApiService
import edu.cit.gako.brainbox.features.auth.data.AuthRepository
import edu.cit.gako.brainbox.features.home.data.HomeRepository
import edu.cit.gako.brainbox.features.home.flashcards.data.FlashcardApiService
import edu.cit.gako.brainbox.features.home.flashcards.data.FlashcardRepository
import edu.cit.gako.brainbox.features.home.library.data.LibraryRepository
import edu.cit.gako.brainbox.features.notebook.data.NotebookApiService
import edu.cit.gako.brainbox.features.playback.audio.BrainBoxAudioStore
import edu.cit.gako.brainbox.features.notebook.data.NotebookRepository
import edu.cit.gako.brainbox.features.playback.data.PlaybackQueueApiService
import edu.cit.gako.brainbox.features.playback.data.PlaybackQueueRepository
import edu.cit.gako.brainbox.features.home.playlists.data.PlaylistApiService
import edu.cit.gako.brainbox.features.home.playlists.data.PlaylistRepository
import edu.cit.gako.brainbox.features.home.quizzes.data.QuizApiService
import edu.cit.gako.brainbox.features.home.quizzes.data.QuizRepository
import edu.cit.gako.brainbox.platform.network.RetrofitClient
import edu.cit.gako.brainbox.platform.network.SessionManager

class BrainBoxAppGraph private constructor(context: Context) {
    private val appContext = context.applicationContext
    val context: Context get() = appContext

    val sessionManager: SessionManager by lazy { SessionManager(appContext) }
    private val authApiService: AuthApiService by lazy {
        RetrofitClient.create(AuthApiService::class.java, sessionManager)
    }
    private val notebookApiService: NotebookApiService by lazy {
        RetrofitClient.create(NotebookApiService::class.java, sessionManager)
    }
    private val quizApiService: QuizApiService by lazy {
        RetrofitClient.create(QuizApiService::class.java, sessionManager)
    }
    private val flashcardApiService: FlashcardApiService by lazy {
        RetrofitClient.create(FlashcardApiService::class.java, sessionManager)
    }
    private val playbackQueueApiService: PlaybackQueueApiService by lazy {
        RetrofitClient.create(PlaybackQueueApiService::class.java, sessionManager)
    }
    private val playlistApiService: PlaylistApiService by lazy {
        RetrofitClient.create(PlaylistApiService::class.java, sessionManager)
    }

    val authRepository: AuthRepository by lazy { AuthRepository(authApiService, sessionManager) }
    val homeRepository: HomeRepository by lazy {
        HomeRepository(
            authRepository = authRepository,
            notebookRepository = notebookRepository,
            quizRepository = quizRepository,
            flashcardRepository = flashcardRepository,
            playbackQueueRepository = playbackQueueRepository,
            playlistRepository = playlistRepository,
            sessionManager = sessionManager
        )
    }
    val notebookRepository: NotebookRepository by lazy { NotebookRepository(notebookApiService) }
    val quizRepository: QuizRepository by lazy { QuizRepository(quizApiService) }
    val flashcardRepository: FlashcardRepository by lazy { FlashcardRepository(flashcardApiService) }
    val playbackQueueRepository: PlaybackQueueRepository by lazy {
        PlaybackQueueRepository(playbackQueueApiService)
    }
    val playlistRepository: PlaylistRepository by lazy { PlaylistRepository(playlistApiService) }
    val libraryRepository: LibraryRepository by lazy {
        LibraryRepository(
            notebooks = notebookRepository
        )
    }
    val audioStore: BrainBoxAudioStore by lazy { BrainBoxAudioStore(appContext) }

    companion object {
        @Volatile
        private var instance: BrainBoxAppGraph? = null

        fun from(context: Context): BrainBoxAppGraph {
            return instance ?: synchronized(this) {
                instance ?: BrainBoxAppGraph(context).also { graph ->
                    instance = graph
                }
            }
        }
    }
}
