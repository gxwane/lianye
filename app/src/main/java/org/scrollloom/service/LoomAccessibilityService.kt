package org.scrollloom.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.scrollloom.App
import org.scrollloom.engine.LoomEngine
import org.scrollloom.engine.OverlapMatcher
import org.scrollloom.engine.TileStore
import org.scrollloom.engine.model.LoomState
import org.scrollloom.service.capture.AccessibilityFrameCapturer
import org.scrollloom.service.gesture.AccessibilityGestureDispatcher
import org.scrollloom.service.gesture.AntiFlingController
import org.scrollloom.ui.floating.FloatingOverlayManager
import org.scrollloom.ui.main.MainActivity

class LoomAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var overlayManager: FloatingOverlayManager? = null
    private var loomEngine: LoomEngine? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "LoomAccessibilityService connected")
        val repository = App.instance.appComponent.loomRepository
        repository.updateServiceConnected(true)

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val gestureDispatcher = AccessibilityGestureDispatcher(this)
        val antiFlingController = AntiFlingController(gestureDispatcher)
        val overlapMatcher = OverlapMatcher()

        var overlay: FloatingOverlayManager? = null
        val frameCapturer = AccessibilityFrameCapturer(
            service = this,
            onPreCapture = {
                overlay?.hideBeforeCapture()
            },
            onPostCapture = {
                overlay?.showAfterCapture()
            }
        )

        val tileStore = TileStore(cacheDir)
        val engine = LoomEngine(overlapMatcher, antiFlingController, tileStore, frameCapturer)
        loomEngine = engine

        overlay = FloatingOverlayManager(
            context = this,
            windowManager = windowManager,
            repository = repository,
            scope = serviceScope,
            onStartCapture = {
                serviceScope.launch {
                    repository.startWeaving()
                    val tiles = engine.startWeaving()
                    if (engine.state.value == LoomState.COMPLETED) {
                        repository.finishWeaving(tiles)
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
        overlay.show()
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
        serviceScope.cancel()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "LoomAccessibilityService destroyed")
        App.instance.appComponent.loomRepository.updateServiceConnected(false)
        overlayManager?.hide()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "LoomAccessibility"
    }
}
