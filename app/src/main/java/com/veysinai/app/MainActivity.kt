package com.veysinai.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.common.model.DownloadConditions
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private lateinit var input: EditText
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        window.statusBarColor = Color.rgb(13, 18, 38)
        window.navigationBarColor = Color.rgb(13, 18, 38)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 36, 24, 24)
            setBackgroundColor(Color.rgb(13, 18, 38))
        }
        val title = TextView(this).apply {
            text = "Veysin AI"
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(title)
        val subtitle = TextView(this).apply {
            text = "Your all-in-one AI assistant"
            textSize = 15f
            setTextColor(Color.rgb(185, 195, 220))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 22)
        }
        root.addView(subtitle)

        input = EditText(this).apply {
            hint = "Type English text to translate into Bengali"
            textSize = 16f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(14, 12, 14, 12)
            setBackgroundColor(Color.rgb(35, 43, 67))
        }
        root.addView(input, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        val translate = Button(this).apply {
            text = "Translate English → Bengali"
            isAllCaps = false
            setOnClickListener { translateText() }
        }
        root.addView(translate, fullWidth())
        output = TextView(this).apply {
            text = "Translation will appear here."
            textSize = 17f
            setTextColor(Color.WHITE)
            setPadding(4, 18, 4, 18)
        }
        root.addView(output)

        val speak = Button(this).apply {
            text = "🔊 Read Bengali aloud"
            isAllCaps = false
            setOnClickListener {
                val text = output.text.toString()
                if (text.isNotBlank() && text != "Translation will appear here.") {
                    tts?.language = Locale("bn", "IN")
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "veysin-tts")
                } else {
                    Toast.makeText(this@MainActivity, "Translate some text first.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        root.addView(speak, fullWidth())

        listOf("AI Chat", "AI Image", "AI Video").forEach { feature ->
            val button = Button(this).apply {
                text = feature
                isAllCaps = false
                setOnClickListener {
                    Toast.makeText(this@MainActivity,
                        "$feature needs a provider API key or free-tier account. It is not connected yet.",
                        Toast.LENGTH_LONG).show()
                }
            }
            root.addView(button, fullWidth())
        }
        val note = TextView(this).apply {
            text = "Translator uses on-device language models. Voice reading uses Android Text-to-Speech."
            textSize = 12f
            setTextColor(Color.rgb(145, 155, 180))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }
        root.addView(note)
        setContentView(root)
    }

    private fun fullWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = 8 }

    private fun translateText() {
        val source = input.text.toString().trim()
        if (source.isEmpty()) {
            Toast.makeText(this, "Please enter English text.", Toast.LENGTH_SHORT).show()
            return
        }
        output.text = "Preparing free Bengali translation model…"
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.BENGALI)
            .build()
        val translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(source)
                    .addOnSuccessListener { translated ->
                        output.text = translated
                        translator.close()
                    }
                    .addOnFailureListener { error ->
                        output.text = "Translation failed: ${error.localizedMessage}"
                        translator.close()
                    }
            }
            .addOnFailureListener { error ->
                output.text = "Model download failed: ${error.localizedMessage}"
                translator.close()
            }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("bn", "IN")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
