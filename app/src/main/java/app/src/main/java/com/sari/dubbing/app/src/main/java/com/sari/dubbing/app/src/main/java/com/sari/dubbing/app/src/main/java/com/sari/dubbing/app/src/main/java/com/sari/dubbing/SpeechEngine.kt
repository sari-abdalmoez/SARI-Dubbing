package com.sari.dubbing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpeechEngine {

    suspend fun transcribe(
        audioPath: String
    ): List<DialogueSegment> =
        withContext(Dispatchers.Default) {

            /*
             * Whisper الحقيقي سيتم ربطه هنا.
             *
             * لا نضع نصًا وهميًا.
             *
             * المدخل:
             * audioPath
             *
             * المخرج:
             * DialogueSegment
             *
             * مع:
             * - بداية الكلام
             * - نهاية الكلام
             * - النص
             * - درجة الثقة
             */

            emptyList()
        }
}
