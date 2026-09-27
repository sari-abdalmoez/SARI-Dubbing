package com.sari.dubbing

/**
 * Represents a single dialogue segment in the source audio.
 */
data class DialogueSegment(
    val speaker: String,
    val text: String,
    val startMs: Long,
    val endMs: Long
)
