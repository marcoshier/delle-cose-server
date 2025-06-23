package com.marcoshier.components

import com.marcoshier.lib.formatFileSize
import com.marcoshier.media.isImageFile
import java.io.File

fun mediaComponent(file: File, baseUrl: String): String {
    val mediaContent = if (file.isImageFile()) {
        """<img src="$baseUrl/image?path=${file.absolutePath}" alt="${file.name}" loading="lazy">"""
    } else {
        """
        <video controls preload="metadata">
            <source src="$baseUrl/video?path=${file.absolutePath}" type="video/${file.extension}">
            Your browser does not support the video tag.
        </video>
        """.trimIndent()
    }

    return """
            <div class="media-item">
                <div class="media-title">${file.name}</div>
                <div class="media-info">
                    Type: ${if (file.isImageFile()) "Image" else "Video"} | 
                    Size: ${formatFileSize(file.length())} | 
                    Extension: ${file.extension.uppercase()}
                </div>
                $mediaContent
            </div>
            """.trimIndent()
}
