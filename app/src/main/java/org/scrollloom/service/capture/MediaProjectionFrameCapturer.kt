package org.scrollloom.service.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import org.scrollloom.engine.FrameCapturer
import org.scrollloom.engine.model.PixelSlice

/**
 * Android 10 (API 29) Screen Capturer backed by MediaProjection & VirtualDisplay.
 *
 * Implements Skia SIMD row stride alignment and color swizzle remediation for
 * devices enforcing non-standard row padding (e.g. Kirin 710F Mali-G51 64-byte alignment).
 */
class MediaProjectionFrameCapturer(
    private val context: Context,
    var onPreCapture: (suspend () -> Unit)? = null,
    var onPostCapture: (() -> Unit)? = null
) : FrameCapturer {

    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureHandlerThread: HandlerThread? = null
    private var reusableBitmap: Bitmap? = null
    private val frameSignal = Channel<Unit>(Channel.CONFLATED)

    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0

    init {
        updateScreenMetrics()
    }

    private fun updateScreenMetrics() {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi
    }

    override fun prepareSession() {
        if (virtualDisplay != null) return

        val projection = MediaProjectionHolder.get()
        if (projection == null) {
            Log.e(TAG, "prepareSession failed: MediaProjection is not available")
            return
        }

        updateScreenMetrics()
        if (screenWidth <= 0 || screenHeight <= 0) {
            Log.e(TAG, "prepareSession failed: Invalid screen dimensions ($screenWidth x $screenHeight)")
            return
        }

        val thread = HandlerThread("ScrollLoom_Capture").apply { start() }
        captureHandlerThread = thread
        val handler = Handler(thread.looper)

        val reader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
        reader.setOnImageAvailableListener({
            frameSignal.trySend(Unit)
        }, handler)
        imageReader = reader

        val flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
        virtualDisplay = projection.createVirtualDisplay(
            "ScrollLoom_VD",
            screenWidth,
            screenHeight,
            screenDensity,
            flags,
            reader.surface,
            null,
            handler
        )
        Log.i(TAG, "VirtualDisplay session created: ${screenWidth}x$screenHeight @ ${screenDensity}dpi")
    }

    override suspend fun captureFrame(): PixelSlice? {
        val reader = imageReader ?: run {
            Log.e(TAG, "captureFrame failed: ImageReader is null (session not prepared)")
            return null
        }

        onPreCapture?.invoke()
        try {
            var image = reader.acquireLatestImage()
            if (image == null) {
                withTimeoutOrNull(600L) {
                    frameSignal.receive()
                }
                image = reader.acquireLatestImage()
            }

            if (image == null) {
                Log.w(TAG, "captureFrame timed out waiting for display buffer")
                return null
            }

            return processImage(image)
        } finally {
            onPostCapture?.invoke()
        }
    }

    private fun processImage(image: Image): PixelSlice? {
        try {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val paddedWidth = rowStride / pixelStride
            val imgWidth = image.width
            val imgHeight = image.height

            var bmp = reusableBitmap
            if (bmp == null || bmp.width != paddedWidth || bmp.height != imgHeight || bmp.isRecycled) {
                bmp?.recycle()
                bmp = Bitmap.createBitmap(paddedWidth, imgHeight, Bitmap.Config.ARGB_8888)
                reusableBitmap = bmp
            }

            buffer.rewind()
            bmp.copyPixelsFromBuffer(buffer)

            val cleanPixels = IntArray(imgWidth * imgHeight)
            bmp.getPixels(cleanPixels, 0, imgWidth, 0, 0, imgWidth, imgHeight)
            return PixelSlice(cleanPixels, imgWidth, imgHeight)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process image buffer", e)
            return null
        } finally {
            image.close()
        }
    }

    override fun releaseSession() {
        try {
            virtualDisplay?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing VirtualDisplay", e)
        }
        virtualDisplay = null

        try {
            imageReader?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing ImageReader", e)
        }
        imageReader = null

        captureHandlerThread?.quitSafely()
        captureHandlerThread = null

        reusableBitmap?.recycle()
        reusableBitmap = null
        Log.i(TAG, "VirtualDisplay session released")
    }

    override fun release() {
        releaseSession()
    }

    companion object {
        private const val TAG = "MediaProjCapturer"
    }
}
