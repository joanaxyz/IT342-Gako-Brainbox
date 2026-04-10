package edu.cit.gako.brainbox.notebook

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.print.PrintManager
import android.util.Base64
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.webkit.WebViewAssetLoader
import edu.cit.gako.brainbox.BuildConfig
import edu.cit.gako.brainbox.app.NotebookEditorHostStatus
import edu.cit.gako.brainbox.network.RetrofitClient
import edu.cit.gako.brainbox.network.SessionManager
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink2
import edu.cit.gako.brainbox.ui.theme.White
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

private const val EditorAssetDomain = "appassets.androidplatform.net"
private const val EditorAssetOrigin = "http://$EditorAssetDomain"
private const val EditorEntryUrl = "$EditorAssetOrigin/mobile-editor.html"
private const val EditorStartupTimeoutMs = 15_000L
private const val BrainBoxHostInterfaceName = "BrainBoxHostNative"
private const val PendingPdfExportScript =
    "window.__BRAINBOX_PENDING_PDF_EXPORT__ ? JSON.stringify(window.__BRAINBOX_PENDING_PDF_EXPORT__) : null"

@Composable
internal fun NotebookEditorHostScreen(
    notebookUuid: String,
    status: NotebookEditorHostStatus,
    onClose: () -> Unit,
    onLoadingStarted: () -> Unit,
    onReady: () -> Unit,
    onError: (String) -> Unit,
    onSessionCleared: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) {
        checkNotNull(context.findActivity()) { "NotebookEditorHostScreen requires an Activity context." }
    }
    val sessionManager = remember(context.applicationContext) {
        SessionManager(context.applicationContext)
    }
    val assetLoader = remember(context.applicationContext) {
        WebViewAssetLoader.Builder()
            .setDomain(EditorAssetDomain)
            .setHttpAllowed(true)
            .addPathHandler("/", WebViewAssetLoader.AssetsPathHandler(context.applicationContext))
            .build()
    }
    val editorUrl = remember(notebookUuid) { buildEditorUrl(notebookUuid) }
    val currentOnClose by rememberUpdatedState(onClose)
    val currentOnLoadingStarted by rememberUpdatedState(onLoadingStarted)
    val currentOnReady by rememberUpdatedState(onReady)
    val currentOnError by rememberUpdatedState(onError)
    val currentOnSessionCleared by rememberUpdatedState(onSessionCleared)

    var webView by remember { mutableStateOf<WebView?>(null) }
    var fileChooserCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = fileChooserCallback
        fileChooserCallback = null
        callback?.onReceiveValue(
            WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        )
    }

    BackHandler {
        val browser = webView
        if (browser?.canGoBack() == true) {
            browser.goBack()
        } else {
            currentOnClose()
        }
    }

    LaunchedEffect(notebookUuid, status.isLoading) {
        if (status.isLoading) {
            delay(EditorStartupTimeoutMs)
            if (status.isLoading) {
                currentOnError("The notebook editor is taking too long to load. Check the embedded web assets or backend connection and try again.")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fileChooserCallback?.onReceiveValue(null)
            fileChooserCallback = null
            webView?.apply {
                stopLoading()
                webChromeClient = null
                removeJavascriptInterface(BrainBoxHostInterfaceName)
                destroy()
            }
            webView = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

                WebView(viewContext).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.mediaPlaybackRequiresUserGesture = false

                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    addJavascriptInterface(
                        NotebookEditorHostBridge(
                            activity = activity,
                            sessionManager = sessionManager,
                            assetLoader = assetLoader,
                            webViewProvider = { webView },
                            onClose = { currentOnClose() },
                            onReady = { currentOnReady() },
                            onError = { message -> currentOnError(message) },
                            onSessionCleared = { currentOnSessionCleared() }
                        ),
                        BrainBoxHostInterfaceName
                    )

                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            val callback = filePathCallback ?: return false
                            val params = fileChooserParams ?: return false

                            fileChooserCallback?.onReceiveValue(null)
                            fileChooserCallback = callback

                            return try {
                                fileChooserLauncher.launch(params.createIntent())
                                true
                            } catch (_: ActivityNotFoundException) {
                                fileChooserCallback = null
                                callback.onReceiveValue(null)
                                currentOnError("No compatible file picker is available on this device.")
                                false
                            }
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest
                        ): WebResourceResponse? {
                            return assetLoader.shouldInterceptRequest(request.url)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            currentOnLoadingStarted()
                            super.onPageStarted(view, url, favicon)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest,
                            error: android.webkit.WebResourceError
                        ) {
                            if (request.isForMainFrame) {
                                currentOnError(
                                    error.description?.toString()?.ifBlank {
                                        "The embedded notebook editor failed to load."
                                    } ?: "The embedded notebook editor failed to load."
                                )
                            }
                            super.onReceivedError(view, request, error)
                        }

                        override fun onReceivedHttpError(
                            view: WebView?,
                            request: WebResourceRequest,
                            errorResponse: android.webkit.WebResourceResponse
                        ) {
                            if (request.isForMainFrame && errorResponse.statusCode >= 400) {
                                currentOnError(
                                    "The notebook editor returned HTTP ${errorResponse.statusCode}. Check the embedded build and backend availability."
                                )
                            }
                            super.onReceivedHttpError(view, request, errorResponse)
                        }

                        override fun onRenderProcessGone(
                            view: WebView?,
                            detail: android.webkit.RenderProcessGoneDetail?
                        ): Boolean {
                            currentOnError("The notebook editor renderer stopped unexpectedly. Try reopening the notebook.")
                            return true
                        }
                    }

                    syncEditorCookies(sessionManager)
                    currentOnLoadingStarted()
                    loadUrl(editorUrl)
                    webView = this
                }
            },
            update = { browser ->
                webView = browser
            }
        )

        if (status.isLoading) {
            LoadingOverlay()
        }

        status.errorMessage?.let { message ->
            ErrorOverlay(
                message = message,
                onRetry = {
                    syncEditorCookies(sessionManager)
                    currentOnLoadingStarted()
                    webView?.loadUrl(editorUrl)
                        ?: currentOnError("The notebook editor view is unavailable. Close and reopen the notebook to try again.")
                },
                onClose = currentOnClose
            )
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream.copy(alpha = 0.86f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = White,
            tonalElevation = 2.dp,
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator(color = Accent)
                Text(
                    text = "Loading notebook editor",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
                Text(
                    text = "Preparing the embedded web editor and syncing your session.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ErrorOverlay(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            color = White,
            tonalElevation = 2.dp,
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Notebook editor unavailable",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink2,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = White
                    )
                ) {
                    Text("Retry")
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onClose,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = AccentBg,
                        contentColor = Ink
                    )
                ) {
                    Text("Close editor")
                }
            }
        }
    }
}

private class NotebookEditorHostBridge(
    private val activity: Activity,
    private val sessionManager: SessionManager,
    private val assetLoader: WebViewAssetLoader,
    private val webViewProvider: () -> WebView?,
    private val onClose: () -> Unit,
    private val onReady: () -> Unit,
    private val onError: (String) -> Unit,
    private val onSessionCleared: () -> Unit
) {
    @JavascriptInterface
    fun closeEditor() {
        activity.runOnUiThread(onClose)
    }

    @JavascriptInterface
    fun persistSession(accessToken: String, refreshToken: String) {
        if (accessToken.isNotBlank()) {
            sessionManager.saveAuthToken(accessToken)
        }
        if (refreshToken.isNotBlank()) {
            sessionManager.saveRefreshToken(refreshToken)
        }
        activity.runOnUiThread {
            syncEditorCookies(sessionManager)
        }
    }

    @JavascriptInterface
    fun clearSession() {
        sessionManager.clearSession()
        syncEditorCookies(sessionManager)
        activity.runOnUiThread(onSessionCleared)
    }

    @JavascriptInterface
    fun exportFile(payloadJson: String) {
        activity.runOnUiThread {
            runBridgeCall {
                val payload = JSONObject(payloadJson)
                val filename = payload.optString("filename").ifBlank { "BrainBox export" }
                val mimeType = payload.optString("mimeType").ifBlank { "application/octet-stream" }
                val base64 = payload.optString("base64")
                if (base64.isBlank()) {
                    error("Export payload was missing file data.")
                }
                activity.shareExportFile(filename, mimeType, base64)
            }
        }
    }

    @JavascriptInterface
    fun requestPdfExport() {
        activity.runOnUiThread {
            val sourceWebView = webViewProvider()
            if (sourceWebView == null) {
                onError("The notebook editor view is unavailable for PDF export.")
                return@runOnUiThread
            }
            activity.requestPdfExport(sourceWebView, assetLoader)
        }
    }

    @JavascriptInterface
    fun reportReady() {
        activity.runOnUiThread(onReady)
    }

    @JavascriptInterface
    fun reportError(message: String) {
        activity.runOnUiThread {
            onError(message.ifBlank { "The embedded notebook editor reported an unknown error." })
        }
    }

    private fun runBridgeCall(action: () -> Unit) {
        try {
            action()
        } catch (error: Exception) {
            activity.runOnUiThread {
                onError(error.message ?: "The Android host couldn't complete the editor request.")
            }
        }
    }
}

private fun buildEditorUrl(notebookUuid: String): String {
    val encodedNotebookId = URLEncoder.encode(notebookUuid, StandardCharsets.UTF_8.toString())
    val encodedApiBaseUrl = URLEncoder.encode(RetrofitClient.apiBaseUrl, StandardCharsets.UTF_8.toString())
    return "$EditorEntryUrl?host=android&notebookId=$encodedNotebookId&apiBaseUrl=$encodedApiBaseUrl"
}

private fun syncEditorCookies(sessionManager: SessionManager) {
    val cookieManager = CookieManager.getInstance()
    cookieManager.setCookie(EditorAssetOrigin, buildCookie("accessToken", sessionManager.fetchAuthToken()))
    cookieManager.setCookie(EditorAssetOrigin, buildCookie("refreshToken", sessionManager.fetchRefreshToken()))
    cookieManager.flush()
}

private fun buildCookie(name: String, value: String?): String {
    return if (value.isNullOrBlank()) {
        "$name=; Max-Age=0; Path=/; SameSite=Lax"
    } else {
        "$name=$value; Path=/; SameSite=Lax"
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Activity.shareExportFile(filename: String, mimeType: String, base64: String) {
    val targetDirectory = (externalCacheDir ?: cacheDir).resolve("exports").apply { mkdirs() }
    val targetFile = File(targetDirectory, sanitizeExportFilename(filename))
    targetFile.writeBytes(Base64.decode(base64, Base64.DEFAULT))

    val uri = FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        targetFile
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivity(Intent.createChooser(shareIntent, "Share export"))
}

private fun sanitizeExportFilename(filename: String): String {
    val cleaned = filename
        .replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .trimEnd('.', ' ')

    return cleaned.ifBlank { "brainbox-export" }
}

private fun Activity.requestPdfExport(
    sourceWebView: WebView,
    assetLoader: WebViewAssetLoader
) {
    sourceWebView.evaluateJavascript(PendingPdfExportScript) { rawValue ->
        val payloadJson = decodeJavascriptString(rawValue)
        if (payloadJson.isNullOrBlank()) {
            printCurrentWebView(sourceWebView, "BrainBox notebook")
            return@evaluateJavascript
        }

        runCatching {
            val payload = JSONObject(payloadJson)
            val title = payload.optString("title").ifBlank { "BrainBox notebook" }
            val printableHtml = payload.optString("printableHtml")
            sourceWebView.evaluateJavascript("delete window.__BRAINBOX_PENDING_PDF_EXPORT__;", null)

            if (printableHtml.isBlank()) {
                printCurrentWebView(sourceWebView, title)
            } else {
                printHtmlDocument(title, printableHtml, assetLoader)
            }
        }.getOrElse {
            printCurrentWebView(sourceWebView, "BrainBox notebook")
        }
    }
}

private fun Activity.printCurrentWebView(sourceWebView: WebView, title: String) {
    val printManager = getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
    val printAdapter = sourceWebView.createPrintDocumentAdapter(title)
    printManager.print(title, printAdapter, null)
}

private fun Activity.printHtmlDocument(
    title: String,
    printableHtml: String,
    assetLoader: WebViewAssetLoader
) {
    val previewWebView = WebView(this).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
                val printAdapter = this@apply.createPrintDocumentAdapter(title)
                printManager.print(title, printAdapter, null)
            }
        }
    }

    previewWebView.loadDataWithBaseURL(
        EditorEntryUrl,
        printableHtml,
        "text/html",
        "utf-8",
        null
    )
}

private fun decodeJavascriptString(value: String?): String? {
    val rawValue = value?.trim().orEmpty()
    if (rawValue.isBlank() || rawValue == "null") {
        return null
    }

    return JSONArray("[$rawValue]").getString(0)
}
