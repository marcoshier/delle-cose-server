package com.marcoshier.components

import com.marcoshier.auth.UserSession
import com.marcoshier.scripts.uploadScript
import com.marcoshier.services.AuthService
import com.marcoshier.styles.stylesCss
import io.ktor.server.routing.RoutingContext
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.koin.ktor.ext.getKoin
import java.io.File


fun RoutingContext.galleryComponent(projectName: String, mediaFiles: List<File>, imageCount: Int, videoCount: Int, mediaComponents: String): String {
    val session = call.sessions.get<UserSession>()
    val authService = call.application.getKoin().get<AuthService>()

    val isAuthenticated = session != null && authService.isSessionAuthenticated(session.sessionId)

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Gallery - ${projectName}</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            $stylesCss
        </head>
        <body>
            <div class="container">
            
                <div class="header">
                    <h1>media / ${projectName}</h1>
                     ${if (!isAuthenticated) 
                         """<a href="/login" class="login-link">login</a>""" else 
                             """<a href="/logout" class="login-link">logout</a>"""
                     }
                </div>
                
                <div class="stats">
                    <div class="stats-item">
                        <strong>Totale File:</strong> ${mediaFiles.size} 
                        (<strong>Immagini:</strong> $imageCount, <strong>Video:</strong> $videoCount)
                    </div>
                    
                    ${if (isAuthenticated) """
                        <div class="upload-container stats-item" id="uploadContainer">
                            <input type="file" id="fileInput" class="file-input" multiple accept="image/*,video/*">
                            <button type="button" class="upload-btn" onclick="document.getElementById('fileInput').click()">
                                Upload
                            </button>
                            <div class="upload-progress" id="uploadProgress">
                                <div class="progress-bar">
                                    <div class="progress-fill" id="progressFill"></div>
                                </div>
                                <div class="upload-status" id="uploadStatus"></div>
                            </div>
                        </div>
                    """ else ""}
                </div>
                
                
                $mediaComponents
                
                
                ${if (isAuthenticated) {
                        uploadScript(projectName)
                    } else ""
                }
                
            </div>
        </body>
        </html>
    """.trimIndent()
}