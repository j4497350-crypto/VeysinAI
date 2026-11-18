package com.veysinai.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.text.InputType

class MainActivity : Activity() {
    private lateinit var link: EditText
    private lateinit var topic: EditText
    private lateinit var result: TextView
    private lateinit var views: EditText
    private lateinit var followers: EditText
    private val prefs by lazy { getSharedPreferences("promotion_progress", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 36, 62)
        window.navigationBarColor = Color.rgb(16, 36, 62)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 26, 22, 24)
            setBackgroundColor(Color.rgb(10, 25, 45))
        }
        fun heading(text: String, size: Float = 20f) = TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.WHITE)
            setPadding(0, 12, 0, 8)
        }
        fun field(hintText: String, type: Int = InputType.TYPE_CLASS_TEXT) = EditText(this).apply {
            hint = hintText
            inputType = type
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(14, 12, 14, 12)
            setBackgroundColor(Color.rgb(30, 49, 72))
        }
        fun button(label: String, action: () -> Unit) = Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        }
        fun addFull(view: android.view.View) {
            root.addView(view, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 })
        }

        addFull(heading("Life in India Diaries", 27f))
        addFull(heading("Free • Safe • Organic Promotion", 15f))
        val safety = TextView(this).apply {
            text = "নিরাপত্তা: এই অ্যাপ Facebook পাসওয়ার্ড চাইবে না, অটো-ফলোয়ার/ভুয়া ভিউ তৈরি করবে না এবং আপনার হয়ে কোনো লাইক বা ফলো করবে না।"
            textSize = 14f
            setTextColor(Color.rgb(185, 220, 205))
            setPadding(12, 12, 12, 12)
            setBackgroundColor(Color.rgb(22, 57, 54))
        }
        addFull(safety)

        addFull(heading("১) ভিডিওর লিংক"))
        link = field("Facebook Reel / ভিডিও লিংক", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        addFull(link)
        addFull(heading("২) ভিডিওর বিষয় (ঐচ্ছিক)"))
        topic = field("যেমন: Bangalore life, daily vlog, comedy")
        addFull(topic)
        addFull(button("ক্যাপশন ও হ্যাশট্যাগ তৈরি করুন") { makeCaption() })
        result = TextView(this).apply {
            text = "তৈরি করা ক্যাপশন এখানে দেখা যাবে।"
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(14, 14, 14, 14)
            setBackgroundColor(Color.rgb(30, 49, 72))
        }
        addFull(result)
        addFull(button("ক্যাপশন কপি করুন") {
            val clip = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clip.setPrimaryClip(ClipData.newPlainText("Promotion caption", result.text))
            Toast.makeText(this, "কপি হয়েছে", Toast.LENGTH_SHORT).show()
        })
        addFull(button("ভিডিও শেয়ার করুন") { shareVideo() })

        addFull(heading("৩) নিজের অগ্রগতি লিখে রাখুন"))
        views = field("বর্তমান ভিউ", InputType.TYPE_CLASS_NUMBER)
        followers = field("বর্তমান ফলোয়ার", InputType.TYPE_CLASS_NUMBER)
        addFull(views)
        addFull(followers)
        addFull(button("অগ্রগতি সেভ করুন") { saveProgress() })
        addFull(button("সেভ করা অগ্রগতি দেখুন") { showProgress() })

        val note = TextView(this).apply {
            text = "মনে রাখবেন: লিংক পেস্ট করলেই Facebook-এর ভিউ বা ফলোয়ার স্বয়ংক্রিয়ভাবে বাড়ে না। এই টুল নিরাপদ প্রচারের লেখা ও পরিকল্পনা বানায়। পোস্টের ফলাফল Facebook/Meta-তে আলাদাভাবে দেখুন।"
            textSize = 13f
            setTextColor(Color.rgb(190, 200, 215))
            setPadding(0, 18, 0, 8)
        }
        addFull(note)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun makeCaption() {
        val url = link.text.toString().trim()
        val subject = topic.text.toString().trim().ifBlank { "আমার নতুন ভিডিও" }
        if (url.isNotBlank() && !(url.startsWith("https://") || url.startsWith("http://"))) {
            Toast.makeText(this, "সঠিক https:// ভিডিও লিংক দিন", Toast.LENGTH_LONG).show()
            return
        }
        result.text = "$subject ❤️\n\nভিডিওটি ভালো লাগলে আপনার মতামত কমেন্টে জানাবেন। এমন আরও ভিডিও পেতে Life in India Diaries ফলো করে পাশে থাকুন।\n\n#LifeInIndiaDiaries #IndiaVlog #BanglaVlog #FacebookReels #DailyLife"
    }

    private fun shareVideo() {
        val url = link.text.toString().trim()
        if (url.isBlank() || !(url.startsWith("https://") || url.startsWith("http://"))) {
            Toast.makeText(this, "আগে একটি সঠিক ভিডিও লিংক দিন", Toast.LENGTH_SHORT).show()
            return
        }
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, result.text.toString() + "\n" + url)
        }
        startActivity(Intent.createChooser(share, "ভিডিও শেয়ার করুন"))
    }

    private fun saveProgress() {
        val v = views.text.toString().toLongOrNull()
        val f = followers.text.toString().toLongOrNull()
        if (v == null || f == null) {
            Toast.makeText(this, "ভিউ ও ফলোয়ারের সংখ্যা লিখুন", Toast.LENGTH_SHORT).show()
            return
        }
        prefs.edit().putLong("views", v).putLong("followers", f).apply()
        Toast.makeText(this, "অগ্রগতি এই ফোনে সেভ হয়েছে", Toast.LENGTH_SHORT).show()
    }

    private fun showProgress() {
        val v = prefs.getLong("views", 0L)
        val f = prefs.getLong("followers", 0L)
        Toast.makeText(this, "সেভ করা তথ্য: ভিউ $v • ফলোয়ার $f", Toast.LENGTH_LONG).show()
    }
}
