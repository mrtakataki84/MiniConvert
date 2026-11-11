package com.example.miniconvert

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

class MainActivity : Activity() {
    private val selectedVideos = mutableListOf<Pair<Uri, String>>()
    private lateinit var queueBox: LinearLayout
    private lateinit var queueCount: TextView
    private lateinit var emptyState: TextView
    private val blue = Color.rgb(37, 99, 235)
    private val ink = Color.rgb(28, 35, 48)
    private val muted = Color.rgb(112, 121, 135)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
    }

    private fun buildScreen() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(20))
            setBackgroundColor(Color.rgb(247, 248, 250))
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)

        content.addView(label("MiniConvert", 28, ink, true))
        content.addView(label("Video converter", 14, muted, false).apply {
            setPadding(0, dp(3), 0, dp(20))
        })

        val pickCard = card()
        pickCard.orientation = LinearLayout.VERTICAL
        pickCard.gravity = Gravity.CENTER
        pickCard.setPadding(dp(18), dp(24), dp(18), dp(24))
        pickCard.addView(label("Pilih video untuk mula", 18, ink, true))
        pickCard.addView(label("Pilih satu atau beberapa fail sekali gus", 13, muted, false).apply {
            setPadding(0, dp(7), 0, dp(16))
        })
        pickCard.addView(button("＋  Pilih video", blue) { openVideoPicker() })
        content.addView(pickCard)

        content.addView(sectionTitle("Tetapan output"))
        val settings = card()
        settings.orientation = LinearLayout.VERTICAL
        settings.setPadding(dp(16), dp(12), dp(16), dp(12))
        settings.addView(label("Format", 13, muted, true))
        val formatSpinner = spinner(listOf("MP4", "MKV", "WebM", "MOV"))
        settings.addView(formatSpinner)
        settings.addView(label("Codec video", 13, muted, true).apply {
            setPadding(0, dp(12), 0, 0)
        })
        settings.addView(spinner(listOf("H.264 (paling serasi)", "H.265 / HEVC", "AV1")))
        settings.addView(label("Resolusi", 13, muted, true).apply {
            setPadding(0, dp(12), 0, 0)
        })
        settings.addView(spinner(listOf("Kekalkan asal", "4K (2160p)", "1080p", "720p", "480p")))
        settings.addView(label("Pemprosesan", 13, muted, true).apply {
            setPadding(0, dp(12), 0, 0)
        })
        settings.addView(spinner(listOf("Auto (hardware jika disokong)", "Hardware encoding", "Software encoding")))
        content.addView(settings)

        val queueHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        queueHeader.addView(sectionTitle("Senarai conversion"), LinearLayout.LayoutParams(0, dp(42), 1f))
        queueCount = label("0 video", 12, muted, true)
        queueHeader.addView(queueCount)
        content.addView(queueHeader)

        val queueCard = card()
        queueCard.orientation = LinearLayout.VERTICAL
        queueCard.setPadding(dp(14), dp(14), dp(14), dp(14))
        emptyState = label("Belum ada video dipilih.", 14, muted, false)
        queueCard.addView(emptyState)
        queueBox = queueCard
        content.addView(queueCard)

        val convert = button("Mula conversion", blue) {
            Toast.makeText(this, "Enjin conversion akan disambungkan pada fasa seterusnya.", Toast.LENGTH_LONG).show()
        }
        val convertParams = LinearLayout.LayoutParams(-1, dp(52))
        convertParams.topMargin = dp(18)
        content.addView(convert, convertParams)

        content.addView(label("Offline • Tiada iklan • Batch queue", 12, muted, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, dp(4))
        })

        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(page)
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, 101)
    }

    @Deprecated("Uses the platform document picker for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 101 || resultCode != RESULT_OK || data == null) return

        val picked = mutableListOf<Uri>()
        data.clipData?.let { clip ->
            for (i in 0 until clip.itemCount) picked.add(clip.getItemAt(i).uri)
        } ?: data.data?.let { picked.add(it) }

        picked.forEach { uri ->
            if (selectedVideos.none { it.first == uri }) {
                selectedVideos.add(uri to getDisplayName(uri))
            }
        }
        refreshQueue()
    }

    private fun getDisplayName(uri: Uri): String {
        var name = "Video"
        val cursor: Cursor? = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) name = it.getString(0) ?: name
        }
        return name
    }

    private fun refreshQueue() {
        queueBox.removeAllViews()
        queueCount.text = "${selectedVideos.size} video"
        if (selectedVideos.isEmpty()) {
            queueBox.addView(label("Belum ada video dipilih.", 14, muted, false))
            return
        }
        selectedVideos.forEachIndexed { index, item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, dp(8))
            }
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            info.addView(label(item.second, 14, ink, true))
            info.addView(label("Menunggu conversion", 12, muted, false).apply {
                setPadding(0, dp(3), 0, 0)
            })
            row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
            val remove = TextView(this).apply {
                text = "Buang"
                textSize = 12f
                setTextColor(blue)
                setPadding(dp(10), dp(10), dp(4), dp(10))
                setOnClickListener {
                    selectedVideos.removeAt(index)
                    refreshQueue()
                }
            }
            row.addView(remove)
            queueBox.addView(row)
            if (index < selectedVideos.lastIndex) {
                val divider = View(this).apply { setBackgroundColor(Color.rgb(232, 235, 240)) }
                queueBox.addView(divider, LinearLayout.LayoutParams(-1, dp(1)))
            }
        }
    }

    private fun sectionTitle(text: String) = label(text, 17, ink, true).apply {
        setPadding(0, dp(20), 0, dp(8))
    }

    private fun label(text: String, size: Int, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun card() = LinearLayout(this).apply {
        background = rounded(Color.WHITE, 18)
        elevation = dp(1).toFloat()
        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(4)
        layoutParams = params
    }

    private fun button(text: String, color: Int, action: () -> Unit) = Button(this).apply {
        this.text = text
        isAllCaps = false
        textSize = 15f
        setTextColor(Color.WHITE)
        background = rounded(color, 14)
        setOnClickListener { action() }
        minHeight = dp(48)
    }

    private fun spinner(items: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
