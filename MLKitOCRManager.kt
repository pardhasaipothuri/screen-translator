package com.manga.autotranslator

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

object MLKitOCRManager {

    // Setup 100% Free On-Device Japanese to English Translation Model
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.JAPANESE)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()

    private val japaneseTranslator = Translation.getClient(options)
    private var isModelDownloaded = false

    init {
        // Download translation model locally on first launch
        japaneseTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                isModelDownloaded = true
            }
    }

    fun translateJapaneseToEnglish(sourceText: String, onComplete: (String) -> Unit) {
        if (!isModelDownloaded) {
            // Fallback download trigger if model wasn't cached yet
            japaneseTranslator.downloadModelIfNeeded().addOnSuccessListener {
                isModelDownloaded = true
                performTranslation(sourceText, onComplete)
            }
        } else {
            performTranslation(sourceText, onComplete)
        }
    }

    private fun performTranslation(text: String, onComplete: (String) -> Unit) {
        japaneseTranslator.translate(text)
            .addOnSuccessListener { translatedResult ->
                onComplete(translatedResult)
            }
            .addOnFailureListener {
                onComplete(text) // Return original text if translation fails
            }
    }
}