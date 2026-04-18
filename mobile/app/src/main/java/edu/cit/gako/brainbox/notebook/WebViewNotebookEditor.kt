package edu.cit.gako.brainbox.notebook

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import edu.cit.gako.brainbox.data.NotebookMutationResult
import edu.cit.gako.brainbox.network.RetrofitClient
import edu.cit.gako.brainbox.ui.theme.Cream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                appGraph.repository.createNotebook(
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
    val appGraph = remember(context.applicationContext) {
        BrainBoxAppGraph.from(context.applicationContext)
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
                            onOpenFlashcardDeck = onOpenFlashcardDeck
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
    private val onOpenFlashcardDeck: (String) -> Unit
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

    @JavascriptInterface
    fun exportFile(_json: String) = Unit

    @JavascriptInterface
    fun requestPdfExport() = Unit
}
