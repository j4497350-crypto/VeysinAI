package com.veysinai.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.common.model.DownloadConditions
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private lateinit var input: EditText
    private lateinit var output: TextView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var authStatus: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
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
            setPadding(0, 8, 0, 18)
        }
        root.addView(subtitle)

        val authHeading = TextView(this).apply {
            text = "Account"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        }
        root.addView(authHeading)
        emailInput = EditText(this).apply {
            hint = "Email address"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(14, 10, 14, 10)
            setBackgroundColor(Color.rgb(35, 43, 67))
        }
        root.addView(emailInput, fullWidth())
        passwordInput = EditText(this).apply {
            hint = "Password (minimum 6 characters)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(14, 10, 14, 10)
            setBackgroundColor(Color.rgb(35, 43, 67))
        }
        root.addView(passwordInput, fullWidth())
        authStatus = TextView(this).apply {
            text = "Not signed in"
            textSize = 13f
            setTextColor(Color.rgb(185, 195, 220))
            setPadding(2, 6, 2, 8)
        }
        root.addView(authStatus)
        val signIn = Button(this).apply {
            text = "Sign in"
            isAllCaps = false
            setOnClickListener { signIn() }
        }
        root.addView(signIn, fullWidth())
        val createAccount = Button(this).apply {
            text = "Create account"
            isAllCaps = false
            setOnClickListener { createAccount() }
        }
        root.addView(createAccount, fullWidth())
        val signOut = Button(this).apply {
            text = "Sign out"
            isAllCaps = false
            setOnClickListener {
                auth.signOut()
                authStatus.text = "Signed out"
                Toast.makeText(this@MainActivity, "Signed out.", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(signOut, fullWidth())
        auth.currentUser?.let {
            authStatus.text = "Signed in as ${it.email ?: "user"}"
        }

        val section = TextView(this).apply {
            text = "English to Bengali Translator"
            textSize = 19f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 8)
        }
        root.addView(section)
        input = EditText(this).apply {
            hint = "Type English text to translate into Bengali"
            textSize = 16f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(14, 12, 14, 12)
            setBackgroundColor(Color.rgb(35, 43, 67))
        }
        root.addView(input, fullWidth())
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
                val spokenText = output.text.toString()
                if (spokenText.isNotBlank() && spokenText != "Translation will appear here.") {
                    tts?.language = Locale("bn", "IN")
                    tts?.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, "veysin-tts")
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
                        "$feature still needs a secure AI provider connection.",
                        Toast.LENGTH_LONG).show()
                }
            }
            root.addView(button, fullWidth())
        }
        val note = TextView(this).apply {
            text = "Translation runs on-device. Bengali voice uses Android Text-to-Speech. Account data is stored in Firebase when the project services are enabled."
            textSize = 12f
            setTextColor(Color.rgb(145, 155, 180))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }
        root.addView(note)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun signIn() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter your email and password.", Toast.LENGTH_SHORT).show()
            return
        }
        authStatus.text = "Signing in…"
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    authStatus.text = "Sign-in failed."
                    return@addOnSuccessListener
                }
                saveUserProfile(user.uid, email)
            }
            .addOnFailureListener { error ->
                authStatus.text = "Sign-in failed."
                Toast.makeText(this, error.localizedMessage ?: "Firebase sign-in failed.", Toast.LENGTH_LONG).show()
            }
    }

    private fun createAccount() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        if (email.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Enter a valid email and a password with at least 6 characters.", Toast.LENGTH_LONG).show()
            return
        }
        authStatus.text = "Creating account…"
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    authStatus.text = "Account creation failed."
                    return@addOnSuccessListener
                }
                saveUserProfile(user.uid, email)
            }
            .addOnFailureListener { error ->
                authStatus.text = "Account creation failed."
                Toast.makeText(this, error.localizedMessage ?: "Firebase account creation failed.", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveUserProfile(uid: String, email: String) {
        val userDoc = firestore.collection("users").document(uid)
        userDoc.get()
            .addOnSuccessListener { snapshot ->
                val profile = hashMapOf<String, Any>(
                    "email" to email,
                    "lastLoginAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                if (!snapshot.exists()) {
                    profile["createdAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
                }
                userDoc.set(profile, SetOptions.merge())
                    .addOnSuccessListener {
                        authStatus.text = "Signed in as $email • Firebase profile synced"
                        Toast.makeText(this, "Account connected to Firebase.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        authStatus.text = "Signed in, but profile sync failed."
                        Toast.makeText(this, error.localizedMessage ?: "Firestore write failed.", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { error ->
                authStatus.text = "Signed in, but database could not be read."
                Toast.makeText(this, error.localizedMessage ?: "Firestore read failed.", Toast.LENGTH_LONG).show()
            }
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
