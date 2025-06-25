package com.marcoshier.components

import io.ktor.server.routing.RoutingContext


fun RoutingContext.loginComponent() = """
     <!DOCTYPE html>
        <html>
        <head>
            <title>Login</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f5f5f5;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                }
                .login-container {
                    background: white;
                    padding: 2rem;
                    border-radius: 8px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    width: 100%;
                    max-width: 400px;
                }
                .login-title {
                    text-align: center;
                    margin-bottom: 2rem;
                    color: #333;
                }
                .form-group {
                    margin-bottom: 1rem;
                }
                label {
                    display: block;
                    margin-bottom: 0.5rem;
                    color: #555;
                }
                input[type="password"] {
                    width: 100%;
                    padding: 0.75rem;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    font-size: 1rem;
                    box-sizing: border-box;
                }
                .login-button {
                    width: 100%;
                    padding: 0.75rem;
                    background-color: #007bff;
                    color: white;
                    border: none;
                    border-radius: 4px;
                    font-size: 1rem;
                    cursor: pointer;
                }
                .login-button:hover {
                    background-color: #0056b3;
                }
                .error {
                    color: #dc3545;
                    text-align: center;
                    margin-top: 1rem;
                }
            </style>
        </head>
        <body>
            <div class="login-container">
                <h2 class="login-title">Media Gallery Login</h2>
                <form method="post" action="/login">
                    <div class="form-group">
                        <label for="password">Password:</label>
                        <input type="password" id="password" name="password" required>
                    </div>
                    <button type="submit" class="login-button">Login</button>
                    ${if (call.request.queryParameters["error"] == "invalid")
                        """<div class="error">Invalid password. Please try again.</div>"""
                    else ""}
                </form>
            </div>
        </body>
        </html>
""".trimIndent()