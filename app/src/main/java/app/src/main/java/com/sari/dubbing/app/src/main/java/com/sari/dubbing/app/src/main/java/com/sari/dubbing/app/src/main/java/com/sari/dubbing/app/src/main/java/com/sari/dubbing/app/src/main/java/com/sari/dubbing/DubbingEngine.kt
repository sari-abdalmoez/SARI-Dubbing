package com.sari.dubbing

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class DubbingEngine(
    context: Context
) {

    private val mediaEngine =
        MediaEngine(
            context.applicationContext
        )

    private val speechEngine =
        SpeechEngine()

    suspend fun analyzeVideo(
        uri: Uri,
        onProgress: (
            Int,
            String
        ) -> Unit = { _, _ -> }
    ): DubbingResult =
        withContext(Dispatchers.IO) {

            try {

                coroutineContext.ensureActive()

                onProgress(
                    5,
                    "فحص الفيديو..."
                )

                val mediaInfo =
                    mediaEngine.inspect(uri)

                if (!mediaInfo.hasVideo) {

                    return@withContext
                        DubbingResult.Error(
                            "الملف لا يحتوي على فيديو"
                        )
                }

                if (!mediaInfo.hasAudio) {

                    return@withContext
                        DubbingResult.Error(
                            "الفيديو لا يحتوي على صوت"
                        )
                }

                coroutineContext.ensureActive()

                onProgress(
                    20,
                    "نسخ الفيديو إلى مساحة المعالجة..."
                )

                val input =
                    mediaEngine.copyToCache(
                        uri
                    )

                coroutineContext.ensureActive()

                onProgress(
                    40,
                    "تجهيز الصوت..."
                )

                /*
                 * لاحقًا هنا سيتم استخراج
                 * المسار الصوتي PCM.
                 */

                val audioPath =
                    input.absolutePath

                coroutineContext.ensureActive()

                onProgress(
                    60,
                    "تحليل الكلام..."
                )

                val segments =
                    speechEngine.transcribe(
                        audioPath
                    )

                coroutineContext.ensureActive()

                onProgress(
                    100,
                    "اكتمل التحليل"
                )

                DubbingResult.Success(
                    segments = segments,
                    audioPath = audioPath
                )

            } catch (
                error:
                kotlinx.coroutines.CancellationException
            ) {

                DubbingResult.Cancelled

            } catch (error: Exception) {

                DubbingResult.Error(
                    message =
                        error.message
                            ?: "حدث خطأ أثناء المعالجة",
                    cause = error
                )
            }
        }
}
