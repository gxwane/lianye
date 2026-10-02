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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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
    private var touchLayout: FloatingTouchLayout? = null
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
        gravity = Gravity.TOP or Gravity.START
        val dm = context.resources.displayMetrics
        x = dm.widthPixels - (120 * dm.density).toInt()
        y = (200 * dm.density).toInt()
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

        val layout = FloatingTouchLayout(context).apply {
            this.windowManager = this@FloatingOverlayManager.windowManager
            this.windowParams = this@FloatingOverlayManager.windowParams
        }

        var collapseStateListener: ((Boolean, Boolean) -> Unit)? = null

        val view = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                ScrollLoomTheme {
                    val state by repository.weavingState.collectAsState()
                    var isCollapsed by remember { mutableStateOf(false) }
                    var isDockedOnRight by remember { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        collapseStateListener = { collapsed, dockedRight ->
                            isCollapsed = collapsed
                            isDockedOnRight = dockedRight
                        }
                    }

                    LaunchedEffect(state) {
                        layout.visibility = if (state is WeavingState.Preview) View.GONE else View.VISIBLE
                        // 开始录制时，自动展开并重置空闲折叠计时器
                        if (state is WeavingState.Weaving) {
                            layout.expandFromCollapse()
                        }
                    }

                    if (state !is WeavingState.Preview) {
                        LoomFloatingBubble(
                            state = state,
                            isCollapsed = isCollapsed,
                            isDockedOnRight = isDockedOnRight,
                            onExpandRequest = { layout.expandFromCollapse() },
                            onStartClick = {
                                layout.resetCollapseTimer()
                                onStartCapture()
                            },
                            onStopClick = {
                                layout.resetCollapseTimer()
                                onStopCapture()
                            },
                            onPreviewClick = {
                                layout.resetCollapseTimer()
                                onOpenPreview()
                            }
                        )
                    }
                }
            }
        }

        layout.onCollapseChange = { collapsed, dockedRight ->
            collapseStateListener?.invoke(collapsed, dockedRight)
        }

        layout.addView(view)
        bridge.attach(root = layout, child = view)
        windowManager.addView(layout, windowParams)
        touchLayout = layout
        composeView = view
        isAttached = true
    }

    suspend fun hideBeforeCapture() {
        val layout = touchLayout ?: return
        scope.launch(Dispatchers.Main) {
            layout.visibility = View.INVISIBLE
            windowParams.alpha = 0.0f
            if (isAttached && layout.isAttachedToWindow) {
                windowManager.updateViewLayout(layout, windowParams)
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
        val layout = touchLayout ?: return
        layout.visibility = View.VISIBLE
        windowParams.alpha = 1.0f
        if (isAttached && layout.isAttachedToWindow) {
            windowManager.updateViewLayout(layout, windowParams)
        }
    }

    @MainThread
    fun hide() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { hide() }
            return
        }
        if (!isAttached) return
        val layout = touchLayout ?: return
        val view = composeView
        if (view != null) {
            lifecycleBridge?.detach(root = layout, composeView = view)
        }
        lifecycleBridge = null
        try {
            windowManager.removeView(layout)
        } catch (e: Exception) {
            // Safe removal
        }
        touchLayout = null
        composeView = null
        isAttached = false
    }
}
