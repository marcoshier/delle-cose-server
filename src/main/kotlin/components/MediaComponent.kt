package com.marcoshier.components

import com.marcoshier.lib.formatFileSize
import com.marcoshier.media.isImageFile
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.log

val logger = KotlinLogging.logger {  }

fun mediaComponent(file: File, baseUrl: String, folderName: String): String {

    val encodedFileName = URLEncoder.encode(file.name, StandardCharsets.UTF_8)
    val encodedFolderName = URLEncoder.encode(folderName, StandardCharsets.UTF_8)
    val fullUrl = "$encodedFolderName/$encodedFileName"

    logger.info { "full url: $fullUrl" }

    val mediaContent = if (file.isImageFile()) {
        """
            <img src="/image/$fullUrl" alt="${file.name}" loading="lazy">""".trimIndent()
    } else {
        """
        <video controls preload="metadata">
            <source src="/video/$fullUrl" type="video/${file.extension}">
            Your browser does not support the video tag.
        </video>
        """.trimIndent()
    }

    return """
            <div class="media-item">
                <div class="media-title">${file.name}</div>
                <div class="media-info">
                    Size: ${formatFileSize(file.length())}
                </div>
                <div class="media-content-wrapper">
                    <div class="media-content-item media-content">
                        $mediaContent
                    </div>
                    <div class="media-content-info media-content">
                        <p>popi</p>
                    </div>
                </div>
            </div>
            """.trimIndent()
}
