package edu.cit.gako.brainbox.features.home.flashcards.data

import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardAttemptRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardCard
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.NetworkFlashcardCard
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.NetworkFlashcardDeckDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.NetworkFlashcardDeckSummary
import edu.cit.gako.brainbox.platform.network.requireData
import edu.cit.gako.brainbox.platform.network.requireSuccess
import java.util.UUID

class FlashcardRepository(
    private val apiService: FlashcardApiService
) {
    suspend fun getFlashcards(): List<FlashcardDeckSummary> {
        return apiService.getFlashcardsEnvelope()
            .requireData("We couldn't load flashcards.")
            .mapNotNull { it.toSummary() }
    }

    suspend fun getFlashcardDeck(uuid: String): FlashcardDeckDetail {
        return apiService.getFlashcardEnvelope(uuid)
            .requireData("We couldn't load that deck.")
            .toDetail(fallbackUuid = uuid)
            ?: throw IllegalStateException("We couldn't load that deck.")
    }

    suspend fun createFlashcardDeck(request: FlashcardDeckCreateRequest): FlashcardDeckDetail {
        return apiService.createFlashcardEnvelope(request)
            .requireData("We couldn't create that flashcard deck yet.")
            .toDetail(fallbackUuid = null)
            ?: throw IllegalStateException("We couldn't create that flashcard deck yet.")
    }

    suspend fun updateFlashcardDeck(uuid: String, request: FlashcardDeckCreateRequest): FlashcardDeckDetail {
        return apiService.updateFlashcardEnvelope(uuid, request)
            .requireData("We couldn't save that flashcard deck yet.")
            .toDetail(fallbackUuid = uuid)
            ?: throw IllegalStateException("We couldn't save that flashcard deck yet.")
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
            .toDetail(fallbackUuid = uuid)
            ?: throw IllegalStateException("We couldn't save your flashcard progress.")
    }
}

private const val UNTITLED_DECK = "Untitled deck"
private const val UNTITLED_CARD_FRONT = "Untitled card"
private const val UNTITLED_CARD_BACK = "No answer yet"

private fun NetworkFlashcardDeckSummary.toSummary(): FlashcardDeckSummary? {
    val safeUuid = uuid.safeRequired() ?: return null
    val validCardCount = cards.orEmpty().mapNotNull { it?.toCard() }.size

    return FlashcardDeckSummary(
        uuid = safeUuid,
        title = title.safeTitle(),
        description = description.safeOptional(),
        notebookUuid = notebookUuid.safeOptional(),
        notebookTitle = notebookTitle.safeOptional(),
        cardCount = (cardCount ?: validCardCount).coerceAtLeast(0),
        bestMastery = bestMastery,
        attempts = (attempts ?: 0L).coerceAtLeast(0L),
        createdAt = createdAt.safeOptional(),
        updatedAt = updatedAt.safeOptional()
    )
}

private fun NetworkFlashcardDeckDetail.toDetail(fallbackUuid: String?): FlashcardDeckDetail? {
    val safeUuid = uuid.safeRequired() ?: fallbackUuid.safeRequired() ?: return null
    val validCards = cards.orEmpty().mapNotNull { it?.toCard() }

    return FlashcardDeckDetail(
        uuid = safeUuid,
        title = title.safeTitle(),
        description = description.safeOptional(),
        notebookUuid = notebookUuid.safeOptional(),
        notebookTitle = notebookTitle.safeOptional(),
        cardCount = validCards.size,
        bestMastery = bestMastery,
        attempts = (attempts ?: 0L).coerceAtLeast(0L),
        cards = validCards,
        createdAt = createdAt.safeOptional(),
        updatedAt = updatedAt.safeOptional()
    )
}

private fun NetworkFlashcardCard.toCard(): FlashcardCard =
    FlashcardCard(
        front = front.safeTitle(fallback = UNTITLED_CARD_FRONT),
        back = back.safeTitle(fallback = UNTITLED_CARD_BACK)
    )

private fun String?.safeTitle(fallback: String = UNTITLED_DECK): String =
    this?.trim()?.takeIf { it.isNotBlank() } ?: fallback

private fun String?.safeOptional(): String? =
    this?.trim()?.takeIf { it.isNotBlank() }

private fun String?.safeRequired(): String? = safeOptional()
