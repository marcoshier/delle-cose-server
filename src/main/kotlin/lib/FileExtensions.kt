package com.marcoshier.lib

import java.io.File

private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff")
private val videoExtensions = setOf("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv", "m4v", "3gp")

val File.isVideoFile get() = isVideoExtension(name)
val File.isImageFile get() = isImageExtension(name)

fun isVideoExtension(filename: String): Boolean =
    filename.substringAfterLast('.', "").lowercase() in videoExtensions

fun isImageExtension(filename: String): Boolean =
    filename.substringAfterLast('.', "").lowercase() in imageExtensions

fun convertedNameOf(filename: String): String =
    if (isVideoExtension(filename)) "${filename.substringBeforeLast('.')}.mp4" else filename