package edu.cit.gako.brainbox.notebook

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import edu.cit.gako.brainbox.R
import edu.cit.gako.brainbox.network.models.NotebookDetail
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.White
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener

@Composable
internal fun NativeRichTextEditorCard(
    initialHtml: String,
    bridgeState: MutableState<NativeEditorBridge?>,
    onHtmlChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = White,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 8.dp
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                NotebookEditorView(context).apply {
                    setHtml(initialHtml)
                    setOnHtmlChangedListener(onHtmlChange)
                    bridgeState.value = NativeEditorBridge(
                        currentHtmlProvider = { currentHtml() },
                        setHtmlProvider = { setHtml(it) },
                        selectedTextProvider = { selectedText() },
                        focusHeadingProvider = { focusHeading(it) }
                    )
                }
            },
            update = { view ->
                if (view.currentHtml() != initialHtml) {
                    view.setHtml(initialHtml)
                }
            }
        )
    }
}

internal class NativeEditorBridge(
    private val currentHtmlProvider: () -> String,
    private val setHtmlProvider: (String) -> Unit,
    private val selectedTextProvider: () -> String,
    private val focusHeadingProvider: (String) -> Unit
) {
    fun currentHtml(): String = currentHtmlProvider()

    fun setHtml(value: String) {
        setHtmlProvider(value)
    }

    fun selectedText(): String = selectedTextProvider()

    fun focusHeading(heading: String) {
        focusHeadingProvider(heading)
    }
}

private class NotebookEditorView(context: Context) : LinearLayout(context), IAztecToolbarClickListener {
    private val toolbar = AztecToolbar(context)
    private val visualEditor = AztecText(context)
    private val sourceEditor = SourceViewEditText(context)
    private val surfaceColor = ContextCompat.getColor(context, R.color.surface_color)
    private val backgroundColor = ContextCompat.getColor(context, R.color.background_color)
    private val borderColor = ContextCompat.getColor(context, R.color.border_color)
    private val textColor = ContextCompat.getColor(context, R.color.text_primary)
    private val secondaryTextColor = ContextCompat.getColor(context, R.color.text_secondary)
    private var lastAppliedHtml = "<p></p>"
    private var onHtmlChanged: ((String) -> Unit)? = null

    init {
        orientation = VERTICAL
        setPadding(0, 0, 0, 0)
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(android.graphics.Color.TRANSPARENT)
        }

        toolbar.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(1)
        }
        visualEditor.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        sourceEditor.layoutParams = LayoutParams(1, 1)
        sourceEditor.visibility = View.GONE

        addView(toolbar)
        addView(visualEditor)
        addView(sourceEditor)

        Aztec.with(visualEditor, sourceEditor, toolbar, this)
        toolbar.enableTaskList()
        toolbar.setBackgroundColor(surfaceColor)
        toolbar.setPadding(dp(8), dp(10), dp(8), dp(12))
        visualEditor.setBackgroundColor(backgroundColor)
        visualEditor.setPadding(dp(20), dp(22), dp(20), dp(22))
        visualEditor.setTextColor(textColor)
        visualEditor.setHintTextColor(secondaryTextColor)
        visualEditor.setLinkTextColor(ContextCompat.getColor(context, R.color.primary_color))
        visualEditor.setHint("Start writing your notebook...")
        visualEditor.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
        visualEditor.setLineSpacing(0f, 1.12f)
        visualEditor.minHeight = dp(240)
        sourceEditor.setBackgroundColor(surfaceColor)
        sourceEditor.setTextColor(textColor)
        sourceEditor.setHintTextColor(secondaryTextColor)
        visualEditor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val nextHtml = currentHtml()
                if (nextHtml != lastAppliedHtml) {
                    lastAppliedHtml = nextHtml
                    onHtmlChanged?.invoke(nextHtml)
                }
            }
        })
    }

    fun setOnHtmlChangedListener(listener: (String) -> Unit) {
        onHtmlChanged = listener
    }

    fun setHtml(html: String) {
        val normalizedHtml = normalizeHtml(html)
        if (normalizedHtml == lastAppliedHtml) return
        lastAppliedHtml = normalizedHtml
        visualEditor.fromHtml(normalizedHtml)
    }

    fun currentHtml(): String = normalizeHtml(visualEditor.toHtml())

    fun selectedText(): String {
        val start = visualEditor.selectionStart
        val end = visualEditor.selectionEnd
        return if (start < 0 || end <= start) "" else visualEditor.text?.substring(start, end).orEmpty()
    }

    fun focusHeading(heading: String) {
        val normalizedHeading = heading.trim()
        if (normalizedHeading.isBlank()) {
            return
        }
        val bodyText = visualEditor.text?.toString().orEmpty()
        val matchIndex = bodyText.indexOf(normalizedHeading, ignoreCase = true)
        if (matchIndex >= 0) {
            visualEditor.post {
                visualEditor.requestFocus()
                val selection = matchIndex.coerceIn(0, bodyText.length)
                visualEditor.setSelection(selection)
            }
        }
    }

    override fun onToolbarCollapseButtonClicked() = Unit
    override fun onToolbarExpandButtonClicked() = Unit
    override fun onToolbarFormatButtonClicked(format: ITextFormat, isChecked: Boolean) = Unit
    override fun onToolbarHeadingButtonClicked() = Unit
    override fun onToolbarHtmlButtonClicked() = Unit
    override fun onToolbarListButtonClicked() = Unit
    override fun onToolbarMediaButtonClicked(): Boolean = false

    private fun normalizeHtml(value: String): String = value.trim().ifBlank { "<p></p>" }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

internal suspend fun readDocumentText(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
        reader.readText()
    }.orEmpty()
}

internal fun shareNotebookHtml(context: Context, title: String, html: String) {
    val exportDirectory = (context.externalCacheDir ?: context.cacheDir).resolve("notebook-exports").apply { mkdirs() }
    val safeTitle = title.replace(Regex("[^A-Za-z0-9._-]"), "-").trim('-').ifBlank { "brainbox-notebook" }
    val exportFile = File(exportDirectory, "$safeTitle.html")
    exportFile.writeText(html)
    val exportUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", exportFile)
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, exportUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Share notebook"
        )
    )
}

internal fun EditorUiState.toNotebookDetail(): NotebookDetail? {
    val resolvedUuid = resolvedNotebookUuid ?: notebookUuid
    if (resolvedUuid.isBlank()) return null
    return NotebookDetail(
        uuid = resolvedUuid,
        title = title.ifBlank { "Untitled notebook" },
        content = contentHtml,
        categoryId = categoryId,
        categoryName = categoryName
    )
}
