package com.sari.dubbing

data class DialogueSegment(

    val id: Int,

    val startMs: Long,

    val endMs: Long,

    val speakerId: String = "speaker_1",

    val originalText: String = "",

    val translatedText: String = "",

    val confidence: Float = 0f
) {

    val durationMs: Long
        get() =
            (endMs - startMs)
                .coerceAtLeast(0L)
}
