package com.mindmatrix.togalugombe

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.URLEncoder

class WebSearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_search)

        val webView = findViewById<WebView>(R.id.webView)
        val tvTitle = findViewById<TextView>(R.id.tvWebSearchTitle)

        // Get what the user typed in the gallery search bar
        val userQuery = intent.getStringExtra("SEARCH_QUERY") ?: ""
        tvTitle.text = "Searching Web: $userQuery"

        // Magic Formulation: "puppet image of[user query]"
        val customSearch = "puppet image of $userQuery"
        
        // Encode for URL (changes spaces to %20 so Google understands it)
        val encodedQuery = URLEncoder.encode(customSearch, "UTF-8")
        
        // Google Image Search URL (tbm=isch forces the Image tab)
        val url = "https://www.google.com/search?tbm=isch&q=$encodedQuery"

        // Configure WebView to act like a real browser
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient() // Keeps clicks inside the app
        webView.webChromeClient = WebChromeClient()

        webView.loadUrl(url)
    }

    // Allows the user to hit the "Back" button to go back to the previous webpage, 
    // instead of instantly closing the whole activity.
    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webView)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}