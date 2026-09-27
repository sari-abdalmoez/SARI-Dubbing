package com.sari.dubbing

/**
 * Orchestrates dubbing using MediaEngine and SpeechEngine.
 */
class DubbingEngine(
    private val mediaEngine: MediaEngine,
    private val speechEngine: SpeechEngine
) {
    fun dub(segments: List<DialogueSegment>, voice: String? = null): DubbingResult {
        try {
            // Very high-level stubbed flow:
            val generatedPaths = segments.map { seg ->
                speechEngine.synthesize(seg.text, voice)
            }
            val output = mediaEngine.mixTracks(*generatedPaths.toTypedArray())
            return DubbingResult(success = true, outputPath = output)
        } catch (e: Exception) {
            return DubbingResult(success = false, errorMessage = e.message)
        }
    }
}
