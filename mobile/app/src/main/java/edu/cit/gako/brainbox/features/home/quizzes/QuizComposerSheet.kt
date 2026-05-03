package edu.cit.gako.brainbox.features.home.quizzes

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
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizCreateRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizDetail
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizQuestion
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

private val DifficultyOptions = listOf("Easy", "Medium", "Hard")
private val OptionLabels = listOf("A", "B", "C", "D")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuizComposerSheet(
    mode: QuizComposerMode,
    initialQuiz: QuizDetail?,
    notebooks: List<NotebookSummary>,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (QuizCreateRequest) -> Unit,
    onDelete: (QuizDetail) -> Unit
) {
    var draft by remember(initialQuiz?.uuid, mode) {
        mutableStateOf(QuizDraft.from(initialQuiz))
    }
    val activeQuestion = draft.activeQuestion
    val canSubmit = draft.canSubmit
    val title = if (mode == QuizComposerMode.CREATE) "Create quiz" else "Edit quiz"

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
                    Text("Build it the same way as web, tuned for one-thumb edits.", style = MaterialTheme.typography.bodySmall, color = Ink3)
                }
                if (mode == QuizComposerMode.EDIT && initialQuiz != null) {
                    TextButton(
                        onClick = { onDelete(initialQuiz) },
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
                BrandedEditorField(
                    label = "Quiz title",
                    value = draft.title,
                    onValueChange = { draft = draft.copy(title = it) },
                    placeholder = "Quiz title"
                )
                BrandedEditorField(
                    label = "Description",
                    value = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    placeholder = "What is this quiz testing?",
                    minLines = 2
                )
                ChipSection(label = "Difficulty") {
                    DifficultyOptions.forEach { difficulty ->
                        EditorChip(
                            label = difficulty,
                            selected = draft.difficulty == difficulty,
                            onClick = { draft = draft.copy(difficulty = difficulty) }
                        )
                    }
                }
                NotebookPicker(
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
                                Text("Questions", style = MaterialTheme.typography.titleMedium, color = Ink)
                                Text("${draft.questions.size} total", style = MaterialTheme.typography.bodySmall, color = Ink3)
                            }
                            OutlinedButton(
                                onClick = { draft = draft.addQuestion("multiple-choice") },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Border),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Text("MC")
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            draft.questions.forEachIndexed { index, _ ->
                                EditorChip(
                                    label = "Q${index + 1}",
                                    selected = draft.activeIndex == index,
                                    onClick = { draft = draft.copy(activeIndex = index) }
                                )
                            }
                            EditorChip(
                                label = "+ T/F",
                                selected = false,
                                onClick = { draft = draft.addQuestion("true-false") }
                            )
                        }
                    }
                }

                QuestionEditor(
                    question = activeQuestion,
                    index = draft.activeIndex,
                    total = draft.questions.size,
                    onQuestionChange = { nextQuestion -> draft = draft.replaceActiveQuestion(nextQuestion) },
                    onRemove = { draft = draft.removeActiveQuestion() }
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
                    enabled = canSubmit && !isBusy,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(color = White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Text(if (mode == QuizComposerMode.CREATE) "Create" else "Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionEditor(
    question: QuizQuestionDraft,
    index: Int,
    total: Int,
    onQuestionChange: (QuizQuestionDraft) -> Unit,
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
                    TokenBadge("Q${index + 1}")
                    Text("Question ${index + 1} of $total", style = MaterialTheme.typography.titleMedium, color = Ink)
                }
                TextButton(onClick = onRemove, enabled = total > 1) {
                    Text("Remove", color = if (total > 1) ErrorRed else Ink4)
                }
            }
            ChipSection(label = "Type") {
                EditorChip(
                    label = "Multiple choice",
                    selected = question.type == "multiple-choice",
                    onClick = {
                        onQuestionChange(
                            QuizQuestionDraft(
                                type = "multiple-choice",
                                text = question.text,
                                options = listOf("", "", "", ""),
                                correctIndex = 0
                            )
                        )
                    }
                )
                EditorChip(
                    label = "True / False",
                    selected = question.type == "true-false",
                    onClick = {
                        onQuestionChange(
                            QuizQuestionDraft(
                                type = "true-false",
                                text = question.text,
                                options = listOf("True", "False"),
                                correctIndex = 0
                            )
                        )
                    }
                )
            }
            BrandedEditorField(
                label = "Question",
                value = question.text,
                onValueChange = { onQuestionChange(question.copy(text = it)) },
                placeholder = "Write the question here",
                minLines = 3
            )
            if (question.type == "true-false") {
                ChipSection(label = "Correct answer") {
                    listOf("True", "False").forEachIndexed { optionIndex, option ->
                        EditorChip(
                            label = option,
                            selected = question.correctIndex == optionIndex,
                            onClick = { onQuestionChange(question.copy(correctIndex = optionIndex)) }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Options", style = MaterialTheme.typography.labelMedium, color = Ink3)
                    question.options.forEachIndexed { optionIndex, option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EditorChip(
                                label = OptionLabels.getOrElse(optionIndex) { "${optionIndex + 1}" },
                                selected = question.correctIndex == optionIndex,
                                onClick = { onQuestionChange(question.copy(correctIndex = optionIndex)) }
                            )
                            OutlinedTextField(
                                value = option,
                                onValueChange = { value ->
                                    onQuestionChange(question.copy(options = question.options.replaceAt(optionIndex, value)))
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Option ${OptionLabels.getOrElse(optionIndex) { "${optionIndex + 1}" }}") },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = editorTextFieldColors(),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                    Text("Tap the option badge to mark the correct answer.", style = MaterialTheme.typography.bodySmall, color = Ink3)
                }
            }
        }
    }
}

@Composable
private fun NotebookPicker(
    selectedNotebookUuid: String?,
    notebooks: List<NotebookSummary>,
    onSelect: (String?) -> Unit
) {
    ChipSection(label = "Notebook") {
        EditorChip(
            label = "No notebook",
            selected = selectedNotebookUuid.isNullOrBlank(),
            onClick = { onSelect(null) }
        )
        notebooks.forEach { notebook ->
            EditorChip(
                label = notebook.title,
                selected = selectedNotebookUuid == notebook.uuid,
                onClick = { onSelect(notebook.uuid) }
            )
        }
    }
}

@Composable
private fun ChipSection(
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
private fun EditorChip(
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
private fun BrandedEditorField(
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
            colors = editorTextFieldColors(),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun editorTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Accent,
    unfocusedBorderColor = Border,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
    cursorColor = Accent,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink
)

internal enum class QuizComposerMode { CREATE, EDIT }

private data class QuizDraft(
    val title: String,
    val description: String,
    val difficulty: String,
    val notebookUuid: String?,
    val questions: List<QuizQuestionDraft>,
    val activeIndex: Int
) {
    val activeQuestion: QuizQuestionDraft
        get() = questions.getOrElse(activeIndex.coerceIn(0, questions.lastIndex)) { createQuestion() }

    val validQuestions: List<QuizQuestionDraft>
        get() = questions.filter { it.isValid }

    val canSubmit: Boolean
        get() = title.isNotBlank() && validQuestions.isNotEmpty()

    fun addQuestion(type: String): QuizDraft {
        val nextQuestions = questions + createQuestion(type)
        return copy(questions = nextQuestions, activeIndex = nextQuestions.lastIndex)
    }

    fun replaceActiveQuestion(question: QuizQuestionDraft): QuizDraft =
        copy(questions = questions.replaceAt(activeIndex, question))

    fun removeActiveQuestion(): QuizDraft {
        if (questions.size == 1) {
            return this
        }

        val nextQuestions = questions.filterIndexed { index, _ -> index != activeIndex }
        return copy(
            questions = nextQuestions,
            activeIndex = activeIndex.coerceAtMost(nextQuestions.lastIndex)
        )
    }

    fun toPayload(): QuizCreateRequest =
        QuizCreateRequest(
            title = title.trim(),
            description = description.trim().ifBlank { null },
            difficulty = difficulty,
            notebookUuid = notebookUuid?.takeUnless { it.isBlank() },
            questions = validQuestions.map { it.toQuestion() }
        )

    companion object {
        fun from(quiz: QuizDetail?): QuizDraft =
            QuizDraft(
                title = quiz?.title.orEmpty(),
                description = quiz?.description.orEmpty(),
                difficulty = quiz?.difficulty?.takeIf { it in DifficultyOptions } ?: "Medium",
                notebookUuid = quiz?.notebookUuid,
                questions = quiz?.questions?.takeIf { it.isNotEmpty() }?.map { QuizQuestionDraft.from(it) }
                    ?: listOf(createQuestion()),
                activeIndex = 0
            )
    }
}

private data class QuizQuestionDraft(
    val type: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int
) {
    val isValid: Boolean
        get() = text.isNotBlank() && options.isNotEmpty() && options.all { it.isNotBlank() }

    fun toQuestion(): QuizQuestion =
        QuizQuestion(
            type = if (type == "true-false") "true-false" else "multiple-choice",
            text = text.trim(),
            options = if (type == "true-false") listOf("True", "False") else options.map { it.trim() },
            correctIndex = correctIndex.coerceIn(0, options.lastIndex.coerceAtLeast(0))
        )

    companion object {
        fun from(question: QuizQuestion): QuizQuestionDraft {
            if (question.type == "true-false") {
                return QuizQuestionDraft(
                    type = "true-false",
                    text = question.text,
                    options = listOf("True", "False"),
                    correctIndex = question.correctIndex.coerceIn(0, 1)
                )
            }

            val normalizedOptions = question.options.takeIf { it.size == 4 } ?: buildList {
                addAll(question.options.take(4))
                repeat((4 - size).coerceAtLeast(0)) { add("") }
            }
            return QuizQuestionDraft(
                type = "multiple-choice",
                text = question.text,
                options = normalizedOptions,
                correctIndex = question.correctIndex.coerceIn(0, normalizedOptions.lastIndex.coerceAtLeast(0))
            )
        }
    }
}

private fun createQuestion(type: String = "multiple-choice"): QuizQuestionDraft =
    if (type == "true-false") {
        QuizQuestionDraft(
            type = "true-false",
            text = "",
            options = listOf("True", "False"),
            correctIndex = 0
        )
    } else {
        QuizQuestionDraft(
            type = "multiple-choice",
            text = "",
            options = listOf("", "", "", ""),
            correctIndex = 0
        )
    }

private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
    mapIndexed { itemIndex, item -> if (itemIndex == index) value else item }
