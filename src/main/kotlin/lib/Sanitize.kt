package com.marcoshier.lib


fun String.sanitize(): String {
    return this.replace(Regex("[<>:\"/\\\\|?*.]"), "")
}

fun String.sanitizeFileName(): String {
    val lastDotIndex = this.lastIndexOf('.')

    return if (lastDotIndex > 0) {
        // Has extension
        val name = this.substring(0, lastDotIndex)
        val extension = this.substring(lastDotIndex + 1)

        val sanitizedName = name.replace(Regex("[<>:\"/\\\\|?*.]"), "_")
        val sanitizedExtension = extension.replace(Regex("[<>:\"/\\\\|?*.]"), "")

        "$sanitizedName.$sanitizedExtension"
    } else {
        // No extension
        this.replace(Regex("[<>:\"/\\\\|?*.]"), "_")
    }
}