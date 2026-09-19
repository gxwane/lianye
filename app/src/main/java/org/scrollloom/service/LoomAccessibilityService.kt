package org.scrollloom.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.scrollloom.App
import org.scrollloom.domain.model.WeavingState
import org.scrollloom.engine.FrameCapturer
import org.scrollloom.engine.LoomEngine
import org.scrollloom.engine.OverlapMatcher
import org.scrollloom.engine.TileStore
import org.scrollloom.engine.model.LoomState
import org.scrollloom.service.capture.FrameCapturerFactory
import org.scrollloom.service.gesture.AccessibilityGestureDispatcher
import org.scrollloom.service.gesture.AntiFlingController
import org.scrollloom.ui.floating.FloatingOverlayManager
import org.scrollloom.ui.main.MainActivity

class LoomAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var overlayManager: FloatingOverlayManager? = null
    private var loomEngine: LoomEngine? = null
    private var frameCapturer: FrameCapturer? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "LoomAccessibilityService connected")
        val repository = App.instance.appComponent.loomRepository
        repository.updateServiceConnected(true)

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val gestureDispatcher = AccessibilityGestureDispatcher(this)
        val antiFlingController = AntiFlingController(gestureDispatcher)
        val density = resources.displayMetrics.density
        val overlapMatcher = OverlapMatcher(screenDensity = density)

        var overlay: FloatingOverlayManager? = null
        val capturer = FrameCapturerFactory.create(
            service = this,
            onPreCapture = {
                overlay?.hideBeforeCapture()
            },
            onPostCapture = {
                overlay?.showAfterCapture()
            }
        )
        frameCapturer = capturer

        val tileStore = TileStore(cacheDir)
        val engine = LoomEngine(overlapMatcher, antiFlingController, tileStore, capturer)
        loomEngine = engine

        overlay = FloatingOverlayManager(
            context = this,
            windowManager = windowManager,
            repository = repository,
            scope = serviceScope,
            onStartCapture = {
                if (repository.weavingState.value is WeavingState.Weaving) return@FloatingOverlayManager
                serviceScope.launch(Dispatchers.Default) {
                    repository.startWeaving()
                    val tiles = engine.startWeaving()
                    if (engine.state.value == LoomState.COMPLETED) {
                        repository.finishWeaving(tiles)
                        val intent = Intent(this@LoomAccessibilityService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                    } else {
                        repository.failWeaving("长截图未完成", tiles)
                    }
                }
            },
            onStopCapture = {
                engine.stop()
            },
            onOpenPreview = {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
        )

        overlayManager = overlay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            overlay.show()
        } else {
            // Android 10 (API 29): Only show overlay once MediaProjection permission is granted
            // to avoid Huawei EMUI "Screen overlay detected" tapjacking security dialog block
            serviceScope.launch {
                repository.isProjectionGranted.collect { granted ->
                    if (granted) {
                        overlay.show()
                    } else {
                        overlay.hide()
                    }
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // STRICT INVARIANT: Blind service does not process any window events
    }

    override fun onInterrupt() {
        Log.w(TAG, "LoomAccessibilityService interrupted")
        App.instance.appComponent.loomRepository.updateServiceConnected(false)
        overlayManager?.hide()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "LoomAccessibilityService unbind")
        App.instance.appComponent.loomRepository.updateServiceConnected(false)
        overlayManager?.hide()
        frameCapturer?.release()
        frameCapturer = null
        serviceScope.cancel()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "LoomAccessibilityService destroyed")
        App.instance.appComponent.loomRepository.updateServiceConnected(false)
        overlayManager?.hide()
        frameCapturer?.release()
        frameCapturer = null
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "LoomAccessibility"
    }
}
