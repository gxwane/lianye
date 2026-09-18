package org.scrollloom.ui.floating

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.scrollloom.domain.repository.LoomRepository
import org.scrollloom.ui.common.theme.ScrollLoomTheme

class FloatingOverlayManager(
    private val context: Context,
    private val windowManager: WindowManager,
    private val repository: LoomRepository,
    private val scope: CoroutineScope,
    private val onStartCapture: () -> Unit,
    private val onStopCapture: () -> Unit,
    private val onOpenPreview: () -> Unit
) {
    private var composeView: ComposeView? = null
    private var lifecycleBridge: FloatingLifecycleBridge? = null
    private var isAttached = false

    private val windowParams = WindowManager.LayoutParams().apply {
        type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        format = PixelFormat.TRANSLUCENT
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        gravity = Gravity.TOP or Gravity.END
        x = 0
        y = 500
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    fun show() {
        if (isAttached) return

        val bridge = FloatingLifecycleBridge()
        lifecycleBridge = bridge

        val view = ComposeView(context).apply {
            setContent {
                ScrollLoomTheme {
                    val state by repository.weavingState.collectAsState()
                    LoomFloatingBubble(
                        state = state,
                        onStartClick = onStartCapture,
                        onStopClick = onStopCapture,
                        onPreviewClick = onOpenPreview
                    )
                }
            }
        }

        bridge.attach(view)
        windowManager.addView(view, windowParams)
        composeView = view
        isAttached = true
    }

    suspend fun hideBeforeCapture() {
        val view = composeView ?: return
        scope.launch(Dispatchers.Main) {
            view.alpha = 0.0f
        }.join()

        // Hybrid Timing Guard: At least 2 VSYNC intervals (40ms physical floor)
        delay(40L)
    }

    fun showAfterCapture() {
        val view = composeView ?: return
        scope.launch(Dispatchers.Main) {
            view.alpha = 1.0f
        }
    }

    fun hide() {
        if (!isAttached) return
        val view = composeView ?: return
        lifecycleBridge?.detach(view)
        lifecycleBridge = null
        windowManager.removeView(view)
        composeView = null
        isAttached = false
    }
}
