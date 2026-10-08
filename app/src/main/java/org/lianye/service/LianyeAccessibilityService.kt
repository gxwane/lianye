package org.lianye.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import org.lianye.App
import org.lianye.domain.model.CaptureCompletion
import org.lianye.domain.model.CaptureMode
import org.lianye.domain.model.ManualPhase
import org.lianye.engine.*
import org.lianye.engine.model.LianyeState
import org.lianye.service.capture.FrameCapturerFactory
import org.lianye.service.gesture.AccessibilityGestureDispatcher
import org.lianye.service.gesture.AntiFlingController
import org.lianye.ui.floating.FloatingOverlayManager
import org.lianye.ui.main.MainActivity
import java.lang.ref.WeakReference

class LianyeAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var overlayManager: FloatingOverlayManager? = null
    private var lianyeEngine: LianyeEngine? = null
    private var frameCapturer: FrameCapturer? = null
    private var captureJob: Job? = null
    private var serviceBound = false
    private val repository get() = App.instance.appComponent.lianyeRepository

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceBound = true
        instance = WeakReference(this)
        repository.updateServiceConnected(true)
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        var overlay: FloatingOverlayManager? = null
        val capturer = FrameCapturerFactory.create(this,
            { overlay?.hideBeforeCapture() ?: if (overlay == null) 0L else null },
            { withContext(Dispatchers.Main.immediate) { overlay?.showAfterCapture() } })
        frameCapturer = capturer
        val store = TileStore(App.instance.appComponent.draftStore.tilesDirectory, onCheckpoint = {
            repository.checkpointDraft(it)
            if (repository.lastPersistenceError.value != null) throw java.io.IOException("Draft checkpoint failed")
        })
        val engine = LianyeEngine(OverlapMatcher(screenDensity = resources.displayMetrics.density), AntiFlingController(AccessibilityGestureDispatcher(this)), store, capturer)
        lianyeEngine = engine
        overlay = FloatingOverlayManager(this, wm, repository, serviceScope,
            onStartCapture = capture@ {
                if (repository.captureMode.value != CaptureMode.AUTO || repository.manualSession.value.phase != ManualPhase.IDLE) return@capture
                if (repository.draft.value?.isSaved == false) openPreview()
                else if (captureJob?.isActive != true) {
                    captureJob = serviceScope.launch(Dispatchers.IO) {
                        if (repository.draft.value != null && !repository.discardDraft()) return@launch
                        if (!repository.startWeaving()) return@launch
                        val tiles = engine.startWeaving(onProgress = { repository.updateProgress(it.frameCount, it.stitchedHeightPx, it.viewportHeightPx) })
                        if (engine.state.value == LianyeState.COMPLETED) repository.finishWeaving(tiles, engine.completionReason)
                        else repository.failWeaving(engine.failureMessage ?: "capture_interrupted", tiles, engine.completionReason)
                        withContext(Dispatchers.Main) { openPreview() }
                    }
                }
            },
            onStopCapture = { engine.stop() },
            onOpenPreview = { openPreview() }
        )
        overlayManager = overlay
        overlay.setPreviewVisible(previewVisible)
        serviceScope.launch {
            combine(repository.isProjectionGranted, repository.captureMode, repository.manualSession) { granted, mode, manual ->
                granted && mode == CaptureMode.AUTO && manual.phase == ManualPhase.IDLE
            }.collect { granted ->
                if (!granted && captureJob?.isActive == true) engine.stop(CaptureCompletion.PERMISSION_LOST)
                if (granted && getSharedPreferences("overlay", MODE_PRIVATE).getBoolean("enabled", true)) overlay.show()
                else overlay.hide()
            }
        }
    }

    private fun openPreview() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_draft", true)
        }
        try { startActivity(intent) } catch (_: RuntimeException) { /* Result remains available in the floating controls. */ }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() {
        // Feedback interruption does not unbind the service or revoke permissions.
        lianyeEngine?.stop(CaptureCompletion.INTERRUPTED)
        overlayManager?.hide()
    }

    private fun releaseResources() {
        serviceBound = false
        lianyeEngine?.stop(CaptureCompletion.PERMISSION_LOST)
        captureJob?.cancel()
        repository.updateServiceConnected(false)
        overlayManager?.hide()
        frameCapturer?.release()
        frameCapturer = null
        serviceScope.cancel()
        instance = null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        releaseResources()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        releaseResources()
        super.onDestroy()
    }

    companion object {
        private var instance: WeakReference<LianyeAccessibilityService>? = null
        private var previewVisible = false
        fun isConnected() = instance?.get()?.serviceBound == true
        fun showControls(): Boolean {
            val service = instance?.get() ?: return false
            if (service.repository.captureMode.value != CaptureMode.AUTO || service.repository.manualSession.value.phase != ManualPhase.IDLE) return false
            if (!service.repository.isProjectionGranted.value) return false
            service.getSharedPreferences("overlay", MODE_PRIVATE).edit().putBoolean("enabled", true).apply()
            service.overlayManager?.show()
            return true
        }
        fun hideControls() {
            instance?.get()?.let { service ->
                service.getSharedPreferences("overlay", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
                service.overlayManager?.hide()
            }
        }
        fun setPreviewVisible(visible: Boolean) {
            previewVisible = visible
            instance?.get()?.overlayManager?.setPreviewVisible(visible)
        }
        fun disableCurrentService(): Boolean {
            val service = instance?.get() ?: return false
            service.lianyeEngine?.stop()
            return try { service.disableSelf(); true } catch (_: RuntimeException) { false }
        }
    }
}
