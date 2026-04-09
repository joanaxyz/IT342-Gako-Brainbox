package com.it342.brainbox.data

import com.it342.brainbox.app.HomeBundle
import com.it342.brainbox.network.ApiService
import com.it342.brainbox.network.SessionManager
import com.it342.brainbox.network.models.FlashcardDeckDetail
import com.it342.brainbox.network.models.QuizDetail
import com.it342.brainbox.network.models.UserProfile

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
