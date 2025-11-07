package com.marcoshier.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Data (
    val projects: List<Project>,
    val authors: List<Author>,
    val categories: List<Category>
)

@Serializable
data class Project(
    val name: String,
    val year: Int? = null,
    val description: String = "",
    val authors: List<Int>,
    val categories: List<Int>,
    val partner: String,
    val location: String,
    val slug: String
)

@Serializable
data class Author(
    val name: String,
    val projects: List<Int>
)

@Serializable
data class Category(
    val name: String,
    val projects: List<Int>
)


@Serializable
data class MediaItems(
    val items: MutableMap<String, MediaItem>
)

@Serializable
data class MediaItem(
    val filename: String,
    val caption: String,
    val type: String,
    val updatedAt: Long
) {

    val formattedDate: String
        get() = try {
            val instant = Instant.fromEpochSeconds(updatedAt)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            "${localDateTime.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, " +
            "${localDateTime.dayOfMonth} ${localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${localDateTime.year}," +
            "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            "Unknown"
        }
}

@Serializable
data class MediaFile(
    val path: String,
    val type: String,
    val caption: String = "",
)

@Serializable
data class MediaFolderResponse(
    val folder: String,
    val totalFiles: Int,
    val images: List<MediaFile>,
    val videos: List<MediaFile>
)