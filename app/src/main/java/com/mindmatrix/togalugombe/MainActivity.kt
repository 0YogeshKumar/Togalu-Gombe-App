package com.mindmatrix.togalugombe

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        if (prefs.getString("EMAIL", null) == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
            setBackgroundColor(Color.parseColor("#0A0A0A"))
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "Togalu Gombe\nತೊಗಲು ಗೊಂಬೆಯಾಟ"
            textSize = 32f
            setTextColor(Color.parseColor("#FFCA28"))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 0, 0, 80)
        }
        layout.addView(title)

        // Removed the Scanner, kept the best features.
        val menuItems = listOf(
            "Live Assist (ದೃಶ್ಯಾವಳಿ)" to LiveAssistActivity::class.java,
            "Puppet Gallery (ಗ್ಯಾಲರಿ)" to GalleryActivity::class.java,
            "Artist Connect (ಸಂಪರ್ಕ)" to ArtistConnectActivity::class.java,
            "History Feed (ಇತಿಹಾಸ)" to HistoryFeedActivity::class.java
        )

        for ((text, activityClass) in menuItems) {
            val btn = MaterialButton(this).apply {
                this.text = text
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.parseColor("#FFCA28"))
                cornerRadius = 24 // Rounded, elegant corners
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 150
                ).apply { setMargins(0, 0, 0, 40) }
                
                setOnClickListener { startActivity(Intent(this@MainActivity, activityClass)) }
            }
            layout.addView(btn)
        }

        setContentView(layout)
        setupBackgroundMusic()
    }

    private fun setupBackgroundMusic() {
        exoPlayer = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true

        Handler(Looper.getMainLooper()).postDelayed({
            exoPlayer?.stop()
        }, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }
}