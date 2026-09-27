package com.sari.dubbing

/**
 * Result of a dubbing operation.
 */
data class DubbingResult(
    val success: Boolean,
    val outputPath: String? = null,
    val errorMessage: String? = null
)
