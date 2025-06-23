package com.marcoshier.components

fun noMediaComponent() = """
    <!DOCTYPE html>
    <html>
    <head>
        <title>delle cose - server</title>
        <style>
            body { font-family: monospace; margin: 40px; }
            .empty { text-align: center; color: #666; }
        </style>
    </head>
    <body>
        <div class="empty">
            <p>No media files found in this folder.</p>
        </div>
    </body>
    </html>
""".trimIndent()