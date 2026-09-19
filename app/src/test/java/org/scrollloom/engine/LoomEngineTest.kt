package org.scrollloom.engine

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.scrollloom.engine.model.LoomState
import org.scrollloom.engine.model.PixelSlice
import org.scrollloom.service.gesture.AntiFlingController
import org.scrollloom.service.gesture.GestureDispatcher
import org.scrollloom.service.gesture.model.ControlledScrollCommand

@OptIn(ExperimentalCoroutinesApi::class)
class LoomEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var overlapMatcher: OverlapMatcher
    private lateinit var antiFlingController: AntiFlingController
    private lateinit var tileStore: TileStore
    private val width = 100
    private val height = 400

    private class FakeGestureDispatcher : GestureDispatcher {
        var lastCommand: ControlledScrollCommand? = null
        var cancelGestureCalled = false

        override fun sendGesture(command: ControlledScrollCommand, callback: (Boolean) -> Unit): Boolean {
            lastCommand = command
            callback(true)
            return true
        }

        override fun sendCancelGesture(): Boolean {
            cancelGestureCalled = true
            return true
        }
    }

    private class FakeFrameCapturer(private val frames: List<PixelSlice>) : FrameCapturer {
        private var index = 0
        override suspend fun captureFrame(): PixelSlice? {
            return if (index < frames.size) frames[index++] else null
        }
    }

    @Before
    fun setUp() {
        overlapMatcher = OverlapMatcher(templateHeight = 100, maxSearchRange = 250)
        antiFlingController = AntiFlingController(FakeGestureDispatcher())
        tileStore = TileStore(tempFolder.root, chunkTargetHeight = 500)
    }

    @Test
    fun `normal capture loop should stitch frames and finalize tiles`() = runTest {
        val shiftY = 80
        val basePattern = generatePattern(width, height + shiftY * 3)
        val frame0 = extractSlice(basePattern, 0, height)
        val frame1 = extractSlice(basePattern, shiftY, height)
        val frame2 = extractSlice(basePattern, shiftY * 2, height)

        val capturer = FakeFrameCapturer(listOf(frame0, frame1, frame2))
        val engine = LoomEngine(overlapMatcher, antiFlingController, tileStore, capturer)

        val tiles = engine.startWeaving(maxFrames = 3)

        assertEquals("Engine state should be COMPLETED", LoomState.COMPLETED, engine.state.value)
        assertFalse("Tiles should not be empty", tiles.isEmpty())
        val totalHeight = tiles.sumOf { it.height }
        // Frame 0 is base (height), Frame 1 adds shiftY, Frame 2 adds shiftY
        assertEquals("Total assembled height", height + shiftY * 2, totalHeight)
    }

    @Test
    fun `bottom reached should terminate early and finalize tiles`() = runTest {
        val basePattern = generatePattern(width, height + 100)
        val frame0 = extractSlice(basePattern, 0, height)
        val frame1 = extractSlice(basePattern, 50, height)
        val frame2 = extractSlice(basePattern, 50, height) // Identical frame (0 shift) -> isBottomReached

        val capturer = FakeFrameCapturer(listOf(frame0, frame1, frame2))
        val engine = LoomEngine(overlapMatcher, antiFlingController, tileStore, capturer)

        val tiles = engine.startWeaving(maxFrames = 10)

        assertEquals("Engine state should be COMPLETED", LoomState.COMPLETED, engine.state.value)
        assertEquals("Total height should include initial frame + frame1 increment only", height + 50, tiles.sumOf { it.height })
    }

    @Test
    fun `consecutive stationary frames should terminate with completed state`() = runTest {
        val basePattern = generatePattern(width, height)
        val frame0 = extractSlice(basePattern, 0, height)
        val frame1 = extractSlice(basePattern, 0, height) // Stationary frame 1
        val frame2 = extractSlice(basePattern, 0, height) // Stationary frame 2

        val capturer = FakeFrameCapturer(listOf(frame0, frame1, frame2))
        val engine = LoomEngine(overlapMatcher, antiFlingController, tileStore, capturer)

        val tiles = engine.startWeaving(maxFrames = 10)

        assertEquals("Engine state should be COMPLETED", LoomState.COMPLETED, engine.state.value)
        assertFalse("Tiles should not be empty", tiles.isEmpty())
    }

    @Test
    fun `secure blocked frame should transition to ERROR and stop`() = runTest {
        val normalFrame = generatePattern(width, height)
        val blackPixels = IntArray(width * height) { -0x1000000 }
        val secureFrame = PixelSlice(blackPixels, width, height)

        val capturer = FakeFrameCapturer(listOf(normalFrame, secureFrame))
        val engine = LoomEngine(overlapMatcher, antiFlingController, tileStore, capturer)

        val tiles = engine.startWeaving(maxFrames = 5)

        assertEquals("Engine state should be ERROR", LoomState.ERROR, engine.state.value)
        // Initial frame should be preserved in error so user doesn't lose what was already captured
        assertFalse("Initial captured tiles should be preserved on error", tiles.isEmpty())
    }

    @Test
    fun `detectBottomBarHeight should accurately detect stationary footer`() {
        val testH = 600
        val testW = 100
        val footerH = 80
        val deltaY = 80

        val slice0 = generateSliceWithFixedBottom(testW, testH, footerH, scrollOffset = 0)
        val slice1 = generateSliceWithFixedBottom(testW, testH, footerH, scrollOffset = deltaY)

        val engine = LoomEngine(overlapMatcher, antiFlingController, tileStore, FakeFrameCapturer(emptyList()))
        val detected = engine.detectBottomBarHeight(slice0, slice1, deltaY)

        assertEquals("Detected bottom bar height should match exactly", footerH, detected)
    }

    @Test
    fun `weaving app with fixed bottom bar should append footer only once at end`() = runTest {
        val testH = 600
        val testW = 100
        val footerH = 80
        val shiftY = 80

        val slice0 = generateSliceWithFixedBottom(testW, testH, footerH, scrollOffset = 0)
        val slice1 = generateSliceWithFixedBottom(testW, testH, footerH, scrollOffset = shiftY)
        val slice2 = generateSliceWithFixedBottom(testW, testH, footerH, scrollOffset = shiftY * 2)

        val capturer = FakeFrameCapturer(listOf(slice0, slice1, slice2))
        val engine = LoomEngine(overlapMatcher, antiFlingController, tileStore, capturer)

        val tiles = engine.startWeaving(maxFrames = 3)

        assertEquals("Engine state should be COMPLETED", LoomState.COMPLETED, engine.state.value)
        // Frame 0 body (testH - footerH = 520) + Frame 1 (shiftY = 80) + Frame 2 (shiftY = 80) + Final Footer (footerH = 80) = 760
        val totalHeight = tiles.sumOf { it.height }
        assertEquals("Total assembled height with single footer", (testH - footerH) + shiftY * 2 + footerH, totalHeight)
    }

    private fun generateSliceWithFixedBottom(w: Int, h: Int, footerH: Int, scrollOffset: Int): PixelSlice {
        val base = generatePattern(w, h + scrollOffset + footerH)
        val pixels = IntArray(w * h)
        // Body (0 until h - footerH): scrolls with scrollOffset
        for (y in 0 until (h - footerH)) {
            val srcY = y + scrollOffset
            System.arraycopy(base.pixels, srcY * w, pixels, y * w, w)
        }
        // Fixed footer (h - footerH until h): stationary across all frames
        for (y in (h - footerH) until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (x % 8 < 4) -0x5500aa else -0x223344
            }
        }
        return PixelSlice(pixels, w, h)
    }

    private fun generatePattern(w: Int, h: Int): PixelSlice {
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val r = (x * 7 + y * 13) % 255
                val g = (x * 19 + y * 3) % 255
                val b = (x * 31 + y * 23) % 255
                pixels[y * w + x] = -0x1000000 or (r shl 16) or (g shl 8) or b
            }
        }
        return PixelSlice(pixels, w, h)
    }

    private fun extractSlice(source: PixelSlice, startY: Int, h: Int): PixelSlice {
        val slicePixels = IntArray(source.width * h)
        System.arraycopy(source.pixels, startY * source.width, slicePixels, 0, source.width * h)
        return PixelSlice(slicePixels, source.width, h)
    }
}
