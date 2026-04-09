package edu.cit.gako.brainbox.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream
import edu.cit.gako.brainbox.ui.theme.Cream2
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.Ink3
import edu.cit.gako.brainbox.ui.theme.Ink4
import edu.cit.gako.brainbox.ui.theme.White
import java.util.Locale

@Composable
internal fun AuthHeader(title: String, subtitle: String, eyebrow: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!eyebrow.isNullOrBlank()) {
            Text(
                eyebrow.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = Ink4
            )
        }
        Text(title, style = MaterialTheme.typography.headlineLarge, color = Ink)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Ink3)
    }
}

@Composable
internal fun BrandedTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Ink3)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = Ink4) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent.copy(alpha = 0.8f),
                unfocusedBorderColor = Border.copy(alpha = 0.55f),
                focusedContainerColor = Cream,
                unfocusedContainerColor = Cream2.copy(alpha = 0.55f),
                cursorColor = Accent,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                focusedPlaceholderColor = Ink4,
                unfocusedPlaceholderColor = Ink4
            ),
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
internal fun BrandedSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = Ink4) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Accent,
            unfocusedBorderColor = Border,
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            cursorColor = Accent,
            focusedTextColor = Ink,
            unfocusedTextColor = Ink
        ),
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
internal fun PrimaryActionButton(label: String, isBusy: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = White),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp)
    ) {
        if (isBusy) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = White, strokeWidth = 2.dp)
        } else {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
internal fun OutlinedActionButton(label: String, isBusy: Boolean = false, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = Ink2),
        border = BorderStroke(1.dp, Border),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp)
    ) {
        if (isBusy) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Accent, strokeWidth = 2.dp)
        } else {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
internal fun InlinePrompt(text: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = Ink3)
        TextButton(
            onClick = onAction,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Text(action, style = MaterialTheme.typography.bodySmall, color = Accent)
        }
    }
}

@Composable
internal fun CodeInputRow(value: String, onValueChange: (String) -> Unit, length: Int = 6) {
    BasicTextField(
        value = value,
        onValueChange = { incoming -> onValueChange(incoming.filter { it.isDigit() }.take(length)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(length) { index ->
                    val digit = value.getOrNull(index)?.toString().orEmpty()
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        color = Cream,
                        border = BorderStroke(1.5.dp, if (digit.isEmpty()) Border else Accent.copy(alpha = 0.82f))
                    ) {
                        Box(modifier = Modifier.height(62.dp), contentAlignment = Alignment.Center) {
                            Text(
                                digit,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Ink
                            )
                        }
                    }
                }
            }
        }
    )
}

