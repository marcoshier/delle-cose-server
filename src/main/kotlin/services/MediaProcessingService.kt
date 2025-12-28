package com.marcoshier.services

import com.marcoshier.lib.sanitize
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger {  }

class MediaProcessingService : KoinComponent {
    private val mediaService by inject<MediaService>()

    private val processingJobs = mutableMapOf<String, Job>()

    fun processMedia(projectName: String) {
        processingJobs[projectName]?.cancel()

        processingJobs[projectName] = CoroutineScope(Dispatchers.IO).launch {
            try {
                logger.info { "Starting media processing for: $projectName" }

                mediaService.run {
                    reencodeAllMediaForProject(projectName)
                    generateThumbnailsForProject(projectName)
                    loadMediaInfo(projectName.sanitize())
                }

                logger.info { "Completed media processing for: $projectName" }
            } catch (e: CancellationException) {
                logger.info { "Media processing cancelled for: $projectName" }
                throw e
            } catch (e: Exception) {
                logger.error(e) { "Failed processing media for: $projectName" }
            } finally {
                processingJobs.remove(projectName)
            }
        }
    }

    fun isProcessing(projectName: String): Boolean {
        return processingJobs[projectName]?.isActive == true
    }

    fun getAllProcessingProjects(): List<String> {
        return processingJobs.filter { it.value.isActive }.keys.toList()
    }

    fun cancelProcessing(projectName: String): Boolean {
        return processingJobs[projectName]?.let { job ->
            job.cancel()
            true
        } ?: false
    }

    fun cancelAllProcessing() {
        processingJobs.values.forEach { it.cancel() }
        processingJobs.clear()
    }
}