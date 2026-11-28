package com.example.lambda_bbb_events.view.ui.components.map

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.lambda_bbb_events.model.services.MAP_USER_AGENT
import org.json.JSONException
import org.json.JSONObject

/*
 * Shows the OpenStreetMap pages, which are built in OpenStreetMapPage.kt, in a WebView. Google
 * maps are drawn natively instead, in GoogleMapViews.kt.
 */

/**
 * Receives events from the map page's JavaScript. Calls arrive on a background thread and
 * are passed on to the main thread.
 */
private class MapBridge(
    private val onEvent: (name: String, data: JSONObject) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    /** Set on the main thread once the page is gone, after which its late events are ignored. */
    var isClosed = false

    /** Receives a named map event and its JSON payload from the WebView. */
    @JavascriptInterface
    fun post(name: String, json: String) {
        var data = JSONObject()
        try {
            data = JSONObject(json)
        } catch (e: JSONException) {
            // Send the event without data rather than dropping it
        }
        mainHandler.post {
            if (!isClosed) {
                onEvent(name, data)
            }
        }
    }
}

/** The bundled files that map pages can load, with their types. */
private val BUNDLED_FILES = mapOf(
    "leaflet/leaflet.js" to "text/javascript",
    "leaflet/leaflet.css" to "text/css"
)

/**
 * Loads Leaflet from the app's files, and opens links tapped on the map page in the phone's
 * browser instead of inside the map.
 */
private class MapPageClient(context: Context) : WebViewClient() {
    private val assets = context.applicationContext.assets

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        val path = url.removePrefix(OSM_PAGE_BASE_URL)
        val type = BUNDLED_FILES[path]
        if (path == url || type == null) {
            return null // not a bundled file, so it loads from the internet
        }
        return WebResourceResponse(type, "utf-8", assets.open(path))
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (!request.isForMainFrame) {
            return false
        }
        try {
            view.context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
        } catch (e: ActivityNotFoundException) {
            // No app can open the link, so ignore the tap
        }
        return true
    }
}

/**
 * JavaScript to run on a map page that is already open. Every instance is run once, even if an
 * earlier one had the same script.
 */
internal class MapCommand(val script: String)

/**
 * Displays a map page built with [osmPageHtml] and passes its events to [onEvent].
 *
 * Drags on the map pan the map instead of scrolling the screen it is in.
 *
 * @param html The map page.
 * @param modifier Sets the size and shape of the map.
 * @param command Run on the page when it changes. The page must have finished loading.
 * @param onEvent Called on the main thread with each event the page sends.
 */
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
internal fun MapsJavaScriptView(
    html: String,
    modifier: Modifier = Modifier,
    command: MapCommand? = null,
    onEvent: (name: String, data: JSONObject) -> Unit = { _, _ -> }
) {
    // The page is only created once, so it must always call the newest onEvent
    val currentOnEvent by rememberUpdatedState(onEvent)
    val bridge = remember { MapBridge { name, data -> currentOnEvent(name, data) } }
    var shownWebView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(shownWebView, command) {
        if (command != null) {
            shownWebView?.evaluateJavascript(command.script, null)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val webView = WebView(context)

            // A WebView with wrap-content height gives its page a 0px tall viewport,
            // which collapses the map; Compose still sizes it from the modifier
            webView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.addJavascriptInterface(bridge, "Android")

            webView.settings.userAgentString = MAP_USER_AGENT

            // Links such as OpenStreetMap's credit open in the browser instead of replacing the map
            webView.webViewClient = MapPageClient(context)

            // Keep scrolling parents from taking over drags meant for the map
            webView.setOnTouchListener { view, _ ->
                view.parent?.requestDisallowInterceptTouchEvent(true)
                false
            }

            webView.tag = html
            webView.loadDataWithBaseURL(OSM_PAGE_BASE_URL, html, "text/html", "utf-8", null)
            shownWebView = webView
            webView
        },
        update = { webView ->
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL(OSM_PAGE_BASE_URL, html, "text/html", "utf-8", null)
            }
        },
        onRelease = { webView ->
            bridge.isClosed = true
            shownWebView = null
            webView.destroy()
        }
    )
}
