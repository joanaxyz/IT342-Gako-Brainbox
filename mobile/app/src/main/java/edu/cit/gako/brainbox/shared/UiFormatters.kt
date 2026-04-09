package edu.cit.gako.brainbox.shared

import androidx.compose.ui.graphics.Color
import edu.cit.gako.brainbox.ui.theme.ErrorRed
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.SuccessGreen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatShortDate(value: String): String =
    parseInstant(value)
        ?.atZone(ZoneId.systemDefault())
        ?.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
        ?: "Recently"

internal fun formatLongDate(value: String): String =
    parseInstant(value)
        ?.atZone(ZoneId.systemDefault())
        ?.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH))
        ?: "Recently"

internal fun joinMeta(vararg values: String?): String =
    values.mapNotNull { value -> value?.takeIf { it.isNotBlank() } }
        .joinToString(" - ")

internal fun progressColor(value: Int): Color = when {
    value >= 70 -> SuccessGreen
    value >= 40 -> Ink2
    else -> ErrorRed
}

private fun parseInstant(value: String): Instant? = runCatching { Instant.parse(value) }.getOrNull()

