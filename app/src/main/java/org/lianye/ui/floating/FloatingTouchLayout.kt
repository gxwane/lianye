package org.lianye.ui.floating

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import kotlin.math.hypot

/**
 * 悬浮窗顶级物理手势与动量吸附容器：
 * 1. 在 onInterceptTouchEvent 中根据 ViewConfiguration.getScaledTouchSlop 识别拖拽与点击；
 * 2. 判定拖拽时拦截事件，并向子 View 分发 ACTION_CANCEL，防止内部按钮残留按压高亮；
 * 3. 拖拽过程中动态更新 WindowManager.LayoutParams.x / y，限制在屏幕安全区内；
 * 4. 手指释放时，基于瞬时速度（VelocityTracker）与屏幕中线，触发平滑的磁吸贴边物理动画；
 * 5. 吸附到位后触发系统微触觉反馈（Haptic Tick）；
 * 6. 空闲 5 秒后折叠，截图时保持结束入口可见；
 * 7. 在尺寸动态变化（折叠/展开）时，自适应保持右贴边对齐。
 */
class FloatingTouchLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var capturePixelsHidden = false
        set(value) {
            if (field != value) { field = value; invalidate() }
        }

    override fun dispatchDraw(canvas: Canvas) {
        // Root View.alpha is not applied by every software window drawing path.
        // Keep layout and input intact, while emitting a genuinely empty surface.
        if (!capturePixelsHidden) super.dispatchDraw(canvas)
    }

    var windowManager: WindowManager? = null
    var windowParams: WindowManager.LayoutParams? = null

    // 状态回调：isCollapsed, isDockedOnRight
    var onCollapseChange: ((Boolean, Boolean) -> Unit)? = null
    var onPositionChanged: ((Int, Int) -> Unit)? = null
    var allowCollapse = true
        set(value) {
            field = value
            if (!value) expandFromCollapse()
            resetCollapseTimer()
        }

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var velocityTracker: VelocityTracker? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    var isDockedOnRight = true
        private set
    var isCollapsed = false
        private set

    private val mainHandler = Handler(Looper.getMainLooper())
    private val collapseRunnable = Runnable {
        if (allowCollapse && !isDragging && !isCollapsed) {
            isCollapsed = true
            onCollapseChange?.invoke(true, isDockedOnRight)
        }
    }

    private var snapAnimator: ValueAnimator? = null

    init {
        resetCollapseTimer()
    }

    fun resetCollapseTimer() {
        mainHandler.removeCallbacks(collapseRunnable)
        if (allowCollapse) mainHandler.postDelayed(collapseRunnable, 5000L)
    }

    fun expandFromCollapse() {
        if (isCollapsed) {
            isCollapsed = false
            onCollapseChange?.invoke(false, isDockedOnRight)
            resetCollapseTimer()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                snapAnimator?.cancel()
                initialTouchX = ev.rawX
                initialTouchY = ev.rawY
                initialX = windowParams?.x ?: 0
                initialY = windowParams?.y ?: 0
                isDragging = false

                velocityTracker?.recycle()
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(ev)

                resetCollapseTimer()
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = ev.rawX - initialTouchX
                val dy = ev.rawY - initialTouchY
                if (hypot(dx.toDouble(), dy.toDouble()) > touchSlop) {
                    isDragging = true
                    if (isCollapsed) {
                        expandFromCollapse()
                    }
                    // 向子 View 派发 ACTION_CANCEL，取消其内部按压态
                    val cancelEvent = MotionEvent.obtain(ev).apply { action = MotionEvent.ACTION_CANCEL }
                    super.dispatchTouchEvent(cancelEvent)
                    cancelEvent.recycle()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                resetCollapseTimer()
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        velocityTracker?.addMovement(ev)
        val params = windowParams ?: return super.onTouchEvent(ev)
        val wm = windowManager ?: return super.onTouchEvent(ev)

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                resetCollapseTimer()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (ev.rawX - initialTouchX).toInt()
                val dy = (ev.rawY - initialTouchY).toInt()

                val screenBounds = getScreenBounds()
                val newX = (initialX + dx).coerceIn(screenBounds.leftInset, maxOf(screenBounds.leftInset, screenBounds.width - width - screenBounds.rightInset))
                val newY = (initialY + dy).coerceIn(screenBounds.topInset, maxOf(screenBounds.topInset, screenBounds.height - height - screenBounds.bottomInset))

                params.x = newX
                params.y = newY
                if (isAttachedToWindow) {
                    wm.updateViewLayout(this, params)
                }
                onPositionChanged?.invoke(newX, newY)
                resetCollapseTimer()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.computeCurrentVelocity(1000)
                val xVelocity = velocityTracker?.xVelocity ?: 0f
                velocityTracker?.recycle()
                velocityTracker = null

                if (isDragging) {
                    isDragging = false
                    snapToEdge(xVelocity)
                } else {
                    performClick()
                }
                resetCollapseTimer()
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isDockedOnRight && oldw != 0 && w != oldw) {
            val params = windowParams ?: return
            val wm = windowManager ?: return
            val screenBounds = getScreenBounds()
            params.x = (screenBounds.width - w).coerceAtLeast(0)
            if (isAttachedToWindow) {
                wm.updateViewLayout(this, params)
            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        expandFromCollapse()
        return true
    }

    fun clampPosition() {
        val params = windowParams ?: return
        val bounds = getScreenBounds()
        params.x = params.x.coerceIn(bounds.leftInset, maxOf(bounds.leftInset, bounds.width - width - bounds.rightInset))
        params.y = params.y.coerceIn(bounds.topInset, maxOf(bounds.topInset, bounds.height - height - bounds.bottomInset))
        if (isAttachedToWindow) windowManager?.updateViewLayout(this, params)
    }

    private data class ScreenBounds(val width: Int, val height: Int, val topInset: Int, val bottomInset: Int, val leftInset: Int, val rightInset: Int)

    private fun getScreenBounds(): ScreenBounds {
        val dm = resources.displayMetrics
        val bounds = if (android.os.Build.VERSION.SDK_INT >= 30) windowManager?.currentWindowMetrics?.bounds else null
        val width = bounds?.width() ?: dm.widthPixels
        val height = bounds?.height() ?: dm.heightPixels
        val density = dm.density
        val insets = if (android.os.Build.VERSION.SDK_INT >= 30) {
            windowManager?.currentWindowMetrics?.windowInsets?.getInsetsIgnoringVisibility(android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout())
        } else null
        @Suppress("DEPRECATION")
        val topInset = insets?.top ?: rootWindowInsets?.systemWindowInsetTop ?: (24 * density).toInt()
        @Suppress("DEPRECATION")
        val bottomInset = insets?.bottom ?: rootWindowInsets?.systemWindowInsetBottom ?: (48 * density).toInt()
        return ScreenBounds(width, height, maxOf(topInset, rootWindowInsets?.displayCutout?.safeInsetTop ?: 0), bottomInset,
            maxOf(insets?.left ?: 0, rootWindowInsets?.displayCutout?.safeInsetLeft ?: 0),
            maxOf(insets?.right ?: 0, rootWindowInsets?.displayCutout?.safeInsetRight ?: 0))
    }

    fun snapToEdge(xVelocity: Float) {
        val params = windowParams ?: return
        val wm = windowManager ?: return
        val screenBounds = getScreenBounds()

        val currentX = params.x
        val centerX = currentX + width / 2
        val screenCenterX = screenBounds.width / 2

        val snapToRight = when {
            xVelocity > 500f -> true
            xVelocity < -500f -> false
            else -> centerX >= screenCenterX
        }

        val targetX = if (snapToRight) {
            maxOf(screenBounds.leftInset, screenBounds.width - width - screenBounds.rightInset)
        } else {
            screenBounds.leftInset
        }

        isDockedOnRight = snapToRight

        snapAnimator?.cancel()
        snapAnimator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 260L
            interpolator = DecelerateInterpolator(1.5f)
            addUpdateListener { animator ->
                params.x = animator.animatedValue as Int
                if (isAttachedToWindow) {
                    wm.updateViewLayout(this@FloatingTouchLayout, params)
                }
                onPositionChanged?.invoke(params.x, params.y)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onCollapseChange?.invoke(isCollapsed, isDockedOnRight)
                    resetCollapseTimer()
                }
            })
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mainHandler.removeCallbacksAndMessages(null)
        snapAnimator?.cancel()
        velocityTracker?.recycle()
        velocityTracker = null
    }
}
