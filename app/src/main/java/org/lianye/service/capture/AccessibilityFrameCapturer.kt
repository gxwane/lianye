package org.lianye.service.capture

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.view.Display
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.lianye.engine.FrameCapturer
import org.lianye.engine.model.PixelSlice
import org.lianye.domain.model.CaptureCompletion
import java.util.concurrent.Executors
import kotlin.coroutines.resume

@RequiresApi(Build.VERSION_CODES.R)
class AccessibilityFrameCapturer(
    private val service: AccessibilityService,
    var onPreCapture: (suspend () -> Long?)? = null,
    var onPostCapture: (suspend () -> Unit)? = null
) : FrameCapturer {

    override var failureReason = CaptureCompletion.PERMISSION_LOST
        private set

    private val executor = Executors.newSingleThreadExecutor()

    override suspend fun captureFrame(): PixelSlice? {
        try {
            if (onPreCapture?.let { it() == null } == true) {
                failureReason = CaptureCompletion.INTERRUPTED
                return null
            }
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
                            failureReason = if (Build.VERSION.SDK_INT >= 34 && errorCode == AccessibilityService.ERROR_TAKE_SCREENSHOT_SECURE_WINDOW) {
                                CaptureCompletion.SECURE_BLOCKED
                            } else CaptureCompletion.PERMISSION_LOST
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }
                    }
                )
            }
        } finally {
            withContext(NonCancellable) { onPostCapture?.invoke() }
        }
    }

    private fun extractPixelSlice(result: AccessibilityService.ScreenshotResult): PixelSlice? {
        val buffer = result.hardwareBuffer
        val colorSpace = result.colorSpace
        var hwBitmap: Bitmap? = null
        var softBitmap: Bitmap? = null
        return try {
            hwBitmap = Bitmap.wrapHardwareBuffer(buffer, colorSpace) ?: return null
            val w = hwBitmap.width
            val h = hwBitmap.height
            val pixels = IntArray(w * h)
            softBitmap = hwBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return null
            softBitmap.getPixels(pixels, 0, w, 0, 0, w, h)
            PixelSlice(pixels, w, h)
        } catch (t: Throwable) {
            null
        } finally {
            softBitmap?.recycle()
            hwBitmap?.recycle()
            buffer.close()
        }
    }

    override fun release() {
        executor.shutdown()
    }
}
