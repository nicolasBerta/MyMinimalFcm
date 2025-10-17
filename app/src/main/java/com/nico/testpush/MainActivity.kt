package com.nico.testpush

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var tokenText: TextView
    private lateinit var btnGetToken: Button
    private lateinit var btnCopy: Button

    private val askNotifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
        }
        fetchToken()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenText = findViewById(R.id.tokenText)
        btnGetToken = findViewById(R.id.btnGetToken)
        btnCopy = findViewById(R.id.btnCopy)

        btnGetToken.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                askNotifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                fetchToken()
            }
        }

        btnCopy.setOnClickListener {
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("fcmToken", tokenText.text))
            Toast.makeText(this, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchToken() {
        tokenText.text = "Obteniendo token…"
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { t ->
                tokenText.text = t
                Log.d("FCM", "Token: $t")
            }
            .addOnFailureListener { e ->
                tokenText.text = "Error: ${e.message}"
                Log.e("FCM", "Error token", e)
            }
    }
}
