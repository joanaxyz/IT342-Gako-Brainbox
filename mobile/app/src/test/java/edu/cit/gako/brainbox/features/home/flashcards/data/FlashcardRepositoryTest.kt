package edu.cit.gako.brainbox.features.home.flashcards.data

import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardAttemptRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.NetworkFlashcardCard
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.NetworkFlashcardDeckDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.NetworkFlashcardDeckSummary
import edu.cit.gako.brainbox.platform.network.ApiEnvelope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FlashcardRepositoryTest {
    @Test
    fun getFlashcardsSanitizesMalformedRowsWithoutFailingFetch() = runBlocking {
        val repository = FlashcardRepository(
            StubFlashcardApiService(
                summaries = listOf(
                    NetworkFlashcardDeckSummary(
                        uuid = "deck-1",
                        title = null,
                        cardCount = -4,
                        attempts = -2
                    ),
                    NetworkFlashcardDeckSummary(
                        uuid = "",
                        title = "Missing id"
                    )
                )
            )
        )

        val decks = repository.getFlashcards()

        assertEquals(1, decks.size)
        assertEquals("deck-1", decks.first().uuid)
        assertEquals("Untitled deck", decks.first().title)
        assertEquals(0, decks.first().cardCount)
        assertEquals(0L, decks.first().attempts)
    }

    @Test
    fun getFlashcardDeckUsesRouteUuidAndSanitizesCards() = runBlocking {
        val repository = FlashcardRepository(
            StubFlashcardApiService(
                detail = NetworkFlashcardDeckDetail(
                    uuid = null,
                    title = null,
                    cards = listOf(
                        NetworkFlashcardCard(front = null, back = " Back "),
                        NetworkFlashcardCard(front = " ", back = null)
                    )
                )
            )
        )

        val deck = repository.getFlashcardDeck("deck-1")

        assertEquals("deck-1", deck.uuid)
        assertEquals("Untitled deck", deck.title)
        assertEquals(2, deck.cardCount)
        assertEquals(2, deck.cards.size)
        assertEquals("Untitled card", deck.cards.first().front)
        assertEquals("Back", deck.cards.first().back)
        assertEquals("No answer yet", deck.cards[1].back)
    }

    private class StubFlashcardApiService(
        private val summaries: List<NetworkFlashcardDeckSummary> = emptyList(),
        private val detail: NetworkFlashcardDeckDetail =
            NetworkFlashcardDeckDetail(uuid = "deck-1", title = "Deck")
    ) : FlashcardApiService {
        override suspend fun getFlashcardsEnvelope(): ApiEnvelope<List<NetworkFlashcardDeckSummary>> =
            ApiEnvelope(success = true, data = summaries)

        override suspend fun createFlashcardEnvelope(
            request: FlashcardDeckCreateRequest
        ): ApiEnvelope<NetworkFlashcardDeckDetail> = ApiEnvelope(success = true, data = detail)

        override suspend fun updateFlashcardEnvelope(
            uuid: String,
            request: FlashcardDeckCreateRequest
        ): ApiEnvelope<NetworkFlashcardDeckDetail> = ApiEnvelope(success = true, data = detail)

        override suspend fun deleteFlashcardEnvelope(uuid: String): ApiEnvelope<Unit> =
            ApiEnvelope(success = true)

        override suspend fun getFlashcardEnvelope(uuid: String): ApiEnvelope<NetworkFlashcardDeckDetail> =
            ApiEnvelope(success = true, data = detail)

        override suspend fun recordFlashcardAttemptEnvelope(
            uuid: String,
            request: FlashcardAttemptRequest
        ): ApiEnvelope<NetworkFlashcardDeckDetail> = ApiEnvelope(success = true, data = detail)
    }
}
