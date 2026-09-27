package com.sari.dubbing

sealed class DubbingResult {

    data class Success(
        val segments: List<DialogueSegment>,
        val audioPath: String
    ) : DubbingResult()

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : DubbingResult()

    data object Cancelled :
        DubbingResult()
}
