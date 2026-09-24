package com.marcoshier.components

import com.marcoshier.auth.UserSession
import com.marcoshier.services.AuthService
import com.marcoshier.styles.globalStyles
import io.ktor.server.routing.RoutingContext
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.intellij.lang.annotations.Language
import org.koin.ktor.ext.getKoin
import scripts.uploadScript
import java.io.File

fun RoutingContext.galleryComponent(
    projectName: String,
    mediaFiles: List<File>,
    imageCount: Int,
    videoCount: Int,
    mediaComponents: String
): String {
    val session = call.sessions.get<UserSession>()
    val authService = call.application.getKoin().get<AuthService>()

    val isAuthenticated = session != null && authService.isSessionAuthenticated(session.sessionId)

    @Language("css")
    val localStyles = """
        .processing-status {
            background: white;
            border: 1px solid black;
            border-radius: 1px;
            padding: 15px;
            margin-bottom: 30px;
        }
        .processing-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 14px;
            font-size: 16px;
        }
        .processing-right {
            display: flex;
            align-items: center;
            gap: 14px;
        }
        .processing-overall { font-variant-numeric: tabular-nums; }
        .pf-row {
            display: grid;
            grid-template-columns: 1fr 110px 160px 48px;
            gap: 12px;
            align-items: center;
            padding: 5px 0;
            font-size: 13px;
        }
        .pf-name {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .pf-stage { color: #888; text-align: right; }
        .pf-bar {
            height: 10px;
            border: 1px solid black;
            background: white;
            overflow: hidden;
        }
        .pf-fill {
            height: 100%;
            width: 0%;
            background: black;
            transition: width 0.4s ease;
        }
        
        .pf-pct { text-align: right; font-variant-numeric: tabular-nums; }
        .pf-done .pf-stage { color: black; }
        .pf-queued .pf-stage { color: #aaa; }
        .pf-failed .pf-fill { background: #c0392b; }
        .pf-failed .pf-stage { color: #c0392b; }
        .pf-cancelled .pf-fill { background: #999; }
        .pf-cancelled .pf-stage { color: #999; }
       
        .pf-uploading .pf-fill { background: #777; }
        .pf-uploading .pf-stage { color: #777; }
        
        .cancel-btn {
            background: white;
            color: black;
            border: 1px solid black;
            padding: 6px 12px;
            cursor: pointer;
            font-family: monospace;
            font-size: 13px;
            margin: 0;
        }
        .cancel-btn:hover { background: black; color: white; }
        .cancel-btn:disabled { opacity: 0.5; cursor: default; }
        .cancel-btn:disabled:hover { background: white; color: black; }
        
    """.trimIndent()

    @Language("html")
    val page = """
        <!DOCTYPE html>
        <head>
            <title>Gallery - ${projectName}</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                $globalStyles
                $localStyles
            </style>
        </head>
        <body>
            <div class="container">
            
                <div class="header">
                    <h1>media / ${projectName}</h1>
                     ${if (!isAuthenticated) """<a href="/login" class="login-link">login</a>""" 
                       else """<a href="/logout" class="login-link">logout</a>"""}
                </div>
                
                ${
                    if (isAuthenticated) """
                       <div class="processing-status" id="processingStatus" style="display: none;">
                            <div class="processing-header">
                                <span id="processingText">Working…</span>
                                <span class="processing-right">
                                    <span class="processing-overall" id="processingOverall">0%</span>
                                    <button class="cancel-btn" id="cancelBtn" type="button" style="display:none;">Cancel</button>
                                </span>
                            </div>
                            <div id="uploadFiles"></div>
                            <div id="processingFiles"></div>
                       </div>
                    """ else ""
                }
                
                <div class="stats">
                    <div class="stats-item">
                        <strong>Totale File:</strong> ${mediaFiles.size} 
                        (<strong>Immagini:</strong> $imageCount, <strong>Video:</strong> $videoCount)
                    </div>
                    
                    ${
                        if (isAuthenticated) """
                            <div class="upload-container stats-item" id="uploadContainer">
                                <input type="file" id="fileInput" class="file-input" multiple accept="image/*,video/*">
                                <button type="button" class="upload-btn" onclick="document.getElementById('fileInput').click()">
                                    Upload
                                </button>
                            </div>
                        """ else ""
                    }
                </div>
                
                $mediaComponents
                
                ${if (isAuthenticated) { uploadScript(projectName) } else ""}
                
            </div>
        </body>
        </html>
    """.trimIndent()

    return page
}


