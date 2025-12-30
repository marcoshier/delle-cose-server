package com.marcoshier.media

import com.marcoshier.data.MediaFile

fun getThumbnails(images: List<MediaFile>, videos: List<MediaFile>): List<MediaFile> {
    val result = mutableListOf<MediaFile>()

    for (image in images) {
        val path = image.path
        val thumbnailPath = path
            .replace("converted", "thumbnails") + "-128.png"

        result.add(
            MediaFile(
                path = thumbnailPath,
                type = "image"
            )
        )
    }

    for(video in videos) {
        val path = video.path
        println("from manifest $path")

        for(i in 0 until 10) {
            val thumbnailPath = path
                .replace("converted", "thumbnails") + "-128-$i.png"

            result.add(
                MediaFile(
                    path = thumbnailPath,
                    type = "video"
                )
            )
        }
    }

    return result
}