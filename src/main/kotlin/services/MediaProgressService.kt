package com.marcoshier.services

import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

@Serializable
enum class MediaStage { QUEUED, REENCODING, THUMBNAIL, DONE, FAILED, CANCELLED }

@Serializable
data class FileProgress(
    val fileName: String,
    val type: String,
    val stage: MediaStage,
    val percent: Int = 0,
    val error: String? = null,
    val inBatch: Boolean = true   // whether to surface this file in the widget
)

@Serializable
data class ProjectProgress(
    val projectName: String,
    val active: Boolean,
    val overallPercent: Int,
    val files: List<FileProgress>
)

class MediaProgressService {

    private val projects = ConcurrentHashMap<String, ConcurrentHashMap<String, FileProgress>>()

    private fun getOrPut(projectName: String) =
        projects.getOrPut(projectName) { ConcurrentHashMap() }

    fun start(projectName: String, files: List<Triple<String, String, Boolean>>) {
        val b = getOrPut(projectName)
        b.clear()
        for ((name, type, inBatch) in files) {
            b[name] = FileProgress(name, type, MediaStage.QUEUED, 0, inBatch = inBatch)
        }
    }

    fun stage(projectName: String, fileName: String, stage: MediaStage, percent: Int? = null) {
        val b = getOrPut(projectName)
        val prev = b[fileName]
        val pct = percent ?: when (stage) {
            MediaStage.QUEUED -> 0
            MediaStage.DONE -> 100
            else -> prev?.percent ?: 0
        }
        b[fileName] = prev?.copy(stage = stage, percent = pct.coerceIn(0, 100))
            ?: FileProgress(fileName, "image", stage, pct.coerceIn(0, 100))
    }

    fun percent(projectName: String, fileName: String, percent: Int) {
        val b = projects[projectName] ?: return
        val prev = b[fileName] ?: return
        b[fileName] = prev.copy(percent = percent.coerceIn(0, 100))
    }

    fun fail(projectName: String, fileName: String, error: String) {
        val b = getOrPut(projectName)
        val prev = b[fileName]
        b[fileName] = prev?.copy(stage = MediaStage.FAILED, error = error)
            ?: FileProgress(fileName, "image", MediaStage.FAILED, 0, error)
    }

    fun markAllCancelled(projectName: String) {
        val b = projects[projectName] ?: return
        for ((name, fp) in b) {
            if (fp.stage != MediaStage.DONE && fp.stage != MediaStage.FAILED) {
                b[name] = fp.copy(stage = MediaStage.CANCELLED)
            }
        }
    }

    fun snapshot(projectName: String): ProjectProgress? {
        val b = projects[projectName] ?: return null
        val files = b.values.filter { it.inBatch }.sortedBy { it.fileName }
        if (files.isEmpty()) return null
        val overall = files.sumOf { it.percent } / files.size
        val active = files.any {
            it.stage != MediaStage.DONE &&
                    it.stage != MediaStage.FAILED &&
                    it.stage != MediaStage.CANCELLED
        }
        return ProjectProgress(projectName, active, overall, files)
    }
}