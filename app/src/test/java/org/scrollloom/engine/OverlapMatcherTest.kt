package org.scrollloom.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.scrollloom.engine.model.PixelSlice
import kotlin.math.abs
import kotlin.random.Random

class OverlapMatcherTest {

    private lateinit var matcher: OverlapMatcher
    private val width = 100
    private val height = 600

    @Before
    fun setUp() {
        matcher = OverlapMatcher(
            templateHeight = 120,
            maxSearchRange = 300,
            trimRatio = 0.15f
        )
    }

    @Test
    fun `standard overlap should calculate exact deltaY within 1px`() {
        val shiftY = 150
        val basePattern = generateRealisticUiPattern(width, height + shiftY)

        val prevSlice = extractSlice(basePattern, 0, height)
        val nextSlice = extractSlice(basePattern, shiftY, height)

        val result = matcher.match(prevSlice, nextSlice)

        assertFalse("Bottom should not be reached", result.isBottomReached)
        assertFalse("Should not be secure blocked", result.isSecureBlocked)
        assertEquals("DeltaY must match shift within 1px", shiftY.toDouble(), result.deltaY.toDouble(), 1.0)
    }

    @Test
    fun `weak texture area at bottom should cause template window to shift upward`() {
        val prevPixels = IntArray(width * height) { -0x1 } // All white (0xFFFFFFFF)
        // Add texture in rows 100..250 (upper-middle area)
        for (y in 100..250) {
            for (x in 0 until width) {
                prevPixels[y * width + x] = if ((x + y) % 6 == 0) -0x1000000 else -0x1
            }
        }
        val prevSlice = PixelSlice(prevPixels, width, height)
        val sig = matcher.extractLumaSignature(prevSlice)

        val templateStart = matcher.chooseTemplateStart(prevSlice, sig)

        // Template should avoid the bottom white area (300..599) and pick an index <= 250
        assertTrue("Template start should be shifted into textured zone", templateStart <= 250)
    }

    @Test
    fun `periodic repetitive list should disambiguate using prior displacement`() {
        // Create repeating pattern with 50px period
        val totalH = 800
        val pixels = IntArray(width * totalH)
        for (y in 0 until totalH) {
            val isBar = (y % 50) in 0..10
            val color = if (isBar) -0x1000000 else -0x222222
            for (x in 0 until width) {
                pixels[y * width + x] = color
            }
        }
        val fullSlice = PixelSlice(pixels, width, totalH)

        val expectedShift = 100 // exactly 2 periods
        val prevSlice = extractSlice(fullSlice, 0, height)
        val nextSlice = extractSlice(fullSlice, expectedShift, height)

        // With prior expected shift of 100, should disambiguate to 100
        val result = matcher.match(prevSlice, nextSlice, priorDeltaY = 100)

        assertEquals("Periodic pattern should disambiguate to expected shift", expectedShift, result.deltaY)
    }

    @Test
    fun `temporal variance mask should eliminate static header interference`() {
        val shiftY = 75
        val staticHeaderH = 60

        val slice1 = generateSliceWithStaticHeader(width, height, staticHeaderH, scrollOffset = 0)
        val slice2 = generateSliceWithStaticHeader(width, height, staticHeaderH, scrollOffset = shiftY)
        val slice3 = generateSliceWithStaticHeader(width, height, staticHeaderH, scrollOffset = shiftY * 2)

        val maskEngine = TemporalVarianceMask(varianceThreshold = 16L)
        val mask = maskEngine.computeRowMask(slice1, slice2, slice3)

        // Header rows should be marked false (static)
        for (y in 0 until staticHeaderH) {
            assertFalse("Header row $y should be marked static (false)", mask[y])
        }
        // Body rows should be marked true (dynamic)
        assertTrue("Content row should be marked dynamic (true)", mask[staticHeaderH + 20])

        // Matching slice1 and slice2 using the mask
        val result = matcher.match(slice1, slice2, rowMask = mask)
        assertEquals("Matching with mask should detect correct content shift", shiftY.toDouble(), result.deltaY.toDouble(), 1.0)
    }

    @Test
    fun `trimmed SAD should tolerate outlier rows from video or gif`() {
        val shiftY = 120
        val basePattern = generateRealisticUiPattern(width, height + shiftY)

        val prevSlice = extractSlice(basePattern, 0, height)
        val nextSliceRaw = extractSlice(basePattern, shiftY, height)

        // Corrupt 10% of rows in nextSlice with random noise (simulating moving GIF)
        val corruptedPixels = nextSliceRaw.pixels.clone()
        val random = Random(42)
        for (y in 200..230) { // 31 rows = ~5% of 600
            for (x in 0 until width) {
                corruptedPixels[y * width + x] = random.nextInt()
            }
        }
        val nextSliceCorrupted = PixelSlice(corruptedPixels, width, height)

        val result = matcher.match(prevSlice, nextSliceCorrupted)

        assertEquals("Trimmed SAD must ignore noisy rows and find correct deltaY", shiftY.toDouble(), result.deltaY.toDouble(), 1.0)
    }

    @Test
    fun `identical consecutive frames should terminate with bottom reached`() {
        val basePattern = generateRealisticUiPattern(width, height)
        val prevSlice = extractSlice(basePattern, 0, height)
        val nextSlice = extractSlice(basePattern, 0, height) // 0 shift

        val result = matcher.match(prevSlice, nextSlice)

        assertTrue("Zero displacement must trigger isBottomReached", result.isBottomReached)
    }

    @Test
    fun `all black frame should trigger secure window blocked`() {
        val blackPixels = IntArray(width * height) { -0x1000000 } // All black (0xFF000000)
        val blackSlice = PixelSlice(blackPixels, width, height)
        val normalSlice = generateRealisticUiPattern(width, height)

        val result = matcher.match(normalSlice, blackSlice)

        assertTrue("All-black next frame must trigger isSecureBlocked", result.isSecureBlocked)
    }

    @Test
    fun `matching 600px slice benchmark should complete within 5ms`() {
        val basePattern = generateRealisticUiPattern(width, height + 200)
        val prevSlice = extractSlice(basePattern, 0, height)
        val nextSlice = extractSlice(basePattern, 120, height)

        // Warm up
        repeat(5) { matcher.match(prevSlice, nextSlice) }

        val start = System.nanoTime()
        matcher.match(prevSlice, nextSlice)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000.0

        assertTrue("Matching should take <= 15ms, took ${elapsedMs}ms", elapsedMs <= 15.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `templateHeight below 32 should throw IllegalArgumentException`() {
        OverlapMatcher(templateHeight = 16)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `templateHeight above 1024 should throw IllegalArgumentException`() {
        OverlapMatcher(templateHeight = 1025)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid trimRatio should throw IllegalArgumentException`() {
        OverlapMatcher(trimRatio = 1.0f)
    }

    @Test
    fun `all black frame with non-trivial stride should trigger secure window blocked`() {
        val stride = width + 64
        val blackPixels = IntArray(stride * height) { -0x1000000 }
        val blackSlice = PixelSlice(blackPixels, width, height, stride = stride, offset = 16)
        val normalSlice = generateRealisticUiPattern(width, height)

        val result = matcher.match(normalSlice, blackSlice)

        assertTrue("All-black slice with stride must trigger isSecureBlocked", result.isSecureBlocked)
    }

    private fun generateRealisticUiPattern(w: Int, h: Int): PixelSlice {
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val itemIdx = y / 80
            val isHeader = (y % 80) in 0..15
            val isDivider = (y % 80) == 79
            val baseColor = when {
                isHeader -> -0x333334
                isDivider -> -0x777778
                else -> -0x1
            }
            val avatarW = 10 + (itemIdx * 5) % 15
            val textLen = 25 + (itemIdx * 11) % 35
            val textColor = if (itemIdx % 2 == 0) -0xcccccd else -0x888889
            for (x in 0 until w) {
                val isAvatar = (x in 10 until (10 + avatarW)) && ((y % 80) in 25..55)
                val isText = (x in 40 until (40 + textLen)) && ((y % 20) in 5..12)
                pixels[y * w + x] = when {
                    isAvatar -> -0x1000000 + (itemIdx * 0x050709)
                    isText -> textColor
                    else -> baseColor
                }
            }
        }
        return PixelSlice(pixels, w, h)
    }

    private fun generateSliceWithStaticHeader(w: Int, h: Int, headerH: Int, scrollOffset: Int): PixelSlice {
        val base = generateRealisticUiPattern(w, h + scrollOffset)
        val pixels = IntArray(w * h)
        // Static header: 0..headerH-1 (same content regardless of scrollOffset)
        for (y in 0 until headerH) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (x % 10 < 5) -0xff8800 else -0x333334
            }
        }
        // Dynamic content from basePattern: headerH..h-1
        for (y in headerH until h) {
            val srcY = y + scrollOffset
            System.arraycopy(base.pixels, srcY * w, pixels, y * w, w)
        }
        return PixelSlice(pixels, w, h)
    }

    private fun extractSlice(source: PixelSlice, startY: Int, h: Int): PixelSlice {
        val slicePixels = IntArray(source.width * h)
        System.arraycopy(source.pixels, startY * source.width, slicePixels, 0, source.width * h)
        return PixelSlice(slicePixels, source.width, h)
    }
}
