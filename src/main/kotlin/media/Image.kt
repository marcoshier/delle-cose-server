package com.marcoshier.media

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext
import java.io.File

private val logger = KotlinLogging.logger {  }

private fun getImageFile(file: File): File? {
    if (!file.exists()) {
        logger.info { "Image does not exist" }
        return null
    } else {
        return file
    }
}

suspend fun RoutingContext.image(path: String) {

    val file = File(path)
    val image = getImageFile(file)

    if (image != null) {
        call.respondFile(image)
    } else {
        call.respondRedirect("/404")
    }

}