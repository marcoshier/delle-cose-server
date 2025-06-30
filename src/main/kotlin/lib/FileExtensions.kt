package com.marcoshier.lib

import java.io.File

private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff")
private val videoExtensions = setOf("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv", "m4v", "3gp")

val File.isImageFile: Boolean
    get() = this.extension.lowercase() in imageExtensions


val File.isVideoFile: Boolean
    get() = this.extension.lowercase() in videoExtensions
