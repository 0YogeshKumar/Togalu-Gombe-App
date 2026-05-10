package com.mindmatrix.togalugombe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (email.isNotEmpty() && phone.isNotEmpty()) {
                val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                prefs.edit().putString("EMAIL", email).putString("PHONE", phone).apply()
                
                startActivity(Intent(this, MainActivity::class.java))
                finish() // Close login screen
            } else {
                Toast.makeText(this, "Please enter both Email and Phone", Toast.LENGTH_SHORT).show()
            }
        }
    }
}