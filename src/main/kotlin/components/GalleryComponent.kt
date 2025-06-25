package com.marcoshier.components

import com.marcoshier.auth.UserSession
import com.marcoshier.services.AuthService
import io.ktor.server.request.uri
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
            <style>
                body {
                    font-family: monospace;
                    margin: 0;
                    padding: 20px;
                    background-color: white;
                }
                .container {
                    max-width: 90vw;
                    margin: 0 auto;
                }
                .header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-top: 30px;
                    margin-bottom: 60px;
                }
                h1 {
                    font-weight: 400;
                    color: #000;
                    margin: 0;
                }
                .login-link {
                    color: black;
                    font-size: 15px;
                }
                .media-content-wrapper {
                    display: flex;
                    width: 100%;
                    flex-direction: row;
                }
                .media-content {
                    width: 100%;
                }
                .media-content-info {
                    padding-left: 20px;
                }
                .media-item {
                    margin-bottom: 10px;
                    background: white;
                    padding: 20px;
                    border-radius: 1px;
                    border: 1px solid black;
                }
                .media-title {
                    font-size: 20px;
                    font-weight: 400;
                    margin-bottom: 10px;
                    color: #333;
                }
                .media-info {
                    font-size: 14px;
                    color: #666;
                    margin-bottom: 15px;
                }
                img {
                    max-width: 100%;
                    height: auto;
                    display: block;
                }
                video {
                    width: 100%;
                    height: auto;
                    display: block;
                }
                .stats {
                    text-align: left;
                    margin-bottom: 30px;
                    padding: 15px;
                    font-size: 16px;
                    background: white;
                    border-radius: 1px;
                    border: 1px solid black;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>media / ${projectName}</h1>
                     ${if (!isAuthenticated) """<a href="/login" class="login-link">login</a>""" else """<a href="/logout" class="login-link">logout</a>"""}
                </div>
                <div class="stats">
                    <strong>Totale File:</strong> ${mediaFiles.size} 
                    (<strong>Immagini:</strong> $imageCount, <strong>Video:</strong> $videoCount)
                </div>
                $mediaComponents
            </div>
        </body>
        </html>
    """.trimIndent()
}