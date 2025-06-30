package com.marcoshier.styles

import org.intellij.lang.annotations.Language

@Language("CSS")
val stylesCss = """
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
""".trimIndent()