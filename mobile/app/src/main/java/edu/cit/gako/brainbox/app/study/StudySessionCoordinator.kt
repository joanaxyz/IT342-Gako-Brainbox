package edu.cit.gako.brainbox.app.study

import android.content.Context
import com.google.gson.Gson
import edu.cit.gako.brainbox.app.AppState
import edu.cit.gako.brainbox.app.withFlashcardAttempt
import edu.cit.gako.brainbox.app.withQuizAttempt
import edu.cit.gako.brainbox.features.home.flashcards.data.FlashcardRepository
import edu.cit.gako.brainbox.features.home.quizzes.data.QuizRepository
import edu.cit.gako.brainbox.platform.local.model.OfflineEntityType
import edu.cit.gako.brainbox.platform.local.model.PendingMutation
import edu.cit.gako.brainbox.platform.local.model.PendingMutationOperation
import edu.cit.gako.brainbox.app.infrastructure.BrainBoxLocalInfrastructure
import edu.cit.gako.brainbox.app.worker.BrainBoxSyncWorkScheduler
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private data class QuizAttemptQueuePayload(
    val score: Int
)

private data class FlashcardAttemptQueuePayload(
    val mastery: Int
)

internal class StudySessionCoordinator(
    private val appContext: Context,
    private val quizRepository: QuizRepository,
    private val flashcardRepository: FlashcardRepository,
    private val localInfrastructure: BrainBoxLocalInfrastructure,
    private val scope: CoroutineScope,
    private val getState: () -> AppState,
    private val setState: (AppState) -> Unit,
    private val syncHome: suspend (setBusy: Boolean) -> Unit,
    private val syncStudyContext: (String?) -> Unit,
    private val showMessage: (String) -> Unit,
    private val gson: Gson = Gson()
) {
    fun handleOpenQuiz(uuid: String) {
        openQuiz(uuid, returnToNotebookUuid = null)
    }

    fun handleOpenQuizFromNotebook(uuid: String) {
        openQuiz(uuid, returnToNotebookUuid = getState().activeNotebookUuid)
    }

    fun handleOpenFlashcardDeck(uuid: String) {
        openFlashcardDeck(uuid, returnToNotebookUuid = null)
    }

    fun handleOpenFlashcardDeckFromNotebook(uuid: String) {
        openFlashcardDeck(uuid, returnToNotebookUuid = getState().activeNotebookUuid)
    }

    fun handleExitStudySession() {
        val returnNotebookUuid = getState().studyReturnNotebookUuid
        setState(
            getState().copy(
                activeQuiz = null,
                activeFlashcardDeck = null,
                activeNotebookUuid = returnNotebookUuid,
                studyReturnNotebookUuid = null
            )
        )
    }

    fun handleRecordQuizAttempt(uuid: String, score: Int) {
        setState(getState().copy(homeData = getState().homeData.withQuizAttempt(uuid, score)))
        scope.launch {
            runCatching {
                quizRepository.recordQuizAttempt(uuid, score)
                syncHome(false)
            }.onFailure {
                queueQuizAttempt(uuid, score)
                showMessage("Quiz score saved offline. We'll sync it when you're back online.")
            }
        }
    }

    fun handleRecordFlashcardAttempt(uuid: String, mastery: Int) {
        setState(getState().copy(homeData = getState().homeData.withFlashcardAttempt(uuid, mastery)))
        scope.launch {
            runCatching {
                flashcardRepository.recordFlashcardAttempt(uuid, mastery)
                syncHome(false)
            }.onFailure {
                queueFlashcardAttempt(uuid, mastery)
                showMessage("Deck progress saved offline. We'll sync it when you're back online.")
            }
        }
    }

    private fun openQuiz(uuid: String, returnToNotebookUuid: String?) {
        scope.launch {
            setState(getState().copy(isBusy = true))
            val canAttemptRemote = localInfrastructure.connectivityMonitor.currentState().isConnected

            if (canAttemptRemote) {
                runCatching { quizRepository.getQuiz(uuid) }
                    .onSuccess { quiz ->
                        setState(
                            getState().copy(
                                isBusy = false,
                                activeQuiz = quiz,
                                activeFlashcardDeck = null,
                                activeNotebookUuid = null,
                                studyReturnNotebookUuid = returnToNotebookUuid
                            )
                        )
                        syncStudyContext(quiz.notebookUuid)
                        return@launch
                    }
                    .onFailure {
                        val lostConnection = !localInfrastructure.connectivityMonitor.currentState().isConnected
                        if (lostConnection) {
                            val offlineQuiz = localInfrastructure.offlineRepository.getOfflineQuiz(uuid)
                            if (offlineQuiz != null) {
                                setState(
                                    getState().copy(
                                        isBusy = false,
                                        activeQuiz = offlineQuiz,
                                        activeFlashcardDeck = null,
                                        activeNotebookUuid = null,
                                        studyReturnNotebookUuid = returnToNotebookUuid
                                    )
                                )
                                return@launch
                            }
                        }
                        setState(getState().copy(isBusy = false))
                        showMessage("We couldn't open that quiz yet.")
                    }
                return@launch
            }

            val offlineQuiz = localInfrastructure.offlineRepository.getOfflineQuiz(uuid)
            if (offlineQuiz != null) {
                setState(
                    getState().copy(
                        isBusy = false,
                        activeQuiz = offlineQuiz,
                        activeFlashcardDeck = null,
                        activeNotebookUuid = null,
                        studyReturnNotebookUuid = returnToNotebookUuid
                    )
                )
                return@launch
            }

            setState(getState().copy(isBusy = false))
            showMessage("That quiz isn't available offline yet.")
        }
    }

    private fun openFlashcardDeck(uuid: String, returnToNotebookUuid: String?) {
        scope.launch {
            setState(getState().copy(isBusy = true))
            val canAttemptRemote = localInfrastructure.connectivityMonitor.currentState().isConnected

            if (canAttemptRemote) {
                runCatching { flashcardRepository.getFlashcardDeck(uuid) }
                    .onSuccess { deck ->
                        setState(
                            getState().copy(
                                isBusy = false,
                                activeFlashcardDeck = deck,
                                activeQuiz = null,
                                activeNotebookUuid = null,
                                studyReturnNotebookUuid = returnToNotebookUuid
                            )
                        )
                        syncStudyContext(deck.notebookUuid)
                        return@launch
                    }
                    .onFailure {
                        val lostConnection = !localInfrastructure.connectivityMonitor.currentState().isConnected
                        if (lostConnection) {
                            val offlineDeck = localInfrastructure.offlineRepository.getOfflineFlashcardDeck(uuid)
                            if (offlineDeck != null) {
                                setState(
                                    getState().copy(
                                        isBusy = false,
                                        activeFlashcardDeck = offlineDeck,
                                        activeQuiz = null,
                                        activeNotebookUuid = null,
                                        studyReturnNotebookUuid = returnToNotebookUuid
                                    )
                                )
                                return@launch
                            }
                        }
                        setState(getState().copy(isBusy = false))
                        showMessage("We couldn't open that deck yet.")
                    }
                return@launch
            }

            val offlineDeck = localInfrastructure.offlineRepository.getOfflineFlashcardDeck(uuid)
            if (offlineDeck != null) {
                setState(
                    getState().copy(
                        isBusy = false,
                        activeFlashcardDeck = offlineDeck,
                        activeQuiz = null,
                        activeNotebookUuid = null,
                        studyReturnNotebookUuid = returnToNotebookUuid
                    )
                )
                return@launch
            }

            setState(getState().copy(isBusy = false))
            showMessage("That deck isn't available offline yet.")
        }
    }

    private suspend fun queueQuizAttempt(uuid: String, score: Int) {
        localInfrastructure.offlineRepository.queueMutation(
            PendingMutation(
                clientMutationId = UUID.randomUUID().toString(),
                entityType = OfflineEntityType.QUIZ,
                entityUuid = uuid,
                operation = PendingMutationOperation.RECORD_QUIZ_ATTEMPT,
                payloadJson = gson.toJson(QuizAttemptQueuePayload(score)),
                queuedAt = System.currentTimeMillis(),
                priority = 1
            )
        )
        BrainBoxSyncWorkScheduler.enqueueWhenOnline(appContext)
    }

    private suspend fun queueFlashcardAttempt(uuid: String, mastery: Int) {
        localInfrastructure.offlineRepository.queueMutation(
            PendingMutation(
                clientMutationId = UUID.randomUUID().toString(),
                entityType = OfflineEntityType.FLASHCARD_DECK,
                entityUuid = uuid,
                operation = PendingMutationOperation.RECORD_FLASHCARD_ATTEMPT,
                payloadJson = gson.toJson(FlashcardAttemptQueuePayload(mastery)),
                queuedAt = System.currentTimeMillis(),
                priority = 1
            )
        )
        BrainBoxSyncWorkScheduler.enqueueWhenOnline(appContext)
    }
}
