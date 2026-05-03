package edu.cit.gako.brainbox.features.home.flashcards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardCard
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.shared.ui.TokenBadge
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.AccentBg
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.ErrorRed
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.Ink4
import edu.cit.gako.brainbox.shared.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeckComposerSheet(
    mode: DeckComposerMode,
    initialDeck: FlashcardDeckDetail?,
    notebooks: List<NotebookSummary>,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (FlashcardDeckCreateRequest) -> Unit,
    onDelete: (FlashcardDeckDetail) -> Unit
) {
    var draft by remember(initialDeck?.uuid, mode) {
        mutableStateOf(DeckDraft.from(initialDeck))
    }
    val activeCard = draft.activeCard
    val title = if (mode == DeckComposerMode.CREATE) "Create deck" else "Edit deck"

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = Ink)
                    Text("Create the front and back cards you use on web.", style = MaterialTheme.typography.bodySmall, color = Ink3)
                }
                if (mode == DeckComposerMode.EDIT && initialDeck != null) {
                    TextButton(
                        onClick = { onDelete(initialDeck) },
                        enabled = !isBusy
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = ErrorRed)
                        Text("Delete", color = ErrorRed)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BrandedDeckField(
                    label = "Deck title",
                    value = draft.title,
                    onValueChange = { draft = draft.copy(title = it) },
                    placeholder = "Deck title"
                )
                BrandedDeckField(
                    label = "Description",
                    value = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    placeholder = "What is this deck about?",
                    minLines = 2
                )
                DeckNotebookPicker(
                    selectedNotebookUuid = draft.notebookUuid,
                    notebooks = notebooks,
                    onSelect = { draft = draft.copy(notebookUuid = it) }
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = White,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Cards", style = MaterialTheme.typography.titleMedium, color = Ink)
                                Text("${draft.cards.size} total", style = MaterialTheme.typography.bodySmall, color = Ink3)
                            }
                            OutlinedButton(
                                onClick = { draft = draft.addCard() },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Border),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Text("Card")
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            draft.cards.forEachIndexed { index, _ ->
                                DeckEditorChip(
                                    label = "${index + 1}",
                                    selected = draft.activeIndex == index,
                                    onClick = { draft = draft.copy(activeIndex = index) }
                                )
                            }
                        }
                    }
                }

                CardEditor(
                    card = activeCard,
                    index = draft.activeIndex,
                    total = draft.cards.size,
                    onCardChange = { draft = draft.replaceActiveCard(it) },
                    onRemove = { draft = draft.removeActiveCard() }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isBusy,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Border),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = Ink2),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onSubmit(draft.toPayload()) },
                    modifier = Modifier.weight(1f),
                    enabled = draft.canSubmit && !isBusy,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(color = White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Text(if (mode == DeckComposerMode.CREATE) "Create" else "Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun CardEditor(
    card: FlashcardCardDraft,
    index: Int,
    total: Int,
    onCardChange: (FlashcardCardDraft) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TokenBadge("${index + 1}")
                    Text("Card ${index + 1} of $total", style = MaterialTheme.typography.titleMedium, color = Ink)
                }
                TextButton(onClick = onRemove, enabled = total > 1) {
                    Text("Remove", color = if (total > 1) ErrorRed else Ink4)
                }
            }
            BrandedDeckField(
                label = "Front - question or term",
                value = card.front,
                onValueChange = { onCardChange(card.copy(front = it)) },
                placeholder = "Write the prompt",
                minLines = 4
            )
            BrandedDeckField(
                label = "Back - answer or definition",
                value = card.back,
                onValueChange = { onCardChange(card.copy(back = it)) },
                placeholder = "Write the answer",
                minLines = 4
            )
        }
    }
}

@Composable
private fun DeckNotebookPicker(
    selectedNotebookUuid: String?,
    notebooks: List<NotebookSummary>,
    onSelect: (String?) -> Unit
) {
    DeckChipSection(label = "Notebook") {
        DeckEditorChip(
            label = "No notebook",
            selected = selectedNotebookUuid.isNullOrBlank(),
            onClick = { onSelect(null) }
        )
        notebooks.forEach { notebook ->
            DeckEditorChip(
                label = notebook.title,
                selected = selectedNotebookUuid == notebook.uuid,
                onClick = { onSelect(notebook.uuid) }
            )
        }
    }
}

@Composable
private fun DeckChipSection(
    label: String,
    content: @Composable RowScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Ink3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun DeckEditorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentBg,
            selectedLabelColor = Accent
        )
    )
}

@Composable
private fun BrandedDeckField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Ink3)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Ink4) },
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = deckTextFieldColors(),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun deckTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Accent,
    unfocusedBorderColor = Border,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
    cursorColor = Accent,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink
)

internal enum class DeckComposerMode { CREATE, EDIT }

private data class DeckDraft(
    val title: String,
    val description: String,
    val notebookUuid: String?,
    val cards: List<FlashcardCardDraft>,
    val activeIndex: Int
) {
    val activeCard: FlashcardCardDraft
        get() = cards.getOrElse(activeIndex.coerceIn(0, cards.lastIndex)) { createCard() }

    val validCards: List<FlashcardCardDraft>
        get() = cards.filter { it.isValid }

    val canSubmit: Boolean
        get() = title.isNotBlank() && validCards.isNotEmpty()

    fun addCard(): DeckDraft {
        val nextCards = cards + createCard()
        return copy(cards = nextCards, activeIndex = nextCards.lastIndex)
    }

    fun replaceActiveCard(card: FlashcardCardDraft): DeckDraft =
        copy(cards = cards.replaceAt(activeIndex, card))

    fun removeActiveCard(): DeckDraft {
        if (cards.size == 1) {
            return this
        }

        val nextCards = cards.filterIndexed { index, _ -> index != activeIndex }
        return copy(
            cards = nextCards,
            activeIndex = activeIndex.coerceAtMost(nextCards.lastIndex)
        )
    }

    fun toPayload(): FlashcardDeckCreateRequest =
        FlashcardDeckCreateRequest(
            title = title.trim(),
            description = description.trim().ifBlank { null },
            notebookUuid = notebookUuid?.takeUnless { it.isBlank() },
            cards = validCards.map { it.toCard() }
        )

    companion object {
        fun from(deck: FlashcardDeckDetail?): DeckDraft =
            DeckDraft(
                title = deck?.title.orEmpty(),
                description = deck?.description.orEmpty(),
                notebookUuid = deck?.notebookUuid,
                cards = deck?.cards?.takeIf { it.isNotEmpty() }?.map { FlashcardCardDraft.from(it) }
                    ?: listOf(createCard()),
                activeIndex = 0
            )
    }
}

private data class FlashcardCardDraft(
    val front: String,
    val back: String
) {
    val isValid: Boolean
        get() = front.isNotBlank() && back.isNotBlank()

    fun toCard(): FlashcardCard =
        FlashcardCard(front = front.trim(), back = back.trim())

    companion object {
        fun from(card: FlashcardCard): FlashcardCardDraft =
            FlashcardCardDraft(front = card.front, back = card.back)
    }
}

private fun createCard(): FlashcardCardDraft =
    FlashcardCardDraft(front = "", back = "")

private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
    mapIndexed { itemIndex, item -> if (itemIndex == index) value else item }
