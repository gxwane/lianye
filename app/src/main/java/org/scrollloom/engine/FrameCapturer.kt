package org.scrollloom.engine

import org.scrollloom.engine.model.PixelSlice

/**
 * Pure Kotlin contract for frame acquisition, decoupled from Android Framework specifics.
 */
interface FrameCapturer {
    suspend fun captureFrame(): PixelSlice?
    fun prepareSession() {}
    fun releaseSession() {}
    fun release() {}
}
