package edu.cit.gako.brainbox.features.home.flashcards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.features.home.flashcards.data.FlashcardRepository
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_ALL
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_SCOPE_CATEGORIES
import edu.cit.gako.brainbox.shared.study.STUDY_FILTER_SCOPE_NOTEBOOKS
import edu.cit.gako.brainbox.shared.study.StudyCard
import edu.cit.gako.brainbox.shared.study.StudyFilterControls
import edu.cit.gako.brainbox.shared.study.StudyNotebookFilterModel
import edu.cit.gako.brainbox.shared.study.buildCategoryFilterOptions
import edu.cit.gako.brainbox.shared.study.buildNotebookFilterOptions
import edu.cit.gako.brainbox.shared.study.matchesStudyFilter
import edu.cit.gako.brainbox.shared.study.studyCategoryLabel
import edu.cit.gako.brainbox.shared.ui.BrandedSearchField
import edu.cit.gako.brainbox.shared.ui.EmptyStateCard
import edu.cit.gako.brainbox.shared.ui.NoticeBanner
import edu.cit.gako.brainbox.shared.ui.SimpleHomePage
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.AccentBg
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.ErrorRed
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.White
import java.util.Locale
import kotlinx.coroutines.launch

private const val DECK_SORT_UPDATED_AT = "updatedAt"
private const val DECK_SORT_TITLE = "title"
private const val DECK_SORT_CARD_COUNT = "cardCount"

private val DeckSortOptions = listOf(
    DeckSortOption(DECK_SORT_UPDATED_AT, "Updated"),
    DeckSortOption(DECK_SORT_TITLE, "Title"),
    DeckSortOption(DECK_SORT_CARD_COUNT, "Cards")
)

@Composable
internal fun FlashcardsScreen(
    repository: FlashcardRepository,
    flashcards: List<FlashcardDeckSummary>,
    notebooks: List<NotebookSummary>,
    contentPadding: PaddingValues,
    onFeatureRequest: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var localDecks by remember(flashcards) { mutableStateOf(flashcards) }
    var query by rememberSaveable { mutableStateOf("") }
    var filterScope by rememberSaveable { mutableStateOf(STUDY_FILTER_SCOPE_NOTEBOOKS) }
    var selectedNotebookId by rememberSaveable { mutableStateOf(STUDY_FILTER_ALL) }
    var selectedCategoryId by rememberSaveable { mutableStateOf(STUDY_FILTER_ALL) }
    var sortBy by rememberSaveable { mutableStateOf(DECK_SORT_UPDATED_AT) }
    var sortAscending by rememberSaveable { mutableStateOf(false) }
    var loadingEditUuid by rememberSaveable { mutableStateOf<String?>(null) }
    var editorMode by remember { mutableStateOf<DeckComposerMode?>(null) }
    var editingDeck by remember { mutableStateOf<FlashcardDeckDetail?>(null) }
    var deleteTarget by remember { mutableStateOf<DeckDeleteTarget?>(null) }
    var mutationBusy by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    val notebooksByUuid = remember(notebooks) {
        notebooks.associate { it.uuid to it.toStudyNotebookFilterModel() }
    }
    val notebookOptions = remember(localDecks, notebooksByUuid) {
        buildNotebookFilterOptions(localDecks.map { it.notebookUuid }, notebooksByUuid)
    }
    val categoryOptions = remember(localDecks, notebooksByUuid) {
        buildCategoryFilterOptions(localDecks.map { it.notebookUuid }, notebooksByUuid)
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
    val filtered = remember(
        localDecks,
        notebooksByUuid,
        query,
        filterScope,
        selectedNotebookId,
        selectedCategoryId,
        sortBy,
        sortAscending
    ) {
        val filteredItems = localDecks.filter { deck ->
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

        val sorted = when (sortBy) {
            DECK_SORT_TITLE -> filteredItems.sortedBy { it.title.lowercase(Locale.ENGLISH) }
            DECK_SORT_CARD_COUNT -> filteredItems.sortedBy { it.cardCount }
            else -> filteredItems.sortedBy { it.updatedAt.orEmpty() }
        }

        if (sortAscending) sorted else sorted.asReversed()
    }

    fun upsertDeck(deck: FlashcardDeckDetail) {
        val summary = deck.toSummary()
        localDecks = listOf(summary) + localDecks.filter { it.uuid != summary.uuid }
    }

    fun submitDeck(payload: FlashcardDeckCreateRequest) {
        val mode = editorMode ?: return
        val targetUuid = editingDeck?.uuid
        mutationBusy = true
        scope.launch {
            runCatching {
                if (mode == DeckComposerMode.CREATE) {
                    repository.createFlashcardDeck(payload)
                } else {
                    repository.updateFlashcardDeck(targetUuid.orEmpty(), payload)
                }
            }.onSuccess { saved ->
                upsertDeck(saved)
                editorMode = null
                editingDeck = null
                message = if (mode == DeckComposerMode.CREATE) {
                    "\"${saved.title}\" created."
                } else {
                    "\"${saved.title}\" saved."
                }
            }.onFailure {
                onFeatureRequest(
                    if (mode == DeckComposerMode.CREATE) "Couldn't create deck." else "Couldn't save deck."
                )
            }
            mutationBusy = false
        }
    }

    fun openEditorForDeck(deck: FlashcardDeckSummary) {
        loadingEditUuid = deck.uuid
        scope.launch {
            runCatching { repository.getFlashcardDeck(deck.uuid) }
                .onSuccess {
                    editingDeck = it
                    editorMode = DeckComposerMode.EDIT
                }
                .onFailure {
                    onFeatureRequest("Couldn't open deck editor.")
                }
            loadingEditUuid = null
        }
    }

    fun deleteDeck(target: DeckDeleteTarget) {
        val previousDecks = localDecks
        localDecks = localDecks.filter { it.uuid != target.uuid }
        deleteTarget = null
        if (target.closeEditorOnSuccess) {
            editorMode = null
            editingDeck = null
        }
        mutationBusy = true
        scope.launch {
            runCatching { repository.deleteFlashcardDeck(target.uuid) }
                .onSuccess {
                    message = "\"${target.title}\" deleted."
                }
                .onFailure {
                    localDecks = previousDecks
                    onFeatureRequest("Couldn't delete deck.")
                }
            mutationBusy = false
        }
    }

    SimpleHomePage(
        title = "Flashcards",
        description = "Create, edit, sort, and study the same decks you use on web.",
        contentPadding = contentPadding
    ) {
        message?.let {
            NoticeBanner(it, "Flashcard changes sync with your web workspace.")
            Spacer(modifier = Modifier.height(16.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${localDecks.size} decks", style = MaterialTheme.typography.titleMedium, color = Ink)
                Text("Front and back recall cards", style = MaterialTheme.typography.bodySmall, color = Ink3)
            }
            Button(
                onClick = {
                    editingDeck = null
                    editorMode = DeckComposerMode.CREATE
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("New")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        BrandedSearchField(query, { query = it }, "Search decks")
        Spacer(modifier = Modifier.height(16.dp))
        DeckSortControls(
            options = DeckSortOptions,
            sortBy = sortBy,
            sortAscending = sortAscending,
            onSortChange = { nextSort ->
                if (sortBy == nextSort) {
                    sortAscending = !sortAscending
                } else {
                    sortBy = nextSort
                    sortAscending = nextSort == DECK_SORT_TITLE
                }
            },
            onToggleDirection = { sortAscending = !sortAscending }
        )
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EmptyStateCard(
                    title = if (localDecks.isEmpty()) "No flashcard decks yet" else "No matching decks",
                    body = if (query.isNotBlank() || hasActiveFilter) {
                        "Try a different search or filter."
                    } else {
                        "Create your first deck here or on the web."
                    }
                )
                if (localDecks.isEmpty() && query.isBlank() && !hasActiveFilter) {
                    OutlinedButton(
                        onClick = {
                            editingDeck = null
                            editorMode = DeckComposerMode.CREATE
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Border),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = Ink2),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("New deck")
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.forEach { deck ->
                    val packNotebookUuid = deck.notebookUuid
                    val categoryLabel = studyCategoryLabel(packNotebookUuid, notebooksByUuid)
                        ?.takeUnless { it == "Standalone" }
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
                            DeckCardActions(
                                isOpeningEditor = loadingEditUuid == deck.uuid,
                                onEdit = { openEditorForDeck(deck) },
                                onDelete = {
                                    deleteTarget = DeckDeleteTarget(
                                        uuid = deck.uuid,
                                        title = deck.title,
                                        closeEditorOnSuccess = false
                                    )
                                }
                            )
                        }
                    ) {
                        onOpenFlashcardDeck(deck.uuid)
                    }
                }
            }
        }
    }

    editorMode?.let { mode ->
        DeckComposerSheet(
            mode = mode,
            initialDeck = editingDeck,
            notebooks = notebooks,
            isBusy = mutationBusy,
            onDismiss = {
                if (!mutationBusy) {
                    editorMode = null
                    editingDeck = null
                }
            },
            onSubmit = ::submitDeck,
            onDelete = { deck ->
                deleteTarget = DeckDeleteTarget(
                    uuid = deck.uuid,
                    title = deck.title,
                    closeEditorOnSuccess = true
                )
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!mutationBusy) deleteTarget = null },
            confirmButton = {
                TextButton(
                    onClick = { deleteDeck(target) },
                    enabled = !mutationBusy
                ) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteTarget = null },
                    enabled = !mutationBusy
                ) {
                    Text("Cancel", color = Ink2)
                }
            },
            title = { Text("Delete deck?", color = Ink) },
            text = { Text("\"${target.title}\" will be removed from mobile and web.", color = Ink3) },
            containerColor = White
        )
    }
}

@Composable
private fun DeckCardActions(
    isOpeningEditor: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onEdit,
            enabled = !isOpeningEditor,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null, tint = Accent)
            Text(if (isOpeningEditor) "Opening" else "Edit", color = Accent)
        }
        TextButton(
            onClick = onDelete,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, tint = ErrorRed)
            Text("Delete", color = ErrorRed)
        }
    }
}

@Composable
private fun DeckSortControls(
    options: List<DeckSortOption>,
    sortBy: String,
    sortAscending: Boolean,
    onSortChange: (String) -> Unit,
    onToggleDirection: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Sort by", style = MaterialTheme.typography.labelSmall, color = Ink3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = sortBy == option.key,
                    onClick = { onSortChange(option.key) },
                    label = { Text(option.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBg,
                        selectedLabelColor = Accent
                    )
                )
            }
            FilterChip(
                selected = sortAscending,
                onClick = onToggleDirection,
                label = { Text(if (sortAscending) "Asc" else "Desc") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentBg,
                    selectedLabelColor = Accent
                )
            )
        }
    }
}

private data class DeckSortOption(
    val key: String,
    val label: String
)

private data class DeckDeleteTarget(
    val uuid: String,
    val title: String,
    val closeEditorOnSuccess: Boolean
)

private fun FlashcardDeckDetail.toSummary(): FlashcardDeckSummary =
    FlashcardDeckSummary(
        uuid = uuid,
        title = title,
        description = description,
        notebookUuid = notebookUuid,
        notebookTitle = notebookTitle,
        cardCount = cardCount,
        bestMastery = bestMastery,
        attempts = attempts,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

private fun NotebookSummary.toStudyNotebookFilterModel(): StudyNotebookFilterModel =
    StudyNotebookFilterModel(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName
    )
