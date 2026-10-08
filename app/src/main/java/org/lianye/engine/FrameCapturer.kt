package org.lianye.engine

import org.lianye.engine.model.PixelSlice
import org.lianye.domain.model.CaptureCompletion

/**
 * Pure Kotlin contract for frame acquisition, decoupled from Android Framework specifics.
 */
interface FrameCapturer {
    val failureReason: CaptureCompletion get() = CaptureCompletion.PERMISSION_LOST
    suspend fun captureFrame(): PixelSlice?
    /** Raw samples may contain controls. They are used for motion/stability only. */
    suspend fun sampleFrame(): FrameSample = captureFrame()?.let(FrameSample::Available)
        ?: FrameSample.Failed(failureReason)

    /** A candidate is committed only after acquisition without our own controls. */
    suspend fun captureCleanFrame(candidate: PixelSlice): FrameSample = FrameSample.Available(candidate)
    fun prepareSession() {}
    fun releaseSession() {}
    fun release() {}
}

sealed interface FrameSample {
    data class Available(val frame: PixelSlice) : FrameSample
    /** An unchanged display or a temporary empty ImageReader is not a lost permission. */
    data object NoNewFrame : FrameSample
    data class Failed(val reason: CaptureCompletion) : FrameSample
}
