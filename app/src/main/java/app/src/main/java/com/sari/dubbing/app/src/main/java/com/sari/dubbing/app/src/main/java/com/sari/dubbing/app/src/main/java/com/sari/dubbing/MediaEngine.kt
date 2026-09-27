package com.sari.dubbing

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

class MediaEngine(
    private val context: Context
) {

    data class MediaInfo(
        val durationMs: Long,
        val hasVideo: Boolean,
        val hasAudio: Boolean
    )

    fun inspect(
        uri: Uri
    ): MediaInfo {

        val extractor =
            MediaExtractor()

        try {

            context.contentResolver
                .openAssetFileDescriptor(
                    uri,
                    "r"
                )
                ?.use { descriptor ->

                    extractor.setDataSource(
                        descriptor.fileDescriptor,
                        descriptor.startOffset,
                        descriptor.length
                    )

                    var hasVideo = false
                    var hasAudio = false

                    var durationUs = 0L

                    for (
                        index in
                        0 until extractor.trackCount
                    ) {

                        val format =
                            extractor.getTrackFormat(
                                index
                            )

                        val mime =
                            format.getString(
                                MediaFormat.KEY_MIME
                            ) ?: continue

                        if (
                            mime.startsWith("video/")
                        ) {
                            hasVideo = true
                        }

                        if (
                            mime.startsWith("audio/")
                        ) {
                            hasAudio = true
                        }

                        if (
                            format.containsKey(
                                MediaFormat.KEY_DURATION
                            )
                        ) {

                            durationUs =
                                maxOf(
                                    durationUs,
                                    format.getLong(
                                        MediaFormat.KEY_DURATION
                                    )
                                )
                        }
                    }

                    return MediaInfo(
                        durationMs =
                            durationUs / 1000L,
                        hasVideo = hasVideo,
                        hasAudio = hasAudio
                    )
                }

            throw IllegalArgumentException(
                "تعذر فتح الفيديو"
            )

        } finally {

            extractor.release()
        }
    }

    fun copyToCache(
        uri: Uri
    ): File {

        val output =
            File(
                context.cacheDir,
                "sari_input_video"
            )

        context.contentResolver
            .openInputStream(uri)
            ?.use { input ->

                FileOutputStream(output)
                    .use { outputStream ->

                    val buffer =
                        ByteArray(64 * 1024)

                    while (true) {

                        val count =
                            input.read(buffer)

                        if (count <= 0) {
                            break
                        }

                        outputStream.write(
                            buffer,
                            0,
                            count
                        )
                    }
                }

            } ?: throw IllegalArgumentException(
                "تعذر قراءة الفيديو"
            )

        return output
    }
}
