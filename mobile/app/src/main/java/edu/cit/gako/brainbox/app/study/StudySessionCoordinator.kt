package edu.cit.gako.brainbox.app.study

import edu.cit.gako.brainbox.app.AppState
import edu.cit.gako.brainbox.app.withFlashcardAttempt
import edu.cit.gako.brainbox.app.withQuizAttempt
import edu.cit.gako.brainbox.features.home.flashcards.data.FlashcardRepository
import edu.cit.gako.brainbox.features.home.quizzes.data.QuizRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class StudySessionCoordinator(
    private val quizRepository: QuizRepository,
    private val flashcardRepository: FlashcardRepository,
    private val scope: CoroutineScope,
    private val getState: () -> AppState,
    private val setState: (AppState) -> Unit,
    private val syncHome: suspend (setBusy: Boolean) -> Unit,
    private val syncStudyContext: (String?) -> Unit,
    private val showMessage: (String) -> Unit
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
            }.onSuccess {
                runCatching { syncHome(false) }
            }.onFailure {
                showMessage("We couldn't save that quiz score. Check your connection and try again.")
            }
        }
    }

    fun handleRecordFlashcardAttempt(uuid: String, mastery: Int) {
        setState(getState().copy(homeData = getState().homeData.withFlashcardAttempt(uuid, mastery)))
        scope.launch {
            runCatching {
                flashcardRepository.recordFlashcardAttempt(uuid, mastery)
            }.onSuccess {
                runCatching { syncHome(false) }
            }.onFailure {
                showMessage("We couldn't save that deck progress. Check your connection and try again.")
            }
        }
    }

    private fun openQuiz(uuid: String, returnToNotebookUuid: String?) {
        scope.launch {
            setState(getState().copy(isBusy = true))
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
                }
                .onFailure {
                    setState(getState().copy(isBusy = false))
                    showMessage("We couldn't open that quiz yet.")
                }
        }
    }

    private fun openFlashcardDeck(uuid: String, returnToNotebookUuid: String?) {
        scope.launch {
            setState(getState().copy(isBusy = true))
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
                }
                .onFailure {
                    setState(getState().copy(isBusy = false))
                    showMessage("We couldn't open that deck yet.")
                }
        }
    }
}
