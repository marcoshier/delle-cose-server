package com.marcoshier.components

import com.marcoshier.auth.UserSession
import com.marcoshier.data.MediaItem
import com.marcoshier.lib.formatFileSize
import com.marcoshier.media.isImageFile
import com.marcoshier.services.AuthService
import com.marcoshier.services.MediaService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.routing.RoutingContext
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.koin.ktor.ext.getKoin
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

val logger = KotlinLogging.logger {  }

fun RoutingContext.mediaComponent(file: File, folderName: String, mediaInfo: MediaItem): String {
    val session = call.sessions.get<UserSession>()
    val authService = call.application.getKoin().get<AuthService>()

    val isAuthenticated = session != null && authService.isSessionAuthenticated(session.sessionId)

    val encodedFileName = URLEncoder.encode(file.name, StandardCharsets.UTF_8)
    val encodedFolderName = URLEncoder.encode(folderName, StandardCharsets.UTF_8)
    val fullUrl = "$encodedFolderName/$encodedFileName"

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
                        ${if (isAuthenticated) { """
                            <form method="post" action="/update-caption" class="caption-form">
                                <h3 class="info-title">caption:</h3>
                                <input type="hidden" name="foldername" value="$folderName">
                                <input type="hidden" name="filename" value="${mediaInfo.filename}">
                                <textarea id="caption" name="caption" placeholder="Nessuna caption..." rows="5">${mediaInfo.caption}</textarea>
                                <button type="submit" class="save-caption-btn">Salva</button>
                            </form>
                            <h3 class="info-title">file:</h3>
                            <p>${mediaInfo.filename}</p>
                            <h3 class="info-title">formato:</h3>
                            <p>${mediaInfo.type}</p>
                        """.trimIndent() } else { """
                            <h3 class="info-title">caption:</h3>
                            <p>${mediaInfo.caption}</p>
                            <h3 class="info-title">file:</h3>
                            <p>${mediaInfo.filename}</p>
                            <h3 class="info-title">formato:</h3>
                            <p>${mediaInfo.type}</p>
                            <h3 class="info-title">aggiornato:</h3>
                            <p>${mediaInfo.formattedDate}</p>
                        """.trimIndent()}}
                    </div>
                </div>
            </div>
            """.trimIndent()
}
