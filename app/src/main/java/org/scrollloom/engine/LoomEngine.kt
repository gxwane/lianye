package org.scrollloom.engine

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
    private val varianceMaskEngine: TemporalVarianceMask = TemporalVarianceMask()
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
        scrollCommand: ControlledScrollCommand = ControlledScrollCommand(
            startX = 500f, startY = 1200f, endX = 500f, endY = 600f
        )
    ): List<TileMetadata> {
        if (!weaveMutex.tryLock()) {
            return emptyList()
        }

        try {
            tileStore.clear()
            frameWindow.clear()
            priorDeltaY = null
            isStopRequested = false
            _state.value = LoomState.CAPTURING

            var prevSlice: PixelSlice? = null

            val initialFrame = frameCapturer.captureFrame() ?: run {
                _state.value = LoomState.ERROR
                return emptyList()
            }
            tileStore.appendStrip(initialFrame)
            frameWindow.add(initialFrame)
            prevSlice = initialFrame

            for (frameIndex in 1 until maxFrames) {
                if (isStopRequested) {
                    _state.value = LoomState.STOPPING
                    break
                }
                val currentPrev = prevSlice ?: break
                when (val step = processFrameStep(currentPrev, scrollCommand)) {
                    is FrameStepResult.Continue -> prevSlice = step.nextSlice
                    is FrameStepResult.Completed -> {
                        _state.value = LoomState.COMPLETED
                        break
                    }
                    is FrameStepResult.Error -> {
                        _state.value = LoomState.ERROR
                        break
                    }
                    is FrameStepResult.Stop -> break
                }
            }

            if (_state.value != LoomState.ERROR && _state.value != LoomState.COMPLETED) {
                _state.value = LoomState.COMPLETED
            }
            return tileStore.finalizeTiles()
        } catch (e: Exception) {
            _state.value = LoomState.ERROR
            antiFlingController.cancelCurrentGesture()
            tileStore.clear()
            throw e
        } finally {
            weaveMutex.unlock()
        }
    }

    fun stop() {
        isStopRequested = true
    }

    private suspend fun processFrameStep(
        prevSlice: PixelSlice,
        command: ControlledScrollCommand
    ): FrameStepResult {
        if (!antiFlingController.executeControlledScroll(command)) return FrameStepResult.Stop
        _state.value = LoomState.CAPTURING
        val nextSlice = frameCapturer.captureFrame() ?: return FrameStepResult.Stop

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

        if (matchResult.deltaY > 0) {
            priorDeltaY = matchResult.deltaY
        }

        if (matchResult.isSecureBlocked) return FrameStepResult.Error

        if (matchResult.isBottomReached) {
            appendIncrementStrip(nextSlice, matchResult.deltaY)
            return FrameStepResult.Completed
        }

        appendIncrementStrip(nextSlice, matchResult.deltaY)
        return FrameStepResult.Continue(nextSlice)
    }

    private fun appendIncrementStrip(nextSlice: PixelSlice, deltaY: Int) {
        if (deltaY > 0) {
            val strip = nextSlice.crop(
                startY = nextSlice.height - deltaY,
                cropHeight = deltaY
            )
            tileStore.appendStrip(strip)
        }
    }

    private sealed interface FrameStepResult {
        data class Continue(val nextSlice: PixelSlice) : FrameStepResult
        data object Completed : FrameStepResult
        data object Error : FrameStepResult
        data object Stop : FrameStepResult
    }
}
