package com.it342.brainbox.study

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it342.brainbox.network.models.QuizDetail
import com.it342.brainbox.shared.TokenBadge
import com.it342.brainbox.shared.joinMeta
import com.it342.brainbox.ui.theme.Accent
import com.it342.brainbox.ui.theme.Border
import com.it342.brainbox.ui.theme.Cream
import com.it342.brainbox.ui.theme.ErrorRed
import com.it342.brainbox.ui.theme.Ink
import com.it342.brainbox.ui.theme.Ink2
import com.it342.brainbox.ui.theme.Ink3
import com.it342.brainbox.ui.theme.SuccessGreen
import com.it342.brainbox.ui.theme.White

@Composable
internal fun QuizStudyScreen(
    quiz: QuizDetail,
    onExit: () -> Unit,
    onRecordAttempt: (String, Int) -> Unit
) {
    BackHandler(onBack = onExit)

    var currentQuestionIndex by rememberSaveable(quiz.uuid) { mutableStateOf(0) }
    var selectedOption by rememberSaveable(quiz.uuid) { mutableStateOf<Int?>(null) }
    var isSubmitted by rememberSaveable(quiz.uuid) { mutableStateOf(false) }
    var answers by remember(quiz.uuid) { mutableStateOf(emptyList<QuizAnswerReview>()) }
    var isFinished by rememberSaveable(quiz.uuid) { mutableStateOf(false) }
    var attemptRecorded by rememberSaveable(quiz.uuid) { mutableStateOf(false) }

    val totalQuestions = quiz.questions.size
    if (totalQuestions == 0) {
        StudyUnavailableScreen(
            badge = "QZ",
            title = quiz.title,
            body = "This quiz does not have any questions yet. Add content on the web and reopen it here.",
            onExit = onExit
        )
        return
    }

    val score = answers.count { it.isCorrect }
    val percentage = (score * 100) / totalQuestions

    LaunchedEffect(isFinished, attemptRecorded, quiz.uuid, percentage) {
        if (isFinished && !attemptRecorded) {
            attemptRecorded = true
            onRecordAttempt(quiz.uuid, percentage)
        }
    }

    if (isFinished) {
        QuizResultsScreen(
            quiz = quiz,
            answers = answers,
            score = score,
            percentage = percentage,
            onExit = onExit,
            onRetry = {
                currentQuestionIndex = 0
                selectedOption = null
                isSubmitted = false
                answers = emptyList()
                isFinished = false
                attemptRecorded = false
            }
        )
        return
    }

    val question = quiz.questions[currentQuestionIndex]
    val progress = (currentQuestionIndex + 1) / totalQuestions.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StudySessionHeader(
            badge = "QZ",
            title = quiz.title,
            subtitle = joinMeta(
                quiz.difficulty,
                quiz.notebookTitle,
                "Question ${currentQuestionIndex + 1} of $totalQuestions"
            ),
            onExit = onExit
        )
        StudyProgressCard(
            progress = progress,
            progressLabel = "${currentQuestionIndex + 1}/$totalQuestions",
            helper = quiz.estimatedTime ?: "Choose one answer, then submit to continue."
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = White,
            border = BorderStroke(1.dp, Border),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TokenBadge(text = if (question.type == "true-false") "TF" else "MC")
                Text(text = question.text, style = MaterialTheme.typography.headlineSmall, color = Ink)
                Text(
                    text = if (isSubmitted) {
                        if ((selectedOption ?: -1) == question.correctIndex) {
                            "Correct. Move on when you're ready."
                        } else {
                            "Review the correct answer, then continue."
                        }
                    } else {
                        "Tap an option to lock in your answer."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink2
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { index, option ->
                QuizOptionCard(
                    label = when {
                        question.type == "true-false" && index == 0 -> "T"
                        question.type == "true-false" && index == 1 -> "F"
                        else -> ('A' + index).toString()
                    },
                    option = option,
                    isSelected = selectedOption == index,
                    isCorrect = isSubmitted && question.correctIndex == index,
                    isIncorrectSelection = isSubmitted && selectedOption == index && question.correctIndex != index,
                    enabled = !isSubmitted
                ) {
                    selectedOption = index
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (currentQuestionIndex > 0 && !isSubmitted) {
                        currentQuestionIndex -= 1
                        selectedOption = null
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = currentQuestionIndex > 0 && !isSubmitted,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = Ink2),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Previous")
            }
            if (!isSubmitted) {
                Button(
                    onClick = {
                        val pickedOption = selectedOption ?: return@Button
                        answers = answers + QuizAnswerReview(
                            questionText = question.text,
                            options = question.options,
                            selectedIndex = pickedOption,
                            correctIndex = question.correctIndex,
                            isCorrect = pickedOption == question.correctIndex
                        )
                        isSubmitted = true
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedOption != null,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Submit")
                }
            } else {
                Button(
                    onClick = {
                        if (currentQuestionIndex == totalQuestions - 1) {
                            isFinished = true
                        } else {
                            currentQuestionIndex += 1
                            selectedOption = null
                            isSubmitted = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(if (currentQuestionIndex == totalQuestions - 1) "See results" else "Next")
                }
            }
        }
    }
}

@Composable
private fun QuizResultsScreen(
    quiz: QuizDetail,
    answers: List<QuizAnswerReview>,
    score: Int,
    percentage: Int,
    onExit: () -> Unit,
    onRetry: () -> Unit
) {
    BackHandler(onBack = onExit)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StudySessionHeader(
            badge = "QZ",
            title = quiz.title,
            subtitle = "Results saved back to your home data.",
            onExit = onExit
        )
        StudyResultHero(
            percentage = percentage,
            title = when {
                percentage >= 80 -> "Excellent work"
                percentage >= 50 -> "Good run"
                else -> "Keep practicing"
            },
            subtitle = "You answered $score out of ${answers.size} questions correctly."
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            answers.forEachIndexed { index, answer ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = White,
                    border = BorderStroke(
                        1.dp,
                        if (answer.isCorrect) SuccessGreen.copy(alpha = 0.35f) else ErrorRed.copy(alpha = 0.28f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Question ${index + 1}", style = MaterialTheme.typography.labelLarge, color = Ink3)
                            TokenBadge(if (answer.isCorrect) "OK" else "FIX")
                        }
                        Text(answer.questionText, style = MaterialTheme.typography.titleMedium, color = Ink)
                        if (!answer.isCorrect) {
                            Text(
                                text = "Your answer: ${answer.options.getOrNull(answer.selectedIndex).orEmpty()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRed
                            )
                        }
                        Text(
                            text = "Correct answer: ${answer.options.getOrNull(answer.correctIndex).orEmpty()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (answer.isCorrect) SuccessGreen else Ink2
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = Ink2),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Back to quizzes")
            }
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Try again")
            }
        }
    }
}

private data class QuizAnswerReview(
    val questionText: String,
    val options: List<String>,
    val selectedIndex: Int,
    val correctIndex: Int,
    val isCorrect: Boolean
)
