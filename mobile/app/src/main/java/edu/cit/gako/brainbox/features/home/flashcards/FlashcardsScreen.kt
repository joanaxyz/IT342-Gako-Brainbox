package edu.cit.gako.brainbox.features.home.flashcards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import edu.cit.gako.brainbox.features.home.data.HomeOfflinePackRepository
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_ALL
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_SCOPE_CATEGORIES
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_SCOPE_NOTEBOOKS
import edu.cit.gako.brainbox.shared.ui.BrandedSearchField
import edu.cit.gako.brainbox.shared.ui.EmptyStateCard
import edu.cit.gako.brainbox.shared.ui.OfflinePackActionButton
import edu.cit.gako.brainbox.shared.ui.SimpleHomePage
import edu.cit.gako.brainbox.shared.study.StudyCard
import edu.cit.gako.brainbox.shared.study.StudyFilterControls
import edu.cit.gako.brainbox.shared.study.StudyNotebookFilterModel
import edu.cit.gako.brainbox.shared.study.buildCategoryFilterOptions
import edu.cit.gako.brainbox.shared.study.buildNotebookFilterOptions
import edu.cit.gako.brainbox.shared.study.matchesStudyFilter
import edu.cit.gako.brainbox.shared.study.studyCategoryLabel
import edu.cit.gako.brainbox.shared.ui.SyncNoticeBanner
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import kotlinx.coroutines.launch

@Composable
internal fun FlashcardsScreen(
    offlinePackRepository: HomeOfflinePackRepository,
    flashcards: List<FlashcardDeckSummary>,
    notebooks: List<NotebookSummary>,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onOpenFlashcardDeck: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val offlineNotebookUuids by offlinePackRepository.observeActiveOfflineNotebookUuids().collectAsState(initial = emptySet())
    var offlineMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var activeOfflineNotebookUuid by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var filterScope by rememberSaveable { mutableStateOf(STUDY_FILTER_SCOPE_NOTEBOOKS) }
    var selectedNotebookId by rememberSaveable { mutableStateOf(STUDY_FILTER_ALL) }
    var selectedCategoryId by rememberSaveable { mutableStateOf(STUDY_FILTER_ALL) }
    val notebooksByUuid = remember(notebooks) {
        notebooks.associate { it.uuid to it.toStudyNotebookFilterModel() }
    }
    val notebookOptions = remember(flashcards, notebooksByUuid) {
        buildNotebookFilterOptions(flashcards.map { it.notebookUuid }, notebooksByUuid)
    }
    val categoryOptions = remember(flashcards, notebooksByUuid) {
        buildCategoryFilterOptions(flashcards.map { it.notebookUuid }, notebooksByUuid)
    }

    LaunchedEffect(notebookOptions, selectedNotebookId) {
        if (notebookOptions.none { it.key == selectedNotebookId }) {
            selectedNotebookId = STUDY_FILTER_ALL
        }
    }
    LaunchedEffect(categoryOptions, selectedCategoryId) {
        if (categoryOptions.none { it.key == selectedCategoryId }) {
            selectedCategoryId = STUDY_FILTER_ALL
        }
    }

    val hasActiveFilter = if (filterScope == STUDY_FILTER_SCOPE_CATEGORIES) {
        selectedCategoryId != STUDY_FILTER_ALL
    } else {
        selectedNotebookId != STUDY_FILTER_ALL
    }
    val filtered = flashcards.filter { deck ->
        val categoryLabel = studyCategoryLabel(deck.notebookUuid, notebooksByUuid).orEmpty()
        val matchesQuery = query.isBlank() ||
            deck.title.contains(other = query, ignoreCase = true) ||
            (deck.notebookTitle ?: "").contains(other = query, ignoreCase = true) ||
            categoryLabel.contains(other = query, ignoreCase = true)

        matchesQuery && matchesStudyFilter(
            notebookUuid = deck.notebookUuid,
            notebooksByUuid = notebooksByUuid,
            filterScope = filterScope,
            selectedNotebookId = selectedNotebookId,
            selectedCategoryId = selectedCategoryId
        )
    }

    SimpleHomePage(
        title = "Flashcards",
        description = "Decks keep the same mastery-driven feel from the web app, tuned for mobile scanning.",
        syncNotice = syncNotice,
        syncedAtLabel = syncedAtLabel,
        contentPadding = contentPadding
    ) {
        offlineMessage?.let {
            SyncNoticeBanner(it, "Offline flashcard packs stay linked to their parent notebook.")
            Spacer(modifier = Modifier.height(16.dp))
        }
        BrandedSearchField(query, { query = it }, "Search decks")
        Spacer(modifier = Modifier.height(16.dp))
        StudyFilterControls(
            filterScope = filterScope,
            selectedNotebookId = selectedNotebookId,
            selectedCategoryId = selectedCategoryId,
            notebookOptions = notebookOptions,
            categoryOptions = categoryOptions,
            onFilterScopeChange = { filterScope = it },
            onNotebookFilterChange = { selectedNotebookId = it },
            onCategoryFilterChange = { selectedCategoryId = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            EmptyStateCard(
                title = if (flashcards.isEmpty()) "No flashcard decks yet" else "No matching decks",
                body = if (query.isNotBlank() || hasActiveFilter) {
                    "Try a different search or filter."
                } else {
                    "Create decks on the web and they'll show up here with the same warm card language."
                }
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.forEach { deck ->
                    val packNotebookUuid = deck.notebookUuid
                    val categoryLabel = studyCategoryLabel(packNotebookUuid, notebooksByUuid)
                        ?.takeUnless { it == "Standalone" }
                    val isOffline = !packNotebookUuid.isNullOrBlank() && packNotebookUuid in offlineNotebookUuids
                    val isWorking = activeOfflineNotebookUuid == packNotebookUuid && !packNotebookUuid.isNullOrBlank()
                    StudyCard(
                        title = deck.title,
                        description = deck.description,
                        kicker = deck.notebookTitle ?: "Flashcards",
                        meta = listOfNotNull(
                            "${deck.cardCount} cards",
                            "${deck.attempts} attempts",
                            categoryLabel
                        ),
                        progress = deck.bestMastery,
                        action = "Study deck",
                        footer = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OfflinePackActionButton(
                                    isOffline = isOffline,
                                    isWorking = isWorking,
                                    onClick = {
                                        val targetNotebookUuid = packNotebookUuid
                                        if (targetNotebookUuid.isNullOrBlank()) {
                                            offlineMessage = "We couldn't save that deck offline because it isn't linked to a notebook yet."
                                        } else {
                                            scope.launch {
                                                activeOfflineNotebookUuid = targetNotebookUuid
                                                if (isOffline) {
                                                    offlinePackRepository.removeNotebookPackAssets(targetNotebookUuid)
                                                    offlineMessage = "\"${deck.title}\" and its notebook pack were removed from offline storage."
                                                } else {
                                                    offlinePackRepository.downloadNotebookPack(targetNotebookUuid).onSuccess { result ->
                                                        offlineMessage = if (result.audioPrepared) {
                                                            "\"${deck.title}\" is now available offline with its notebook pack and audio ready."
                                                        } else {
                                                            "\"${deck.title}\" is now available offline with its notebook pack, but audio could not be prepared yet."
                                                        }
                                                    }.onFailure {
                                                        offlineMessage = "We couldn't download that deck for offline use right now."
                                                    }
                                                }
                                                activeOfflineNotebookUuid = null
                                            }
                                        }
                                    }
                                )
                                Text(
                                    text = when {
                                        packNotebookUuid.isNullOrBlank() -> "Notebook required"
                                        isOffline -> "Available offline"
                                        else -> "Online only"
                                    },
                                    color = if (isOffline) Accent else Ink3
                                )
                            }
                        }
                    ) {
                        onOpenFlashcardDeck(deck.uuid)
                    }
                }
            }
        }
    }
}

private fun NotebookSummary.toStudyNotebookFilterModel(): StudyNotebookFilterModel =
    StudyNotebookFilterModel(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName
    )


