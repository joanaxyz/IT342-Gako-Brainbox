package edu.cit.gako.brainbox.features.notebook.editor

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import edu.cit.gako.brainbox.features.playback.audio.BrainBoxAudioClient
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioSnapshot
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsChunk
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsRequest
import edu.cit.gako.brainbox.features.playback.tts.buildNotebookTtsRequest
import edu.cit.gako.brainbox.features.notebook.data.NotebookMutationResult
import edu.cit.gako.brainbox.platform.network.RetrofitClient
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookDetail
import edu.cit.gako.brainbox.shared.ui.theme.Cream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private const val EMBEDDED_EDITOR_DOMAIN = "appassets.androidplatform.net"
private const val EMBEDDED_EDITOR_BASE_URL = "http://$EMBEDDED_EDITOR_DOMAIN"
private const val EMBEDDED_EDITOR_ENTRY_URL = "$EMBEDDED_EDITOR_BASE_URL/mobile-editor.html"
private const val NOTEBOOK_EDITOR_READY_TIMEOUT_MS = 15_000L

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun WebViewNotebookEditor(
    notebookUuid: String,
    onClose: () -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onWebError: (String) -> Unit = {}
) {
    if (notebookUuid == "new") {
        CreateAndOpenNotebookWebView(
            onClose = onClose,
            onOpenQuiz = onOpenQuiz,
            onOpenFlashcardDeck = onOpenFlashcardDeck
        )
    } else {
        NotebookWebView(
            notebookUuid = notebookUuid,
            onClose = onClose,
            onOpenQuiz = onOpenQuiz,
            onOpenFlashcardDeck = onOpenFlashcardDeck,
            onWebError = onWebError
        )
    }
}

@Composable
private fun CreateAndOpenNotebookWebView(
    onClose: () -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit
) {
    val context = LocalContext.current
    val appGraph = remember(context.applicationContext) {
        BrainBoxAppGraph.from(context.applicationContext)
    }

    var resolvedUuid by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        launch {
            val result = runCatching {
                appGraph.notebookRepository.createNotebook(
                    title = "Untitled notebook",
                    categoryId = null,
                    content = "<p></p>"
                )
            }.getOrElse { NotebookMutationResult.Failure(it.message ?: "Couldn't create notebook.") }

            when (result) {
                is NotebookMutationResult.Success -> resolvedUuid = result.notebook?.uuid
                is NotebookMutationResult.Failure -> errorMessage = result.message
                is NotebookMutationResult.Conflict -> errorMessage = result.message
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when {
            errorMessage != null -> NotebookEditorError(
                message = errorMessage!!,
                onRetry = {},
                onClose = onClose
            )

            resolvedUuid != null -> NotebookWebView(
                notebookUuid = resolvedUuid!!,
                onClose = onClose,
                onOpenQuiz = onOpenQuiz,
                onOpenFlashcardDeck = onOpenFlashcardDeck
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun NotebookWebView(
    notebookUuid: String,
    onClose: () -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onWebError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appGraph = remember(context.applicationContext) {
        BrainBoxAppGraph.from(context.applicationContext)
    }
    val audioClient = remember(context.applicationContext) {
        BrainBoxAudioClient(context.applicationContext)
    }
    val assetLoader = remember(context.applicationContext) {
        WebViewAssetLoader.Builder()
            .setDomain(EMBEDDED_EDITOR_DOMAIN)
            .setHttpAllowed(true)
            .addPathHandler("/", WebViewAssetLoader.AssetsPathHandler(context.applicationContext))
            .build()
    }

    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    var isEditorReady by remember { mutableStateOf(false) }
    var hasReportedLoadFailure by remember { mutableStateOf(false) }

    val reportLoadFailure: (String) -> Unit = { message ->
        if (!hasReportedLoadFailure) {
            hasReportedLoadFailure = true
            onWebError(message)
        }
    }

    LaunchedEffect(isEditorReady) {
        if (isEditorReady) {
            return@LaunchedEffect
        }

        delay(NOTEBOOK_EDITOR_READY_TIMEOUT_MS)
        if (!isEditorReady) {
            reportLoadFailure("Opening this notebook is taking longer than expected. Check your connection and try again.")
        }
    }

    val activeWebView = webViewRef.value
    LaunchedEffect(activeWebView) {
        val webView = activeWebView ?: return@LaunchedEffect
        appGraph.audioStore.snapshotFlow.collect { snapshot ->
            webView.emitHostAudioState(snapshot)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef.value?.apply {
                stopLoading()
                removeJavascriptInterface("BrainBoxHost")
                webChromeClient = null
                destroy()
            }
            webViewRef.value = null
        }
    }

    BackHandler {
        val wv = webViewRef.value
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val accessToken = appGraph.sessionManager.fetchAuthToken().orEmpty()
                val refreshToken = appGraph.sessionManager.fetchRefreshToken().orEmpty()

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setCookie(EMBEDDED_EDITOR_BASE_URL, "accessToken=$accessToken; path=/")
                cookieManager.setCookie(EMBEDDED_EDITOR_BASE_URL, "refreshToken=$refreshToken; path=/; Max-Age=604800")
                cookieManager.flush()

                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        setSupportMultipleWindows(false)
                    }
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest
                        ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

                        override fun onReceivedHttpError(
                            view: WebView,
                            request: WebResourceRequest,
                            errorResponse: WebResourceResponse
                        ) {
                            if (request.isForMainFrame) {
                                reportLoadFailure(
                                    "The notebook editor returned ${errorResponse.statusCode}. Try again in a moment."
                                )
                            }
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError
                        ) {
                            if (request.isForMainFrame) {
                                reportLoadFailure(
                                    error.description?.toString()
                                        ?.takeIf { it.isNotBlank() }
                                        ?: "We couldn't load the notebook editor."
                                )
                            }
                        }
                    }
                    addJavascriptInterface(
                        BrainBoxHostBridge(
                            onClose = onClose,
                            onPersistSession = { access, refresh ->
                                if (access.isNotBlank()) {
                                    appGraph.sessionManager.saveAuthToken(access)
                                }
                                if (refresh.isNotBlank()) {
                                    appGraph.sessionManager.saveRefreshToken(refresh)
                                }
                            },
                            onClearSession = onClose,
                            onReady = {
                                isEditorReady = true
                            },
                            onError = reportLoadFailure,
                            onOpenQuiz = onOpenQuiz,
                            onOpenFlashcardDeck = onOpenFlashcardDeck,
                            onPlayNotebookAudio = { payload ->
                                coroutineScope.launch(Dispatchers.Default) {
                                    val request = payload.toHostNotebookAudioRequest()
                                    if (request != null) {
                                        audioClient.play(request)
                                    }
                                }
                            },
                            onPauseAudio = audioClient::pause,
                            onResumeAudio = audioClient::resume,
                            onStopAudio = audioClient::stop,
                            onSetAudioSpeechRate = audioClient::setSpeechRate
                        ),
                        "BrainBoxHost"
                    )

                    val encodedHost = Uri.encode("android")
                    val encodedNotebookId = Uri.encode(notebookUuid)
                    val encodedApiBaseUrl = Uri.encode(RetrofitClient.apiBaseUrl)
                    loadUrl("$EMBEDDED_EDITOR_ENTRY_URL?host=$encodedHost&notebookId=$encodedNotebookId&apiBaseUrl=$encodedApiBaseUrl")

                    webViewRef.value = this
                }
            },
            update = { view ->
                webViewRef.value = view
            }
        )
    }
}

internal class BrainBoxHostBridge(
    private val onClose: () -> Unit,
    private val onPersistSession: (String, String) -> Unit,
    private val onClearSession: () -> Unit,
    private val onReady: () -> Unit,
    private val onError: (String) -> Unit,
    private val onOpenQuiz: (String) -> Unit,
    private val onOpenFlashcardDeck: (String) -> Unit,
    private val onPlayNotebookAudio: (String) -> Unit,
    private val onPauseAudio: () -> Unit,
    private val onResumeAudio: () -> Unit,
    private val onStopAudio: () -> Unit,
    private val onSetAudioSpeechRate: (Float) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun closeEditor() = mainHandler.post { onClose() }

    @JavascriptInterface
    fun persistSession(accessToken: String, refreshToken: String) =
        mainHandler.post { onPersistSession(accessToken, refreshToken) }

    @JavascriptInterface
    fun clearSession() = mainHandler.post { onClearSession() }

    @JavascriptInterface
    fun reportReady() = mainHandler.post { onReady() }

    @JavascriptInterface
    fun reportError(message: String) = mainHandler.post { onError(message) }

    @JavascriptInterface
    fun openQuiz(uuid: String) {
        if (uuid.isNotBlank()) mainHandler.post { onOpenQuiz(uuid) }
    }

    @JavascriptInterface
    fun openFlashcardDeck(uuid: String) {
        if (uuid.isNotBlank()) mainHandler.post { onOpenFlashcardDeck(uuid) }
    }

    @Suppress("UNUSED_PARAMETER")
    @JavascriptInterface
    fun exportFile(json: String) = Unit

    @JavascriptInterface
    fun requestPdfExport() = Unit

    @JavascriptInterface
    fun playNotebookAudio(payloadJson: String) {
        mainHandler.post { onPlayNotebookAudio(payloadJson) }
    }

    @JavascriptInterface
    fun pauseAudio() = mainHandler.post { onPauseAudio() }

    @JavascriptInterface
    fun resumeAudio() = mainHandler.post { onResumeAudio() }

    @JavascriptInterface
    fun stopAudio() = mainHandler.post { onStopAudio() }

    @JavascriptInterface
    fun setAudioSpeechRate(rate: String) {
        mainHandler.post { onSetAudioSpeechRate(rate.toFloatOrNull() ?: 1.0f) }
    }
}

private fun WebView.emitHostAudioState(snapshot: BrainBoxAudioSnapshot) {
    val payload = snapshot.toHostAudioStatePayload().toString()
    post {
        evaluateJavascript(
            "window.dispatchEvent(new CustomEvent('brainbox-host-audio-state', { detail: $payload }));",
            null
        )
    }
}

private fun BrainBoxAudioSnapshot.toHostAudioStatePayload(): JSONObject {
    val activeRequest = request
    val fullText = activeRequest?.playbackText.orEmpty()
    val totalCharCount = activeRequest?.chunks
        ?.lastOrNull()
        ?.endCharIndex
        ?.coerceAtLeast(fullText.length)
        ?: fullText.length
    val resolvedCharOffset = if (status == BrainBoxAudioPlaybackStatus.ENDED) {
        totalCharCount
    } else {
        currentCharOffset.coerceIn(0, totalCharCount.coerceAtLeast(0))
    }

    return JSONObject().apply {
        put("notebookId", activeRequest?.notebookId ?: JSONObject.NULL)
        put("notebookTitle", activeRequest?.notebookTitle ?: "")
        put("status", status.name)
        put("currentCharOffset", resolvedCharOffset)
        put("currentChunkIndex", currentChunkIndex)
        put("totalChunks", activeRequest?.chunks?.size ?: 0)
        put("totalChars", totalCharCount)
        put("speechRate", speechRate)
        put("fullText", fullText)
        put("isOfflineOnly", activeRequest?.offlineOnly == true)
        put("errorMessage", errorMessage ?: JSONObject.NULL)
        put("updatedAtEpochMs", updatedAtEpochMs)
    }
}

private fun String.toHostNotebookAudioRequest(): BrainBoxTtsRequest? {
    return runCatching {
        val payload = JSONObject(this)
        val notebookUuid = payload.optString("notebookUuid").trim()
        if (notebookUuid.isBlank()) {
            return@runCatching null
        }

        val title = payload.optString("notebookTitle")
            .trim()
            .ifBlank { "Notebook" }
        val content = payload.optString("content")
        val speechRate = payload.optDouble("speechRate", 1.0)
            .toFloat()
            .coerceIn(0.25f, 3.0f)
        val webFullText = payload.optString("fullText")
        val webChunks = payload.optJSONArray("chunks")
            ?.toHostTtsChunks(webFullText)
            .orEmpty()

        val baseRequest = if (webFullText.isNotBlank() && webChunks.isNotEmpty()) {
            BrainBoxTtsRequest(
                notebookId = notebookUuid,
                notebookTitle = title,
                chunks = webChunks,
                fullText = webFullText,
                offlineOnly = false
            )
        } else {
            buildNotebookTtsRequest(
                notebook = NotebookDetail(
                    uuid = notebookUuid,
                    title = title,
                    content = content
                ),
                html = content,
                offlineOnly = false
            )
        }
        val totalLength = baseRequest.playbackText.length
        val startCharOffset = payload.optInt("startCharOffset", 0)
            .coerceIn(0, totalLength.coerceAtLeast(0))
        val startChunkIndex = baseRequest.chunks
            .indexOfLast { chunk -> startCharOffset >= chunk.startCharIndex }
            .takeIf { index -> index >= 0 }
            ?: 0

        baseRequest.copy(
            speechRate = speechRate,
            startChunkIndex = startChunkIndex.coerceIn(0, (baseRequest.chunks.size - 1).coerceAtLeast(0)),
            startCharOffset = startCharOffset
        )
    }.getOrNull()
}

private fun JSONArray.toHostTtsChunks(fullText: String): List<BrainBoxTtsChunk> {
    if (fullText.isBlank()) {
        return emptyList()
    }

    return buildList {
        for (index in 0 until length()) {
            val chunk = optJSONObject(index) ?: continue
            val start = chunk.optInt("startCharIndex", -1)
            val end = chunk.optInt("endCharIndex", -1)
            if (start < 0 || end <= start || start >= fullText.length) {
                continue
            }

            val safeEnd = end.coerceIn(start + 1, fullText.length)
            val text = fullText.substring(start, safeEnd)
            add(
                BrainBoxTtsChunk(
                    id = chunk.optString("id").ifBlank { "chunk-$index" },
                    text = text,
                    startCharIndex = start,
                    endCharIndex = safeEnd
                )
            )
        }
    }
}
