package com.sari.dubbing

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelChildren
import kotlin.math.roundToInt

class MainActivity : Activity() {

    private lateinit var root: LinearLayout

    private var selectedVideoUri: Uri? = null

    private lateinit var videoPreview: VideoView
    private lateinit var videoName: TextView
    private lateinit var startButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var statusText: TextView

    private lateinit var sourceLanguage: Spinner
    private lateinit var targetLanguage: Spinner

    private lateinit var preserveMusic: Switch
    private lateinit var preserveEffects: Switch
    private lateinit var detectSpeakers: Switch

    private lateinit var dubbingEngine: DubbingEngine

    private val dubbingScope =
        CoroutineScope(Dispatchers.Main + Job())

    private val pickVideoCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dubbingEngine =
            DubbingEngine(applicationContext)

        buildInterface()
    }

    // =========================================================
    // INTERFACE
    // =========================================================

    private fun buildInterface() {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(
                Color.rgb(10, 10, 15)
            )
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(root)
        }

        setContentView(scroll)

        createHeader()
        createVideoPicker()
        createLanguageSection()
        createVoiceSection()
        createAdvancedSection()
        createStartButton()
        createProgressSection()
    }

    // =========================================================
    // HEADER
    // =========================================================

    private fun createHeader() {

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                dp(20),
                dp(28),
                dp(20),
                dp(20)
            )
        }

        val title = TextView(this).apply {
            text = "SARI DUBBING"
            textSize = 27f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "ترجمة ودبلجة الفيديو بالذكاء الاصطناعي"
            textSize = 14f
            setTextColor(
                Color.rgb(175, 175, 185)
            )
            gravity = Gravity.CENTER
        }

        header.addView(title)
        header.addView(subtitle)

        root.addView(header)
    }

    // =========================================================
    // VIDEO
    // =========================================================

    private fun createVideoPicker() {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(18),
                dp(8),
                dp(18),
                dp(8)
            )
        }

        videoPreview = VideoView(this).apply {
            setBackgroundColor(
                Color.rgb(20, 20, 27)
            )
            visibility = View.GONE
        }

        container.addView(
            videoPreview,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(210)
            )
        )

        val chooseButton = Button(this).apply {
            text = "🎬  اختيار فيديو"
            textSize = 16f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundColor(
                Color.rgb(92, 55, 180)
            )

            setOnClickListener {
                chooseVideo()
            }
        }

        container.addView(
            chooseButton,
            marginParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54),
                0,
                10
            )
        )

        videoName = TextView(this).apply {
            text = "لم يتم اختيار فيديو"
            textSize = 13f
            setTextColor(
                Color.rgb(160, 160, 170)
            )
            gravity = Gravity.CENTER
        }

        container.addView(videoName)

        root.addView(container)
    }

    private fun chooseVideo() {

        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

                type = "video/*"

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            }

        startActivityForResult(
            intent,
            pickVideoCode
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode != pickVideoCode ||
            resultCode != RESULT_OK
        ) {
            return
        }

        val uri = data?.data ?: return

        selectedVideoUri = uri

        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }

        videoPreview.visibility =
            View.VISIBLE

        videoPreview.setVideoURI(uri)
        videoPreview.seekTo(1)

        videoName.text =
            getFileName(uri)

        startButton.isEnabled = true
    }

    private fun getFileName(
        uri: Uri
    ): String {

        var result = "video"

        contentResolver.query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME
            ),
            null,
            null,
            null
        )?.use { cursor ->

            if (cursor.moveToFirst()) {

                val index =
                    cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )

                if (index >= 0) {
                    result =
                        cursor.getString(index)
                }
            }
        }

        return result
    }

    // =========================================================
    // LANGUAGES
    // =========================================================

    private fun createLanguageSection() {

        val card = createCard()

        addSectionTitle(
            card,
            "🌐 اللغات"
        )

        sourceLanguage =
            createSpinner(
                arrayOf(
                    "اكتشاف تلقائي",
                    "العربية",
                    "English",
                    "Français",
                    "Español",
                    "Deutsch"
                )
            )

        targetLanguage =
            createSpinner(
                arrayOf(
                    "العربية",
                    "English",
                    "Français",
                    "Español",
                    "Deutsch"
                )
            )

        addLabel(
            card,
            "اللغة الأصلية"
        )

        card.addView(
            sourceLanguage
        )

        addLabel(
            card,
            "ترجمة إلى"
        )

        card.addView(
            targetLanguage
        )

        root.addView(card)
    }

    // =========================================================
    // VOICE
    // =========================================================

    private fun createVoiceSection() {

        val card = createCard()

        addSectionTitle(
            card,
            "🎙️ أسلوب الدبلجة"
        )

        val spinner =
            createSpinner(
                arrayOf(
                    "دبلجة طبيعية",
                    "ترجمة صوتية",
                    "ترجمة نصية فقط"
                )
            )

        card.addView(spinner)

        val info = TextView(this).apply {
            text =
                "الدبلجة الطبيعية تراعي سياق الحوار والتوقيت."
            textSize = 12f
            setTextColor(
                Color.rgb(150, 150, 160)
            )
        }

        card.addView(info)

        root.addView(card)
    }

    // =========================================================
    // ADVANCED
    // =========================================================

    private fun createAdvancedSection() {

        val card = createCard()

        addSectionTitle(
            card,
            "⚙️ خيارات الدبلجة"
        )

        preserveMusic =
            createSwitch(
                "الحفاظ على الموسيقى",
                true
            )

        preserveEffects =
            createSwitch(
                "الحفاظ على المؤثرات",
                true
            )

        detectSpeakers =
            createSwitch(
                "تمييز المتحدثين",
                true
            )

        card.addView(
            preserveMusic
        )

        card.addView(
            preserveEffects
        )

        card.addView(
            detectSpeakers
        )

        addLabel(
            card,
            "جودة المعالجة"
        )

        card.addView(
            createSpinner(
                arrayOf(
                    "خفيفة",
                    "متوازنة",
                    "عالية الجودة"
                )
            )
        )

        root.addView(card)
    }

    // =========================================================
    // START BUTTON
    // =========================================================

    private fun createStartButton() {

        startButton = Button(this).apply {

            text = "🎙️  بدء الدبلجة"

            textSize = 17f

            isAllCaps = false

            isEnabled = false

            setTextColor(Color.WHITE)

            setBackgroundColor(
                Color.rgb(120, 65, 210)
            )

            setOnClickListener {

                val uri =
                    selectedVideoUri

                if (uri == null) {

                    Toast.makeText(
                        this@MainActivity,
                        "اختر فيديو أولًا",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                startDubbing(uri)
            }
        }

        root.addView(
            startButton,
            marginParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58),
                18,
                14
            )
        )
    }

    // =========================================================
    // PROGRESS
    // =========================================================

    private fun createProgressSection() {

        val card = createCard()

        statusText = TextView(this).apply {
            text = "جاهز"
            textSize = 14f
            setTextColor(
                Color.rgb(180, 180, 190)
            )
            gravity = Gravity.CENTER
        }

        progressBar =
            ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {
                max = 100
                progress = 0
            }

        progressText = TextView(this).apply {
            text = "0%"
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        card.addView(statusText)

        card.addView(
            progressBar,
            marginParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(10),
                0,
                8
            )
        )

        card.addView(progressText)

        root.addView(card)
    }

    // =========================================================
    // REAL ENGINE CONNECTION
    // =========================================================

    private fun startDubbing(
        videoUri: Uri
    ) {

        startButton.isEnabled = false

        progressBar.progress = 0

        progressText.text = "0%"

        statusText.text =
            "بدء تحليل الفيديو..."

        dubbingScope.launch {

            val result =
                dubbingEngine.analyzeVideo(
                    videoUri
                ) { progress, status ->

                    runOnUiThread {

                        progressBar.progress =
                            progress

                        progressText.text =
                            "$progress%"

                        statusText.text =
                            status
                    }
                }

            when (result) {

                is DubbingResult.Success -> {

                    startButton.isEnabled =
                        true

                    statusText.text =
                        "تم تحليل الفيديو"

                    progressBar.progress =
                        100

                    progressText.text =
                        "100%"

                    Toast.makeText(
                        this@MainActivity,
                        "تم تجهيز الفيديو",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is DubbingResult.Error -> {

                    startButton.isEnabled =
                        true

                    statusText.text =
                        "حدث خطأ"

                    Toast.makeText(
                        this@MainActivity,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                DubbingResult.Cancelled -> {

                    startButton.isEnabled =
                        true

                    statusText.text =
                        "تم إلغاء العملية"
                }
            }
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private fun createCard():
        LinearLayout {

        return LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
            )

            setBackgroundColor(
                Color.rgb(20, 20, 28)
            )

            layoutParams =
                marginParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    14,
                    8
                )
        }
    }

    private fun addSectionTitle(
        parent: LinearLayout,
        text: String
    ) {

        val title =
            TextView(this).apply {

                this.text = text

                textSize = 18f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(Color.WHITE)
            }

        parent.addView(title)
    }

    private fun addLabel(
        parent: LinearLayout,
        text: String
    ) {

        val label =
            TextView(this).apply {

                this.text = text

                textSize = 13f

                setTextColor(
                    Color.rgb(
                        165,
                        165,
                        175
                    )
                )

                setPadding(
                    0,
                    dp(8),
                    0,
                    dp(4)
                )
            }

        parent.addView(label)
    }

    private fun createSpinner(
        items: Array<String>
    ): Spinner {

        return Spinner(this).apply {

            adapter =
                ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    items
                )
        }
    }

    private fun createSwitch(
        text: String,
        checked: Boolean
    ): Switch {

        return Switch(this).apply {

            this.text = text

            isChecked = checked

            textSize = 14f

            setTextColor(Color.WHITE)
        }
    }

    private fun marginParams(
        width: Int,
        height: Int,
        left: Int,
        bottom: Int
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            width,
            height
        ).apply {

            setMargins(
                dp(left),
                0,
                dp(left),
                dp(bottom)
            )
        }
    }

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).roundToInt()
    }

    override fun onDestroy() {

        dubbingScope.cancelChildren()

        super.onDestroy()
    }
}
