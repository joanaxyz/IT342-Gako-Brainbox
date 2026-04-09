package edu.cit.gako.brainbox.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.home.SimpleHomePage
import edu.cit.gako.brainbox.network.models.FlashcardDeckSummary
import edu.cit.gako.brainbox.shared.BrandedSearchField
import edu.cit.gako.brainbox.shared.EmptyStateCard
import edu.cit.gako.brainbox.shared.StudyCard

@Composable
internal fun FlashcardsScreen(
    flashcards: List<FlashcardDeckSummary>,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onOpenFlashcardDeck: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = flashcards.filter {
        query.isBlank() ||
            it.title.contains(other = query, ignoreCase = true) ||
            (it.notebookTitle ?: "").contains(other = query, ignoreCase = true)
    }

    SimpleHomePage(
        title = "Flashcards",
        description = "Decks keep the same mastery-driven feel from the web app, tuned for mobile scanning.",
        syncNotice = syncNotice,
        syncedAtLabel = syncedAtLabel,
        contentPadding = contentPadding
    ) {
        BrandedSearchField(query, { query = it }, "Search decks")
        Spacer(modifier = Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            EmptyStateCard("No flashcard decks yet", "Create decks on the web and they'll show up here with the same warm card language.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.forEach { deck ->
                    StudyCard(
                        title = deck.title,
                        description = deck.description,
                        kicker = deck.notebookTitle ?: "Flashcards",
                        meta = listOf("${deck.cardCount} cards", "${deck.attempts} attempts"),
                        progress = deck.bestMastery,
                        action = "Study deck"
                    ) {
                        onOpenFlashcardDeck(deck.uuid)
                    }
                }
            }
        }
    }
}


