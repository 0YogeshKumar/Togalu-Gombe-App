package com.mindmatrix.togalugombe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistoryFeedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        val title = TextView(this).apply {
            text = "History & Lore (YouTube)\nTap a video to watch."
            textSize = 20f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 48)
        }
        layout.addView(title)

        val videos = listOf(
            "Ramayan Puppet Show (English)" to "https://www.youtube.com/watch?v=DBEWNVxYhiE",
            "The Legend of Prince Rama (1993)" to "https://www.youtube.com/watch?v=gKcOjnDJfzk",
            "Why is Diwali the Festival of Light?" to "https://www.youtube.com/watch?v=cvCv7kTcRXI",
            "Sundara Kanda - Hanuman Search" to "https://www.youtube.com/watch?v=LKzWGc0kgug"
        )

        for (video in videos) {
            val btn = com.google.android.material.button.MaterialButton(this).apply {
                text = video.first
                setTextColor(android.graphics.Color.BLACK)
                setBackgroundColor(android.graphics.Color.parseColor("#FFCA28"))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 24) }
                
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.second))
                    startActivity(intent)
                }
            }
            layout.addView(btn)
        }
        setContentView(layout)
    }
}