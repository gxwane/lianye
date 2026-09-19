package org.scrollloom.engine

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import org.scrollloom.engine.model.LoomState
import org.scrollloom.engine.model.PixelSlice
import org.scrollloom.engine.model.TileMetadata
import org.scrollloom.service.gesture.AntiFlingController
import org.scrollloom.service.gesture.model.ControlledScrollCommand

class LoomEngine(
    private val overlapMatcher: OverlapMatcher,
    private val antiFlingController: AntiFlingController,
    private val tileStore: TileStore,
    private val frameCapturer: FrameCapturer,
    private val varianceMaskEngine: TemporalVarianceMask = TemporalVarianceMask(),
    val screenDensity: Float = overlapMatcher.screenDensity
) {
    private val _state = MutableStateFlow(LoomState.IDLE)
    val state: StateFlow<LoomState> = _state.asStateFlow()

    private val weaveMutex = Mutex()
    private val frameWindow = mutableListOf<PixelSlice>()
    private var priorDeltaY: Int? = null

    @Volatile
    private var isStopRequested = false

    suspend fun startWeaving(
        maxFrames: Int = 50,
        scrollCommand: ControlledScrollCommand? = null
    ): List<TileMetadata> {
        if (!weaveMutex.tryLock()) {
            return emptyList()
        }

        try {
            frameCapturer.prepareSession()
            tileStore.clear()
            frameWindow.clear()
            priorDeltaY = null
            isStopRequested = false
            _state.value = LoomState.CAPTURING

            var prevSlice: PixelSlice? = null
            var initialFrame: PixelSlice? = null
            var isInitialFrameWritten = false
            var bottomBarHeight = 0
            var hasStitchedAny = false
            var consecutiveStationaryCount = 0
            var consecutiveDynamicCount = 0

            val captured0 = frameCapturer.captureFrame() ?: run {
                Log.e(TAG, "captureFrame failed for initial frame")
                _state.value = LoomState.ERROR
                return emptyList()
            }
            Log.i(TAG, "Initial frame captured: ${captured0.width}x${captured0.height}")
            initialFrame = captured0
            frameWindow.add(captured0)
            prevSlice = captured0

            // Condition 1: Adaptive gesture coordinate mapping with double-clamped stroke
            val effectiveCommand = scrollCommand ?: run {
                val w = captured0.width.toFloat()
                val h = captured0.height.toFloat()
                val strokeDist = minOf(h * 0.35f, 280.0f * screenDensity)
                val centerX = w * 0.50f
                val centerY = h * 0.50f
                ControlledScrollCommand(
                    startX = centerX,
                    startY = centerY + strokeDist / 2.0f,
                    endX = centerX,
                    endY = centerY - strokeDist / 2.0f,
                    moveDurationMs = 550L,
                    holdDurationMs = 150L
                )
            }

            for (frameIndex in 1 until maxFrames) {
                if (isStopRequested) {
                    Log.i(TAG, "Stop requested at frame $frameIndex")
                    _state.value = LoomState.STOPPING
                    break
                }
                val currentPrev = prevSlice ?: break
                Log.d(TAG, "Processing frame $frameIndex...")
                when (val step = processFrameStep(
                    prevSlice = currentPrev,
                    command = effectiveCommand,
                    frameIndex = frameIndex,
                    hasStitchedAny = hasStitchedAny,
                    initialFrame = initialFrame,
                    isInitialFrameWritten = isInitialFrameWritten,
                    bottomBarHeight = bottomBarHeight,
                    consecutiveStationaryCount = consecutiveStationaryCount,
                    consecutiveDynamicCount = consecutiveDynamicCount,
                    onInitialFrameWritten = { detectedHeight ->
                        isInitialFrameWritten = true
                        bottomBarHeight = detectedHeight
                    },
                    onCountersUpdated = { statCount, dynCount ->
                        consecutiveStationaryCount = statCount
                        consecutiveDynamicCount = dynCount
                    }
                )) {
                    is FrameStepResult.Continue -> {
                        prevSlice = step.nextSlice
                        hasStitchedAny = true
                    }
                    is FrameStepResult.Completed -> {
                        if (step.finalSlice != null) {
                            prevSlice = step.finalSlice
                        }
                        hasStitchedAny = true
                        _state.value = LoomState.COMPLETED
                        break
                    }
                    is FrameStepResult.Error -> {
                        _state.value = LoomState.ERROR
                        break
                    }
                    is FrameStepResult.Stop -> {
                        if (!hasStitchedAny) {
                            Log.e(TAG, "Weaving stopped at frame $frameIndex without stitching any new frames! Setting ERROR state.")
                            _state.value = LoomState.ERROR
                        } else {
                            _state.value = LoomState.COMPLETED
                        }
                        break
                    }
                }
            }

            if (_state.value != LoomState.ERROR && _state.value != LoomState.COMPLETED) {
                _state.value = if (hasStitchedAny) LoomState.COMPLETED else LoomState.ERROR
            }

            // Deferred initial frame fallback (if never written, e.g. single frame or error)
            if (!isInitialFrameWritten) {
                tileStore.appendStrip(initialFrame)
                isInitialFrameWritten = true
            } else if (bottomBarHeight > 0) {
                val lastSlice = prevSlice ?: initialFrame
                // Append fixed bottom bar once from the latest captured slice
                val footerStrip = lastSlice.crop(lastSlice.height - bottomBarHeight, bottomBarHeight)
                tileStore.appendStrip(footerStrip)
                Log.i(TAG, "Appended final bottom bar of height $bottomBarHeight")
            }

            val tiles = tileStore.finalizeTiles()
            Log.i(TAG, "Weaving finalized: state=${_state.value}, hasStitchedAny=$hasStitchedAny, tileCount=${tiles.size}")
            return tiles
        } catch (e: Exception) {
            Log.e(TAG, "Exception during weaving", e)
            _state.value = LoomState.ERROR
            antiFlingController.cancelCurrentGesture()
            tileStore.clear()
            throw e
        } finally {
            frameCapturer.releaseSession()
            weaveMutex.unlock()
        }
    }

    fun stop() {
        isStopRequested = true
    }

    fun detectBottomBarHeight(
        frame0: PixelSlice,
        frame1: PixelSlice,
        deltaY: Int
    ): Int {
        if (deltaY <= 0) return 0
        val w = minOf(frame0.width, frame1.width)
        val h = minOf(frame0.height, frame1.height)
        val maxFooterH = minOf(h / 4, 600)
        val startX = (w * 0.1f).toInt()
        val endX = (w * 0.9f).toInt()

        var detectedFooterH = 0
        var consecutiveMotionRows = 0

        for (offset in 0 until maxFooterH) {
            val y = h - 1 - offset
            val yShifted = y - deltaY
            if (yShifted < 0) break

            var staticDiffSum = 0L
            var motionDiffSum = 0L
            var samples = 0

            for (x in startX until endX step 4) {
                val p0 = frame0.getPixel(x, y)
                val p1 = frame1.getPixel(x, y)
                val p1Shifted = frame1.getPixel(x, yShifted)

                val l0 = ((77 * ((p0 ushr 16) and 0xFF) + 150 * ((p0 ushr 8) and 0xFF) + 29 * (p0 and 0xFF)) ushr 8)
                val l1 = ((77 * ((p1 ushr 16) and 0xFF) + 150 * ((p1 ushr 8) and 0xFF) + 29 * (p1 and 0xFF)) ushr 8)
                val l1s = ((77 * ((p1Shifted ushr 16) and 0xFF) + 150 * ((p1Shifted ushr 8) and 0xFF) + 29 * (p1Shifted and 0xFF)) ushr 8)

                staticDiffSum += kotlin.math.abs(l0 - l1)
                motionDiffSum += kotlin.math.abs(l0 - l1s)
                samples++
            }

            val rStatic = staticDiffSum.toFloat() / maxOf(1, samples)
            val rMotion = motionDiffSum.toFloat() / maxOf(1, samples)

            val isStaticRow = (rStatic <= 8.0f && rMotion > 12.0f)
            if (isStaticRow) {
                detectedFooterH = offset + 1
                consecutiveMotionRows = 0
            } else if (rMotion < rStatic && rMotion <= 10.0f) {
                consecutiveMotionRows++
                if (consecutiveMotionRows >= 12) {
                    break
                }
            }
        }

        val minBottomBarPx = maxOf(20, (24.0f * screenDensity).toInt())
        return if (detectedFooterH >= minBottomBarPx) detectedFooterH else 0
    }

    private suspend fun processFrameStep(
        prevSlice: PixelSlice,
        command: ControlledScrollCommand,
        frameIndex: Int,
        hasStitchedAny: Boolean,
        initialFrame: PixelSlice?,
        isInitialFrameWritten: Boolean,
        bottomBarHeight: Int,
        consecutiveStationaryCount: Int,
        consecutiveDynamicCount: Int,
        onInitialFrameWritten: (Int) -> Unit,
        onCountersUpdated: (Int, Int) -> Unit
    ): FrameStepResult {
        if (!antiFlingController.executeControlledScroll(command)) {
            Log.e(TAG, "executeControlledScroll returned false at frame $frameIndex")
            return FrameStepResult.Stop
        }

        // Settling delay: allow target app VSYNC layout, redraw and overscroll spring bounce to settle
        delay(350L)

        _state.value = LoomState.CAPTURING
        val nextSlice = frameCapturer.captureFrame() ?: run {
            Log.e(TAG, "captureFrame returned null at frame $frameIndex")
            return FrameStepResult.Stop
        }

        _state.value = LoomState.WEAVING
        frameWindow.add(nextSlice)
        if (frameWindow.size > 3) {
            frameWindow.removeAt(0)
        }

        val rowMask = if (frameWindow.size == 3) {
            varianceMaskEngine.computeRowMask(frameWindow[0], frameWindow[1], frameWindow[2])
        } else {
            null
        }

        val matchResult = overlapMatcher.match(
            prevSlice = prevSlice,
            nextSlice = nextSlice,
            rowMask = rowMask,
            priorDeltaY = priorDeltaY
        )

        Log.d(TAG, "Frame $frameIndex matchResult: deltaY=${matchResult.deltaY}, isBottomReached=${matchResult.isBottomReached}, isStationary=${matchResult.isStationary}, isDynamicScene=${matchResult.isDynamicScene}, isSecureBlocked=${matchResult.isSecureBlocked}, sadScore=${matchResult.sadScore}, ambiguityRatio=${matchResult.ambiguityRatio}")

        if (matchResult.deltaY > 0) {
            priorDeltaY = matchResult.deltaY
        }

        if (matchResult.isSecureBlocked) return FrameStepResult.Error

        val newStatCount = if (matchResult.isStationary) consecutiveStationaryCount + 1 else 0
        val newDynCount = if (matchResult.isDynamicScene) consecutiveDynamicCount + 1 else 0
        onCountersUpdated(newStatCount, newDynCount)

        var currentBottomBarH = bottomBarHeight
        if (matchResult.deltaY > 0 && !isInitialFrameWritten && initialFrame != null) {
            currentBottomBarH = detectBottomBarHeight(initialFrame, nextSlice, matchResult.deltaY)
            val bodyHeight = initialFrame.height - currentBottomBarH
            if (bodyHeight > 0) {
                tileStore.appendStrip(initialFrame.crop(0, bodyHeight))
            }
            onInitialFrameWritten(currentBottomBarH)
            Log.i(TAG, "Initial frame written with bodyHeight=$bodyHeight, bottomBarHeight=$currentBottomBarH")
        }

        // Condition 2: Consecutive 2 stationary frames triggers bottom reached even if dynamic content is present
        val isBottom = matchResult.isBottomReached || newStatCount >= 2

        if (isBottom) {
            if (matchResult.deltaY > 0) {
                appendIncrementStrip(nextSlice, matchResult.deltaY, currentBottomBarH)
                return FrameStepResult.Completed(nextSlice)
            } else if (hasStitchedAny) {
                return FrameStepResult.Completed(nextSlice)
            } else {
                return if (matchResult.sadScore <= 15.0f) {
                    FrameStepResult.Completed(nextSlice)
                } else {
                    Log.e(TAG, "Frame $frameIndex failed match with sadScore=${matchResult.sadScore} on initial step")
                    FrameStepResult.Error
                }
            }
        }

        appendIncrementStrip(nextSlice, matchResult.deltaY, currentBottomBarH)
        return FrameStepResult.Continue(nextSlice)
    }

    private fun appendIncrementStrip(nextSlice: PixelSlice, deltaY: Int, bottomBarHeight: Int) {
        if (deltaY > 0) {
            val safeCropStartY = maxOf(0, (nextSlice.height - bottomBarHeight) - deltaY)
            val safeCropHeight = minOf(deltaY, nextSlice.height - safeCropStartY)
            if (safeCropHeight > 0) {
                val strip = nextSlice.crop(
                    startY = safeCropStartY,
                    cropHeight = safeCropHeight
                )
                tileStore.appendStrip(strip)
            }
        }
    }

    private sealed interface FrameStepResult {
        data class Continue(val nextSlice: PixelSlice) : FrameStepResult
        data class Completed(val finalSlice: PixelSlice? = null) : FrameStepResult
        data object Error : FrameStepResult
        data object Stop : FrameStepResult
    }

    companion object {
        private const val TAG = "LoomWeaving"
    }
}
