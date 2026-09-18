package org.scrollloom.service.capture

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.view.Display
import kotlinx.coroutines.suspendCancellableCoroutine
import org.scrollloom.engine.FrameCapturer
import org.scrollloom.engine.model.PixelSlice
import java.util.concurrent.Executors
import kotlin.coroutines.resume

class AccessibilityFrameCapturer(
    private val service: AccessibilityService,
    var onPreCapture: (suspend () -> Unit)? = null,
    var onPostCapture: (() -> Unit)? = null
) : FrameCapturer {

    private val executor = Executors.newSingleThreadExecutor()

    override suspend fun captureFrame(): PixelSlice? {
        onPreCapture?.invoke()
        try {
            return suspendCancellableCoroutine { continuation ->
                service.takeScreenshot(
                    Display.DEFAULT_DISPLAY,
                    executor,
                    object : AccessibilityService.TakeScreenshotCallback {
                        override fun onSuccess(result: AccessibilityService.ScreenshotResult) {
                            val slice = extractPixelSlice(result)
                            if (continuation.isActive) {
                                continuation.resume(slice)
                            }
                        }

                        override fun onFailure(errorCode: Int) {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }
                    }
                )
            }
        } finally {
            onPostCapture?.invoke()
        }
    }

    private fun extractPixelSlice(result: AccessibilityService.ScreenshotResult): PixelSlice? {
        val buffer = result.hardwareBuffer
        val colorSpace = result.colorSpace
        return try {
            val hwBitmap = Bitmap.wrapHardwareBuffer(buffer, colorSpace) ?: return null
            val w = hwBitmap.width
            val h = hwBitmap.height
            val pixels = IntArray(w * h)
            val softBitmap = hwBitmap.copy(Bitmap.Config.ARGB_8888, false)
            softBitmap.getPixels(pixels, 0, w, 0, 0, w, h)
            softBitmap.recycle()
            hwBitmap.recycle()
            PixelSlice(pixels, w, h)
        } finally {
            buffer.close()
        }
    }
}
