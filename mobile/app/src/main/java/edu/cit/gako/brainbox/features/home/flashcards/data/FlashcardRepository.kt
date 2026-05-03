package edu.cit.gako.brainbox.features.home.flashcards.data

import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardAttemptRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import edu.cit.gako.brainbox.platform.network.requireData
import edu.cit.gako.brainbox.platform.network.requireSuccess
import java.util.UUID

class FlashcardRepository(
    private val apiService: FlashcardApiService
) {
    suspend fun getFlashcards(): List<FlashcardDeckSummary> {
        return apiService.getFlashcardsEnvelope()
            .requireData("We couldn't load flashcards.")
    }

    suspend fun getFlashcardDeck(uuid: String): FlashcardDeckDetail {
        return apiService.getFlashcardEnvelope(uuid)
            .requireData("We couldn't load that deck.")
    }

    suspend fun createFlashcardDeck(request: FlashcardDeckCreateRequest): FlashcardDeckDetail {
        return apiService.createFlashcardEnvelope(request)
            .requireData("We couldn't create that flashcard deck yet.")
    }

    suspend fun updateFlashcardDeck(uuid: String, request: FlashcardDeckCreateRequest): FlashcardDeckDetail {
        return apiService.updateFlashcardEnvelope(uuid, request)
            .requireData("We couldn't save that flashcard deck yet.")
    }

    suspend fun deleteFlashcardDeck(uuid: String) {
        apiService.deleteFlashcardEnvelope(uuid)
            .requireSuccess("We couldn't delete that flashcard deck yet.")
    }

    suspend fun recordFlashcardAttempt(
        uuid: String,
        mastery: Int,
        clientMutationId: String = UUID.randomUUID().toString()
    ): FlashcardDeckDetail {
        return apiService.recordFlashcardAttemptEnvelope(
            uuid,
            FlashcardAttemptRequest(mastery = mastery, clientMutationId = clientMutationId)
        )
            .requireData("We couldn't save your flashcard progress.")
    }
}
