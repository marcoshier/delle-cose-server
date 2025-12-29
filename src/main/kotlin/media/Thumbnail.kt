package com.marcoshier.media

import com.marcoshier.data.MediaFile

fun getThumbnails(images: List<MediaFile>, videos: List<MediaFile>): List<MediaFile> {
    val result = mutableListOf<MediaFile>()

    for (image in images) {
        val path = image.path
        val baseNameWithoutExt = path.substringBeforeLast('.')
        val thumbnailPath = "$baseNameWithoutExt-128.png"
            .replace("converted", "thumbnails")

        result.add(
            MediaFile(
                path = thumbnailPath,
                type = "image"
            )
        )
    }

    for(video in videos) {
        val path = video.path
        val baseNameWithoutExt = path.substringBeforeLast('.')

        for(i in 0 until 10) {
            val thumbnailPath = "$baseNameWithoutExt-128-$i.png"
                .replace("converted", "thumbnails")

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