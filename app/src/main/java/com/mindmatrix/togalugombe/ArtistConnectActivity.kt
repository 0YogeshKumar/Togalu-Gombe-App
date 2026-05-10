package com.mindmatrix.togalugombe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.random.Random

class ArtistConnectActivity : AppCompatActivity() {

    // --- PUT YOUR GMAIL SENDER CREDENTIALS HERE ---
    private val SENDER_EMAIL = "kannadagamer420@gmail.com"
    private val SENDER_APP_PASSWORD = "kqgxvalutkicqpok"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        val title = TextView(this).apply {
            text = "Local Artisans\nSelect an artisan to view their work."
            textSize = 20f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 48)
        }
        layout.addView(title)

        val artisans = listOf(
            Triple("Mankutimma (ಮಾಕುತಿಮ್ಮ)", "9632684117", "Books: Ramayana Complete Set\nPuppets: Hanuman, Sugriva"),
            Triple("Ravivarma (ರವಿವರ್ಮ)", "8310484960", "Books: Art of Leather Dyes\nPuppets: Rama, Sita, Lakshmana"),
            Triple("Jakanachari (ಜಕಣಾಚಾರಿ)", "9845861653", "Books: Puppet Crafting 101\nPuppets: Ravana, Indrajit")
        )

        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userRegisteredEmail = prefs.getString("EMAIL", "") ?: ""

        for (artisan in artisans) {
            val card = com.google.android.material.card.MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 32) }
                setCardBackgroundColor(android.graphics.Color.parseColor("#2A2A2A"))
                
                val innerText = TextView(context).apply {
                    text = artisan.first
                    textSize = 18f
                    setTextColor(android.graphics.Color.parseColor("#FFCA28"))
                    setPadding(32, 32, 32, 32)
                }
                addView(innerText)

                setOnClickListener {
                    AlertDialog.Builder(this@ArtistConnectActivity)
                        .setTitle(artisan.first)
                        .setMessage("${artisan.third}\n\nBook a workshop with this artisan for ₹500?")
                        .setPositiveButton("CONFIRM BOOKING") { _, _ ->
                            
                            Toast.makeText(this@ArtistConnectActivity, "Booking... Please wait.", Toast.LENGTH_LONG).show()

                            // 1. Send Notification to Status Bar
                            sendNotification(artisan.first, artisan.second)

                            // 2. SEND THE SILENT BACKGROUND EMAIL TO THE USER
                            sendSilentEmail(userRegisteredEmail, artisan.first, artisan.second)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
            layout.addView(card)
        }
        setContentView(layout)
    }

    private fun sendSilentEmail(userEmail: String, artisanName: String, artisanPhone: String) {
        if (userEmail.isEmpty()) return

        Thread {
            try {
                val props = Properties()
                props.put("mail.smtp.auth", "true")
                props.put("mail.smtp.starttls.enable", "true")
                props.put("mail.smtp.host", "smtp.gmail.com")
                props.put("mail.smtp.port", "587")

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD)
                    }
                })

                val message = MimeMessage(session)
                message.setFrom(InternetAddress(SENDER_EMAIL))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail))
                message.subject = "Workshop Confirmed: Togalu Gombe"
                message.setText("Hello,\n\nYour workshop with $artisanName is successfully booked!\n\nArtist Contact: $artisanPhone\nLocation: Nimmalakunta Village.\n\nThank you for supporting local artisans.\n- Togalu Gombe Team.")

                Transport.send(message) // Sends email silently in background

                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("Booking Successful!")
                        .setMessage("An email receipt has been automatically sent to $userEmail.")
                        .setPositiveButton("OK", null).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to send email. Check credentials.", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun sendNotification(artisanName: String, artisanPhone: String) {
        val channelId = "booking_receipts"
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Email Receipts", NotificationManager.IMPORTANCE_HIGH)
            notifManager.createNotificationChannel(channel)
        }
        
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Workshop Confirmed")
            .setContentText("Booked with $artisanName. Call $artisanPhone.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notifManager.notify(Random.nextInt(), notif)
    }
}