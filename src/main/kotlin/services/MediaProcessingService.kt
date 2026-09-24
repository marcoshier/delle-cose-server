package com.marcoshier.services

import com.marcoshier.lib.ConversionControl
import com.marcoshier.lib.sanitize
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger {  }

class MediaProcessingService : KoinComponent {
    private val mediaService by inject<MediaService>()
    private val progress by inject<MediaProgressService>()

    private val processingJobs = ConcurrentHashMap<String, Job>()
    private val controls = ConcurrentHashMap<String, ConversionControl>()

    fun processMedia(projectName: String, batch: Set<String>? = null) {
        cancelProcessing(projectName)

        val control = ConversionControl()
        controls[projectName] = control

        processingJobs[projectName] = CoroutineScope(Dispatchers.IO).launch {
            val self = coroutineContext[Job]
            try {
                logger.info { "Starting media processing for: $projectName" }
                mediaService.reencodeAllMediaForProject(projectName, control, batch)
                if (!control.isCancelled) mediaService.generateThumbnailsForProject(projectName, control)
                if (!control.isCancelled) mediaService.loadMediaInfo(projectName.sanitize())
                logger.info { "Completed media processing for: $projectName" }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error(e) { "Failed processing media for: $projectName" }
            } finally {
                processingJobs.remove(projectName, self)
                controls.remove(projectName, control)
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
        val control = controls[projectName]
        val job = processingJobs[projectName]
        if (control == null && job == null) return false

        control?.cancel()
        job?.cancel()
        progress.markAllCancelled(projectName)
        return true
    }

    fun cancelAllProcessing() {
        controls.keys.toList().forEach { cancelProcessing(it) }
    }
}