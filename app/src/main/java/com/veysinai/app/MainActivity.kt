package com.veysinai.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(13, 18, 38)
        window.navigationBarColor = Color.rgb(13, 18, 38)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 48, 28, 28)
            setBackgroundColor(Color.rgb(13, 18, 38))
        }

        val title = TextView(this).apply {
            text = "Veysin AI"
            textSize = 34f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(title, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        val subtitle = TextView(this).apply {
            text = "Your all-in-one AI assistant"
            textSize = 16f
            setTextColor(Color.rgb(185, 195, 220))
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 32)
        }
        root.addView(subtitle)

        val features = listOf("AI Chat", "AI Image", "AI Video", "AI Voice", "Translator")
        features.forEach { feature ->
            val button = Button(this).apply {
                text = feature
                textSize = 17f
                isAllCaps = false
                setOnClickListener {
                    Toast.makeText(
                        this@MainActivity,
                        "$feature will be enabled after connecting its AI service.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            root.addView(button, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 12 })
        }

        val note = TextView(this).apply {
            text = "Starter version • AI services need API setup"
            textSize = 12f
            setTextColor(Color.rgb(145, 155, 180))
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }
        root.addView(note)
        setContentView(root)
    }
}
