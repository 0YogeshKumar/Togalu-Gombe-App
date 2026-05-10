package com.mindmatrix.togalugombe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val title = findViewById<TextView>(R.id.tvSplashTitle)
        val sub = findViewById<TextView>(R.id.tvSplashSub)

        // Fade-in animation
        title.animate().alpha(1f).setDuration(1000).start()
        sub.animate().alpha(1f).setDuration(1500).start()

        // Wait 2.5 seconds, then go to Login (or Main if already logged in)
        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            if (prefs.getString("EMAIL", null) == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 2500)
    }
}