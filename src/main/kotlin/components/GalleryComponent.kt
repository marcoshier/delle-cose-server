package com.marcoshier.components

import com.marcoshier.auth.UserSession
import com.marcoshier.services.AuthService
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
                .info-title {
                    margin: 0;
                }
                .media-content-info p {
                    margin: 10px 0 30px 0;
                }
                .media-content-info textarea {
                    width: 100%;
                }
                button {
                    margin-bottom: 20px;
                    border: none;
                    background: white;
                    border: 1px solid black;
                    color: black;
                    padding: 15px;
                    cursor: pointer;
                }
                button:hover {
                    background: black;
                    color: white;
                }
                .file-input {
                    display: none; /* Hide the native input */
                }
                
                .upload-btn {
                    padding: 10px 20px;
                    background: #007bff;
                    color: white;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-family: monospace;
                }
                
                .upload-progress {
                    margin: 0 20px;
                }
                
                .upload-btn:hover {
                    background: #0056b3;
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
                    display: flex;
                    justify-content: space-between;
                    flex-direction: row;
                    align-items: center;
                }
                .stats button {
                    margin: 0;
                }
                .upload-container {
                    display: flex;
                    flex-direction: row;
                }
                .delete-btn {
                    background: #dc3545;
                    color: white;
                    border: none;
                    padding: 5px 8px;
                    border-radius: 3px;
                    cursor: pointer;
                    margin-left: 10px;
                    font-size: 14px;
                }

                .delete-btn:hover {
                    background: #c82333;
                }

                .media-info {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }

                .media-item.deleting {
                    opacity: 0.5;
                    pointer-events: none;
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
                    } else """"""
                }
                
            </div>
        </body>
        </html>
    """.trimIndent()
}