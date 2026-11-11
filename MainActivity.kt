package com.manga.autotranslator

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        
        // Simple launcher interface for granting permissions
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(64, 64, 64, 64)
            gravity = android.view.Gravity.CENTER
        }

        val btnOverlay = Button(this).apply {
            text = "1. Grant Overlay Permission"
            setOnClickListener { requestOverlayPermission() }
        }

        val btnAccessibility = Button(this).apply {
            text = "2. Enable Screen Accessibility Service"
            setOnClickListener { openAccessibilitySettings() }
        }

        layout.addView(btnOverlay)
        layout.addView(btnAccessibility)
        setContentView(layout)
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            Toast.makeText(this, "Overlay Permission Already Granted!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "Enable 'Manga Auto Translator' in Services list", Toast.LENGTH_LONG).show()
    }
}