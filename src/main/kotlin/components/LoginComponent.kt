package com.marcoshier.components

import io.ktor.server.routing.RoutingContext


fun RoutingContext.loginComponent(redirectUrl: String? = null) = """
     <!DOCTYPE html>
        <html>
        <head>
            <title>Login</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: monospace;
                    background-color: white;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                }
                .login-container {
                    background: white;
                    padding: 2rem;
                    border-radius: 1px;
                    width: 100%;
                    border: 1px solid black;
                    max-width: 400px;
                }
                .login-title {
                    text-align: center;
                    margin-bottom: 2rem;
                    font-weight: 400;
                    color: black;
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
                    background-color: white;
                    border: 1px solid black;
                    color: black;
                    border-radius: 4px;
                    font-size: 1rem;
                    cursor: pointer;
                }
                .login-button:hover {
                    background-color: black;
                    color: white;
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
                <form method="post" action="/login">
                    ${if (redirectUrl != null) """<input type="hidden" name="redirect" value="$redirectUrl">""" else ""}
                    <div class="form-group">
                        <label for="password">Password:</label>
                        <input type="password" id="password" name="password" required>
                    </div>
                    <button type="submit" class="login-button">Login</button>
                </form>
            </div>
        </body>
        </html>
""".trimIndent()