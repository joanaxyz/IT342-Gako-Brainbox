package edu.cit.gako.brainbox.data

import edu.cit.gako.brainbox.app.HomeBundle
import edu.cit.gako.brainbox.network.ApiService
import edu.cit.gako.brainbox.network.SessionManager
import edu.cit.gako.brainbox.network.models.AiConfigListResponse
import edu.cit.gako.brainbox.network.models.AiConfigRequest
import edu.cit.gako.brainbox.network.models.AiConfigResponse
import edu.cit.gako.brainbox.network.models.AiQueryRequest
import edu.cit.gako.brainbox.network.models.AiQueryResponse
import edu.cit.gako.brainbox.network.models.CategoryDetail
import edu.cit.gako.brainbox.network.models.FlashcardDeckDetail
import edu.cit.gako.brainbox.network.models.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.network.models.NotebookDetail
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.network.models.PlaylistAddNotebookBody
import edu.cit.gako.brainbox.network.models.PlaylistCreateRequest
import edu.cit.gako.brainbox.network.models.PlaylistSummary
import edu.cit.gako.brainbox.network.models.NotebookVersionItem
import edu.cit.gako.brainbox.network.models.OfflineNotebookBundle
import edu.cit.gako.brainbox.network.models.QuizCreateRequest
import edu.cit.gako.brainbox.network.models.QuizDetail
import edu.cit.gako.brainbox.network.models.UserProfile
import java.util.UUID

class BrainBoxRepository(
    private val apiService: ApiService,
    sessionManager: SessionManager
) {
    private val authRepository = BrainBoxAuthRepository(apiService, sessionManager)
    private val homeRepository = BrainBoxHomeRepository(apiService, sessionManager)
    private val studyRepository = BrainBoxStudyRepository(apiService)
    private val notebookRepository = BrainBoxNotebookRepository(apiService)
    private val aiRepository = BrainBoxAiRepository(apiService)
    private val queueRepository = BrainBoxQueueRepository(apiService)

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

    suspend fun getQueue(): List<NotebookSummary> = queueRepository.getQueue()

    suspend fun addToQueue(notebookUuid: String): List<NotebookSummary> =
        queueRepository.addNotebook(notebookUuid)

    suspend fun removeFromQueue(notebookUuid: String): List<NotebookSummary> =
        queueRepository.removeNotebook(notebookUuid)

    suspend fun clearQueue() = queueRepository.clearQueue()

    suspend fun reorderQueue(notebookUuids: List<String>): List<NotebookSummary> =
        queueRepository.reorderQueue(notebookUuids)

    suspend fun markNotebookReviewed(
        uuid: String,
        baseVersion: Long? = null,
        clientMutationId: String? = null
    ): NotebookMutationResult = notebookRepository.markNotebookReviewed(
        uuid = uuid,
        baseVersion = baseVersion,
        clientMutationId = clientMutationId ?: UUID.randomUUID().toString()
    )

    suspend fun getNotebook(uuid: String): NotebookDetail = notebookRepository.getNotebook(uuid)

    suspend fun createNotebook(
        title: String,
        categoryId: Long? = null,
        content: String = ""
    ): NotebookMutationResult = notebookRepository.createNotebook(title, categoryId, content)

    suspend fun saveNotebookContent(
        uuid: String,
        content: String,
        baseVersion: Long? = null,
        clientMutationId: String? = null
    ): NotebookMutationResult = notebookRepository.saveNotebookContent(
        uuid = uuid,
        content = content,
        baseVersion = baseVersion,
        clientMutationId = clientMutationId ?: UUID.randomUUID().toString()
    )

    suspend fun updateNotebook(
        uuid: String,
        title: String? = null,
        categoryId: Long? = null,
        baseVersion: Long? = null,
        clientMutationId: String? = null
    ): NotebookMutationResult = notebookRepository.updateNotebook(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        baseVersion = baseVersion,
        clientMutationId = clientMutationId ?: UUID.randomUUID().toString()
    )

    suspend fun getNotebookVersions(uuid: String): List<NotebookVersionItem> =
        notebookRepository.getNotebookVersions(uuid)

    suspend fun createNotebookVersion(uuid: String, content: String): NotebookVersionItem =
        notebookRepository.createNotebookVersion(uuid, content)

    suspend fun restoreNotebookVersion(uuid: String, versionId: Long): NotebookMutationResult =
        notebookRepository.restoreNotebookVersion(uuid, versionId)

    suspend fun getCategories(): List<CategoryDetail> = notebookRepository.getCategories()

    suspend fun getOfflineBundle(notebookUuids: List<String>): OfflineNotebookBundle =
        notebookRepository.getOfflineBundle(notebookUuids)

    suspend fun deleteNotebook(
        uuid: String,
        baseVersion: Long? = null,
        clientMutationId: String? = null
    ): NotebookMutationResult = notebookRepository.deleteNotebook(
        uuid = uuid,
        baseVersion = baseVersion,
        clientMutationId = clientMutationId ?: UUID.randomUUID().toString()
    )

    suspend fun queryAi(request: AiQueryRequest): AiQueryResponse = aiRepository.query(request)

    suspend fun getAiConfig(): AiConfigResponse = aiRepository.getConfig()

    suspend fun listAiConfigs(): AiConfigListResponse = aiRepository.listConfigs()

    suspend fun saveAiConfig(request: AiConfigRequest): AiConfigResponse = aiRepository.saveConfig(request)

    suspend fun selectAiConfig(configId: Long): AiConfigResponse = aiRepository.selectConfig(configId)

    suspend fun deleteAiConfig(configId: Long) = aiRepository.deleteConfig(configId)

    suspend fun createQuiz(request: QuizCreateRequest): QuizDetail =
        studyRepository.createQuiz(request)

    suspend fun getQuiz(uuid: String): QuizDetail = studyRepository.getQuiz(uuid)

    suspend fun recordQuizAttempt(uuid: String, score: Int): QuizDetail =
        studyRepository.recordQuizAttempt(uuid, score)

    suspend fun createFlashcardDeck(request: FlashcardDeckCreateRequest): FlashcardDeckDetail =
        studyRepository.createFlashcardDeck(request)

    suspend fun getFlashcardDeck(uuid: String): FlashcardDeckDetail =
        studyRepository.getFlashcardDeck(uuid)

    suspend fun recordFlashcardAttempt(uuid: String, mastery: Int): FlashcardDeckDetail =
        studyRepository.recordFlashcardAttempt(uuid, mastery)
}
