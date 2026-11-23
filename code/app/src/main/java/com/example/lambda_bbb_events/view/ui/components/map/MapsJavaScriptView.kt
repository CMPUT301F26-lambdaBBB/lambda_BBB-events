package com.example.lambda_bbb_events.view.ui.components.map

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.lambda_bbb_events.BuildConfig
import org.json.JSONException
import org.json.JSONObject

/*
 * Shows Google Maps through the Maps JavaScript API in a WebView.
 *
 * The app's Maps key is a Maps Demo Key, which supports the Maps JavaScript API but not
 * the native Maps SDK for Android, so maps are drawn by a small web page instead.
 */

/** Base URL the map pages are loaded under, so the Maps JavaScript API sees a stable origin. */
private const val MAP_PAGE_BASE_URL = "https://lambda-camping.app/"

/**
 * Receives events from the map page's JavaScript. Calls arrive on a background thread and
 * are passed on to the main thread.
 */
private class MapBridge(
    private val onEvent: (name: String, data: JSONObject) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun post(name: String, json: String) {
        var data = JSONObject()
        try {
            data = JSONObject(json)
        } catch (e: JSONException) {
            // Send the event without data rather than dropping it
        }
        mainHandler.post { onEvent(name, data) }
    }
}

/**
 * Opens links tapped on the map page in the phone's browser instead of inside the map.
 */
private class OpenLinksInBrowser : WebViewClient() {
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
 * Builds a map page that loads the Maps JavaScript API and then runs [script].
 *
 * [script] must define an `async function initMap()`. It can send events to the app with
 * `send(name, data)`. If Google rejects the key, the page shows a message instead of a map.
 *
 * @param script JavaScript that creates the map.
 */
internal fun mapPageHtml(script: String): String = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="initial-scale=1, width=device-width, user-scalable=no">
<style>
  html, body { height: 100%; margin: 0; padding: 0; }
  /* Pinned to the page edges, so the map always fills the WebView */
  #map { position: absolute; top: 0; right: 0; bottom: 0; left: 0; }
  #error { display: none; position: absolute; top: 0; right: 0; bottom: 0; left: 0; align-items: center; justify-content: center;
           font: 14px sans-serif; color: #555; background: #e9edf1; text-align: center; padding: 16px; box-sizing: border-box; }
  .info { font: 14px sans-serif; max-width: 220px; }
  .info b { display: block; margin-bottom: 2px; }
  .info a { color: #2c5c7c; font-weight: 600; text-decoration: none; display: inline-block; margin-top: 6px; }
</style>
</head>
<body>
<div id="map"></div>
<div id="error">Map unavailable. Check the Maps API key.</div>
<script>
  function send(name, data) { Android.post(name, JSON.stringify(data || {})); }
  window.gm_authFailure = function () {
    document.getElementById('map').style.display = 'none';
    document.getElementById('error').style.display = 'flex';
    send('authFailure');
  };
  $script
</script>
<script async src="https://maps.googleapis.com/maps/api/js?key=${BuildConfig.MAPS_API_KEY}&loading=async&callback=initMap"></script>
</body>
</html>
""".trimIndent()

/**
 * Displays a map page built with [mapPageHtml] and passes its events to [onEvent].
 *
 * Drags on the map pan the map instead of scrolling the screen it is in.
 *
 * @param html The map page.
 * @param modifier Sets the size and shape of the map.
 * @param onEvent Called on the main thread with each event the page sends.
 */
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
internal fun MapsJavaScriptView(
    html: String,
    modifier: Modifier = Modifier,
    onEvent: (name: String, data: JSONObject) -> Unit = { _, _ -> }
) {
    // The page is only created once, so it must always call the newest onEvent
    val currentOnEvent by rememberUpdatedState(onEvent)

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
            webView.addJavascriptInterface(MapBridge { name, data -> currentOnEvent(name, data) }, "Android")

            // Links such as Google's terms open in the browser instead of replacing the map
            webView.webViewClient = OpenLinksInBrowser()

            // Keep scrolling parents from taking over drags meant for the map
            webView.setOnTouchListener { view, _ ->
                view.parent?.requestDisallowInterceptTouchEvent(true)
                false
            }

            webView.tag = html
            webView.loadDataWithBaseURL(MAP_PAGE_BASE_URL, html, "text/html", "utf-8", null)
            webView
        },
        update = { webView ->
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL(MAP_PAGE_BASE_URL, html, "text/html", "utf-8", null)
            }
        },
        onRelease = { webView -> webView.destroy() }
    )
}
