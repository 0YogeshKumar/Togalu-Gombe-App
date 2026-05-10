package com.mindmatrix.togalugombe

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    // --- PUT YOUR GROQ API KEY HERE ---
    private val GROQ_API_KEY = "YOUR GROQ API KEY HERE"
    
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var puppetList: List<Puppet>
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadPuppetData()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }

        viewFinder = PreviewView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        layout.addView(viewFinder)

        val captureBtn = Button(this).apply {
            text = "CAPTURE & IDENTIFY PUPPET"
            setBackgroundColor(Color.parseColor("#FFCA28"))
            setTextColor(Color.BLACK)
            setPadding(32, 48, 32, 48)
            setOnClickListener {
                text = "AI IS ANALYZING..."
                isEnabled = false
                takePhotoAndAnalyze(this)
            }
        }
        layout.addView(captureBtn)
        setContentView(layout)

        cameraExecutor = Executors.newSingleThreadExecutor()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }
    }

    private fun loadPuppetData() {
        val jsonString = applicationContext.assets.open("puppets.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Puppet>>() {}.type
        puppetList = Gson().fromJson(jsonString, listType)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(viewFinder.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (exc: Exception) { }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhotoAndAnalyze(btn: Button) {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val capturedBitmap = image.toBitmap()
                image.close()

                Thread {
                    callGroqVisionApi(capturedBitmap, btn)
                }.start()
            }

            override fun onError(exception: ImageCaptureException) {
                runOnUiThread { btn.text = "CAPTURE & IDENTIFY PUPPET"; btn.isEnabled = true }
            }
        })
    }

    private fun callGroqVisionApi(bitmap: Bitmap, btn: Button) {
        var errorDetails = "Unknown Network Error"
        try {
            val maxDim = 600f
            val scale = Math.min(maxDim / bitmap.width, maxDim / bitmap.height)
            val scaledWidth = Math.round(bitmap.width * scale)
            val scaledHeight = Math.round(bitmap.height * scale)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
            
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            // STRICT FEATURE EXTRACTION PROMPT
            val jsonPayload = """
            {
              "model": "meta-llama/llama-4-scout-17b-16e-instruct",
              "messages":[
                {
                  "role": "user",
                  "content":[
                    {
                      "type": "text",
                      "text": "Analyze this traditional Indian leather puppet carefully. Look strictly at the number of heads, skin color, and facial features. Rules:\n1. If the puppet has EXACTLY 10 HEADS in a horizontal row, it is 'Ravana'.\n2. If it has 1 head, BLUE skin, and holds a bow, it is 'Rama'.\n3. If it has 1 head, a MONKEY face, and is mostly ORANGE/RED, it is 'Hanuman'.\n4. If it has a GREEN monkey face, it is 'Angada'.\n5. If it is a BIRD, it is 'Jatayu'.\n6. If it is a WOMAN in a saree, it is 'Sita'.\nChoose exactly ONE word from this list:[Rama, Sita, Lakshmana, Dasharatha, Vishwamitra, Vasishta, Valmiki, Narada, Agastya, Hanuman, Sugriva, Vali, Angada, Jambavan, Ravana, Kumbhakarna, Indrajit, Surpanakha, Vibhishana, Jatayu, Sampati, Shabari, Ahalya]. Output the single word ONLY. No punctuation."
                    },
                    {
                      "type": "image_url",
                      "image_url": {
                        "url": "data:image/jpeg;base64,$base64Image"
                      }
                    }
                  ]
                }
              ],
              "temperature": 0.0
            }
            """.trimIndent()

            val url = URL("https://api.groq.com/openai/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $GROQ_API_KEY")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.doOutput = true

            val os = connection.outputStream
            os.write(jsonPayload.toByteArray(Charsets.UTF_8))
            os.flush()
            os.close()

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseString)
                val aiReply = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
                
                runOnUiThread { matchResponseToPuppet(aiReply, btn) }
            } else {
                errorDetails = "HTTP $responseCode: " + (connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No details")
                showError(btn, errorDetails)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError(btn, e.message ?: "Connection Exception. Please check internet.")
        }
    }

    private fun matchResponseToPuppet(aiReply: String, btn: Button) {
        btn.text = "CAPTURE & IDENTIFY PUPPET"
        btn.isEnabled = true

        val matchedPuppet = puppetList.find { aiReply.contains(it.name_en, ignoreCase = true) }

        if (matchedPuppet != null) {
            val imageView = ImageView(this).apply {
                val resId = resources.getIdentifier(matchedPuppet.local_image, "drawable", packageName)
                if (resId != 0) setImageResource(resId)
                
                // UI FIX: Constrain the image height so the 'Awesome' button is never pushed off-screen!
                adjustViewBounds = true
                maxHeight = 600 
                setPadding(0, 32, 0, 0)
            }

            AlertDialog.Builder(this)
                .setTitle("AI Match Found!")
                .setMessage("Name: ${matchedPuppet.name_kn} (${matchedPuppet.name_en})\nPowers: ${matchedPuppet.powers}")
                .setView(imageView)
                .setPositiveButton("Awesome", null).show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("No Match Found")
                .setMessage("AI Reply: '$aiReply'\n\nSorry for the inconvenience, there has been some issue in finding the exact match.")
                .setPositiveButton("Try Again", null).show()
        }
    }

    private fun showError(btn: Button, details: String) {
        runOnUiThread {
            btn.text = "CAPTURE & IDENTIFY PUPPET"
            btn.isEnabled = true
            AlertDialog.Builder(this)
                .setTitle("API Error")
                .setMessage("Could not connect to Groq Vision AI.\n\nDetails: $details")
                .setPositiveButton("OK", null).show()
        }
    }

    override fun onDestroy() { super.onDestroy(); cameraExecutor.shutdown() }
}