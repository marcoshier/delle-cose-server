package com.marcoshier.components

import java.io.File

fun galleryComponent(projectName: String, mediaFiles: List<File>, imageCount: Int, videoCount: Int, mediaComponents: String) = """
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
                    background-color: #f5f5f5;
                }
                .container {
                    max-width: 90vw;
                    margin: 0 auto;
                }
                h1 {
                    text-align: center;
                    color: #000;
                    margin-bottom: 30px;
                }
                .media-item {
                    margin-bottom: 30px;
                    background: white;
                    padding: 20px;
                    border-radius: 8px;
                }
                .media-title {
                    font-size: 18px;
                    font-weight: bold;
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
                    border-radius: 4px;
                    display: block;
                }
                video {
                    max-width: 100%;
                    height: auto;
                    border-radius: 4px;
                    display: block;
                }
                .stats {
                    text-align: center;
                    margin-bottom: 30px;
                    padding: 15px;
                    background: white;
                    border-radius: 8px;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Media / ${projectName}</h1>
                <div class="stats">
                    <strong>Totale File:</strong> ${mediaFiles.size} 
                    (<strong>Immagini:</strong> $imageCount, <strong>Video:</strong> $videoCount)
                </div>
                $mediaComponents
            </div>
        </body>
        </html>
    """.trimIndent()