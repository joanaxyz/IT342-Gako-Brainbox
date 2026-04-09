package edu.cit.gako.brainbox.study

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.network.models.FlashcardDeckDetail
import edu.cit.gako.brainbox.shared.OutlinedActionButton
import edu.cit.gako.brainbox.shared.TokenBadge
import edu.cit.gako.brainbox.shared.joinMeta
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream
import edu.cit.gako.brainbox.ui.theme.ErrorRed
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.SuccessGreen
import edu.cit.gako.brainbox.ui.theme.White

@Composable
internal fun FlashcardStudyScreen(
    deck: FlashcardDeckDetail,
    onExit: () -> Unit,
    onRecordAttempt: (String, Int) -> Unit
) {
    BackHandler(onBack = onExit)

    var currentIndex by rememberSaveable(deck.uuid) { mutableStateOf(0) }
    var isFlipped by rememberSaveable(deck.uuid) { mutableStateOf(false) }
    var evaluations by rememberSaveable(deck.uuid) { mutableStateOf(emptyMap<Int, Boolean>()) }
    var isFinished by rememberSaveable(deck.uuid) { mutableStateOf(false) }
    var attemptRecorded by rememberSaveable(deck.uuid) { mutableStateOf(false) }

    val totalCards = deck.cards.size
    if (totalCards == 0) {
        StudyUnavailableScreen(
            badge = "FC",
            title = deck.title,
            body = "This deck does not have any cards yet. Add them on the web and reopen it here.",
            onExit = onExit
        )
        return
    }

    val knownCount = evaluations.values.count { it }
    val unknownCount = evaluations.values.count { !it }
    val mastery = (knownCount * 100) / totalCards

    LaunchedEffect(isFinished, attemptRecorded, deck.uuid, mastery) {
        if (isFinished && !attemptRecorded) {
            attemptRecorded = true
            onRecordAttempt(deck.uuid, mastery)
        }
    }

    if (isFinished) {
        FlashcardResultsScreen(
            deck = deck,
            mastery = mastery,
            knownCount = knownCount,
            unknownCount = unknownCount,
            onExit = onExit,
            onRetry = {
                currentIndex = 0
                isFlipped = false
                evaluations = emptyMap()
                isFinished = false
                attemptRecorded = false
            }
        )
        return
    }

    val currentCard = deck.cards[currentIndex]
    val progress = (currentIndex + 1) / totalCards.toFloat()

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
            badge = "FC",
            title = deck.title,
            subtitle = joinMeta(deck.notebookTitle, "Card ${currentIndex + 1} of $totalCards"),
            onExit = onExit
        )
        StudyProgressCard(
            progress = progress,
            progressLabel = "${currentIndex + 1}/$totalCards",
            helper = "Flip the card, then mark whether you knew it."
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = White,
            border = BorderStroke(1.dp, Border),
            shadowElevation = 8.dp,
            onClick = { isFlipped = !isFlipped }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TokenBadge(if (isFlipped) "BACK" else "FRONT")
                Text(
                    text = if (isFlipped) "Answer" else "Question",
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink3
                )
                Text(
                    text = if (isFlipped) currentCard.back else currentCard.front,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink
                )
                Text(
                    text = if (isFlipped) "Tap the card to show the prompt again." else "Tap the card to reveal the answer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
            }
        }
        OutlinedActionButton(
            label = if (isFlipped) "Show question" else "Reveal answer",
            onClick = { isFlipped = !isFlipped }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (currentIndex > 0) {
                        currentIndex -= 1
                        isFlipped = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = currentIndex > 0,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = Ink2),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Previous")
            }
            OutlinedButton(
                onClick = {
                    evaluations = evaluations + (currentIndex to false)
                    if (currentIndex == totalCards - 1) {
                        isFinished = true
                    } else {
                        currentIndex += 1
                        isFlipped = false
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.24f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = ErrorRed),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Unknown")
            }
            Button(
                onClick = {
                    evaluations = evaluations + (currentIndex to true)
                    if (currentIndex == totalCards - 1) {
                        isFinished = true
                    } else {
                        currentIndex += 1
                        isFlipped = false
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Known")
            }
        }
    }
}

@Composable
private fun FlashcardResultsScreen(
    deck: FlashcardDeckDetail,
    mastery: Int,
    knownCount: Int,
    unknownCount: Int,
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
            badge = "FC",
            title = deck.title,
            subtitle = "Progress saved back to your home data.",
            onExit = onExit
        )
        StudyResultHero(
            percentage = mastery,
            title = when {
                mastery >= 80 -> "Excellent recall"
                mastery >= 50 -> "Solid progress"
                else -> "Keep reviewing"
            },
            subtitle = "You marked $knownCount cards known and $unknownCount still learning."
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = White,
            border = BorderStroke(1.dp, Border)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Known", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Text(knownCount.toString(), style = MaterialTheme.typography.headlineSmall, color = SuccessGreen)
                }
                HorizontalDivider(color = Border)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Still learning", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Text(unknownCount.toString(), style = MaterialTheme.typography.headlineSmall, color = ErrorRed)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = Ink2),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Study again")
            }
            Button(
                onClick = onExit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Back to decks")
            }
        }
    }
}

