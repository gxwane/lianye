package org.scrollloom.ui.floating

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.annotation.MainThread
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.scrollloom.domain.model.WeavingState
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
    @Volatile
    private var isAttached = false
    private val mainHandler = Handler(Looper.getMainLooper())

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

    @MainThread
    fun show() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { show() }
            return
        }
        if (isAttached) return

        val bridge = FloatingLifecycleBridge()
        lifecycleBridge = bridge

        val view = ComposeView(context).apply {
            setContent {
                ScrollLoomTheme {
                    val state by repository.weavingState.collectAsState()
                    LaunchedEffect(state) {
                        this@apply.visibility = if (state is WeavingState.Preview) View.GONE else View.VISIBLE
                    }
                    if (state !is WeavingState.Preview) {
                        LoomFloatingBubble(
                            state = state,
                            onStartClick = onStartCapture,
                            onStopClick = onStopCapture,
                            onPreviewClick = onOpenPreview
                        )
                    }
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
            view.visibility = View.INVISIBLE
            windowParams.alpha = 0.0f
            if (isAttached && view.isAttachedToWindow) {
                windowManager.updateViewLayout(view, windowParams)
            }
        }.join()

        // Timing Guard: 80ms covers 5+ VSYNC frames for SurfaceFlinger transaction commit
        delay(80L)
    }

    @MainThread
    fun showAfterCapture() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { showAfterCapture() }
            return
        }
        val view = composeView ?: return
        view.visibility = View.VISIBLE
        windowParams.alpha = 1.0f
        if (isAttached && view.isAttachedToWindow) {
            windowManager.updateViewLayout(view, windowParams)
        }
    }

    @MainThread
    fun hide() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { hide() }
            return
        }
        if (!isAttached) return
        val view = composeView ?: return
        lifecycleBridge?.detach(view)
        lifecycleBridge = null
        windowManager.removeView(view)
        composeView = null
        isAttached = false
    }
}
