package edu.cit.gako.brainbox.features.notebook.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.White

@Composable
internal fun NotebookEditorError(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    secondaryActionLabel: String = "Close",
    onSecondaryAction: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp, start = 24.dp, end = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = White,
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Editor unavailable", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Ink2)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White)
                ) {
                    Text("Retry")
                }
                OutlinedButton(onClick = onSecondaryAction ?: onClose) {
                    Text(secondaryActionLabel)
                }
            }
        }
    }
}
