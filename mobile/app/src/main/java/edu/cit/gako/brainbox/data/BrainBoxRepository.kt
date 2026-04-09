package edu.cit.gako.brainbox.data

import edu.cit.gako.brainbox.app.HomeBundle
import edu.cit.gako.brainbox.network.ApiService
import edu.cit.gako.brainbox.network.SessionManager
import edu.cit.gako.brainbox.network.models.FlashcardDeckDetail
import edu.cit.gako.brainbox.network.models.QuizDetail
import edu.cit.gako.brainbox.network.models.UserProfile

class BrainBoxRepository(
    apiService: ApiService,
    sessionManager: SessionManager
) {
    private val authRepository = BrainBoxAuthRepository(apiService, sessionManager)
    private val homeRepository = BrainBoxHomeRepository(apiService, sessionManager)
    private val studyRepository = BrainBoxStudyRepository(apiService)

    fun hasSession(): Boolean = authRepository.hasSession()

    fun sessionUsername(): String = authRepository.sessionUsername()

    suspend fun login(username: String, password: String): UserProfile =
        authRepository.login(username, password)

    suspend fun loginWithGoogle(idToken: String): UserProfile =
        authRepository.loginWithGoogle(idToken)

    suspend fun register(username: String, email: String, password: String) {
        authRepository.register(username, email, password)
    }

    suspend fun sendPasswordResetCode(email: String) {
        authRepository.sendPasswordResetCode(email)
    }

    suspend fun verifyPasswordResetCode(email: String, code: String): String =
        authRepository.verifyPasswordResetCode(email, code)

    suspend fun resetPassword(token: String, password: String) {
        authRepository.resetPassword(token, password)
    }

    suspend fun logout() {
        authRepository.logout()
    }

    suspend fun loadHome(): HomeBundle = homeRepository.loadHome()

    suspend fun markNotebookReviewed(uuid: String) {
        homeRepository.markNotebookReviewed(uuid)
    }

    suspend fun getQuiz(uuid: String): QuizDetail = studyRepository.getQuiz(uuid)

    suspend fun recordQuizAttempt(uuid: String, score: Int): QuizDetail =
        studyRepository.recordQuizAttempt(uuid, score)

    suspend fun getFlashcardDeck(uuid: String): FlashcardDeckDetail =
        studyRepository.getFlashcardDeck(uuid)

    suspend fun recordFlashcardAttempt(uuid: String, mastery: Int): FlashcardDeckDetail =
        studyRepository.recordFlashcardAttempt(uuid, mastery)
}

