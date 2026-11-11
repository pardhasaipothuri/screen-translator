package com.manga.autotranslator

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.TextView

class MangaTranslatorService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private val activeOverlayViews = mutableListOf<View>()
    private val debounceHandler = Handler(Looper.getMainLooper())
    private var scrollDebouncingRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    // Automatically triggers when user scrolls inside any Manga App or Browser
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
            event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            // Debounce scrolling: Waits 350ms after user stops scrolling before scanning
            scrollDebouncingRunnable?.let { debounceHandler.removeCallbacks(it) }
            scrollDebouncingRunnable = Runnable {
                scanAndTranslateScreen()
            }
            debounceHandler.postDelayed(scrollDebouncingRunnable!!, 350)
        }
    }

    private fun scanAndTranslateScreen() {
        clearPreviousOverlays()
        val rootNode = rootInActiveWindow ?: return

        // Scan accessibility node tree for text content
        extractTextNodes(rootNode) { text, bounds ->
            MLKitOCRManager.translateJapaneseToEnglish(text) { translatedText ->
                renderFloatingBubble(translatedText, bounds)
            }
        }
    }

    private fun extractTextNodes(node: AccessibilityNodeInfo, onTextFound: (String, Rect) -> Unit) {
        if (!node.text.isNullOrEmpty()) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            onTextFound(node.text.toString(), bounds)
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { extractTextNodes(it, onTextFound) }
        }
    }

    // Creates floating transparent text bubble over original speech bubble coordinates
    private fun renderFloatingBubble(text: String, bounds: Rect) {
        val overlayView = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#EE312E81")) // Indigo dark background
            setPadding(12, 8, 12, 8)
        }

        val textView = TextView(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 12f
        }
        overlayView.addView(textView)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bounds.left
            y = bounds.top
        }

        try {
            windowManager.addView(overlayView, params)
            activeOverlayViews.add(overlayView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearPreviousOverlays() {
        for (view in activeOverlayViews) {
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                // Ignore if view was already detached
            }
        }
        activeOverlayViews.clear()
    }

    override fun onInterrupt() {}
}