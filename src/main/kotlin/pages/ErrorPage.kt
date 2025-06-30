package com.marcoshier.pages

fun errorPage(error: String): String {
    return """
            <!DOCTYPE html>
            <html>
            <head><title>Error</title></head>
            <body>
                <h1>Error</h1>
                <p>$error</p>
            </body>
            </html>
            """.trimIndent()
}