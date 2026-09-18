package org.scrollloom.engine

import org.scrollloom.engine.model.LumaSignature
import org.scrollloom.engine.model.MatchResult
import org.scrollloom.engine.model.PixelSlice
import java.util.Arrays

class OverlapMatcher(
    val templateHeight: Int = 256,
    val maxSearchRange: Int = 800,
    val trimRatio: Float = 0.15f
) {
    init {
        require(templateHeight in 32..1024) { "templateHeight $templateHeight must be within 32..1024" }
        require(trimRatio in 0.0f..<1.0f) { "trimRatio $trimRatio must be in [0.0, 1.0)" }
        require(maxSearchRange > 0) { "maxSearchRange must be positive" }
    }

    // Pre-allocated scratch buffers to guarantee Zero Heap Allocation in search loop
    private val scratchResiduals = IntArray(1024)
    private val scratchCounts = IntArray(1024)

    fun extractLumaSignature(slice: PixelSlice): LumaSignature {
        val w = slice.width
        val h = slice.height
        require(w >= 32) { "Width $w is too small for 3-column analysis" }

        val startX = (w * 0.05f).toInt()
        val endX = (w * 0.90f).toInt()
        val effW = maxOf(1, endX - startX)

        val col1End = startX + (effW * 0.25f).toInt()
        val col2End = startX + (effW * 0.75f).toInt()

        val leftLuma = IntArray(h)
        val centerLuma = IntArray(h)
        val rightLuma = IntArray(h)
        val horizontalVar = LongArray(0)

        val leftCount = maxOf(1, col1End - startX)
        val centerCount = maxOf(1, col2End - col1End)
        val rightCount = maxOf(1, endX - col2End)

        for (y in 0 until h) {
            var sumLeft = 0L
            var sumCenter = 0L
            var sumRight = 0L

            for (x in startX until endX) {
                val p = slice.getPixel(x, y)
                // P1 Fix: ushr for unsigned byte unpacking
                val r = (p ushr 16) and 0xFF
                val g = (p ushr 8) and 0xFF
                val b = p and 0xFF
                val luma = (77 * r + 150 * g + 29 * b) ushr 8

                when {
                    x < col1End -> sumLeft += luma
                    x < col2End -> sumCenter += luma
                    else -> sumRight += luma
                }
            }

            leftLuma[y] = (sumLeft / leftCount).toInt()
            centerLuma[y] = (sumCenter / centerCount).toInt()
            rightLuma[y] = (sumRight / rightCount).toInt()
        }

        return LumaSignature(h, leftLuma, centerLuma, rightLuma, horizontalVar)
    }

    fun chooseTemplateStart(prevSlice: PixelSlice, sig: LumaSignature, rowMask: BooleanArray? = null): Int {
        val h = sig.height
        val tH = minOf(templateHeight, h)
        if (h <= tH) return 0

        val scanStart = h - tH
        val scanEnd = maxOf(0, tH / 2)

        var bestY = scanStart
        var maxVariance = -1.0f

        val minVarianceThreshold = 25.0f
        val minGradThreshold = 80L

        for (y in scanStart downTo scanEnd) {
            var sumL = 0L
            var sumSq = 0L
            var gradEnergy = 0L
            var staticCount = 0

            for (j in 0 until tH) {
                val rowY = y + j
                if (rowMask != null && rowY < rowMask.size && !rowMask[rowY]) {
                    staticCount++
                }
                val l = sig.centerLuma[rowY].toLong()
                sumL += l
                sumSq += (l * l)
                if (j > 0) {
                    val prevL = sig.centerLuma[rowY - 1].toLong()
                    gradEnergy += kotlin.math.abs(l - prevL)
                }
            }

            if (staticCount > tH / 4) continue // Avoid selecting static headers as template

            val mean = sumL.toDouble() / tH
            val variance = (sumSq.toDouble() / tH - mean * mean).toFloat()

            if (variance > maxVariance) {
                maxVariance = variance
                bestY = y
            }

            if (variance >= minVarianceThreshold && gradEnergy >= minGradThreshold) {
                return y
            }
        }

        return bestY
    }

    fun match(
        prevSlice: PixelSlice,
        nextSlice: PixelSlice,
        rowMask: BooleanArray? = null,
        priorDeltaY: Int? = null
    ): MatchResult {
        // 1. Check for FLAG_SECURE all-black detection
        if (isFrameAllBlack(nextSlice)) {
            return MatchResult(
                deltaY = 0,
                sadScore = 0f,
                ambiguityRatio = 1.0f,
                isBottomReached = false,
                isSecureBlocked = true
            )
        }

        val sigPrev = extractLumaSignature(prevSlice)
        val sigNext = extractLumaSignature(nextSlice)

        val tH = minOf(templateHeight, sigPrev.height)
        val tStartPrev = chooseTemplateStart(prevSlice, sigPrev, rowMask)

        val maxShift = minOf(maxSearchRange, tStartPrev)
        var minScore1 = Float.MAX_VALUE
        var bestDeltaY1 = 0

        var minScore2 = Float.MAX_VALUE
        var bestDeltaY2 = 0

        val scores = FloatArray(maxShift + 1) { Float.MAX_VALUE }

        val searchStep = 1
        for (deltaY in 0..maxShift step searchStep) {
            val tStartNext = tStartPrev - deltaY
            if (tStartNext < 0 || tStartNext + tH > sigNext.height) continue

            val score = computeTrimmedSad(
                sigPrev, tStartPrev,
                sigNext, tStartNext,
                tH, rowMask
            )
            scores[deltaY] = score

            if (score < minScore1) {
                if (kotlin.math.abs(deltaY - bestDeltaY1) >= 15) {
                    minScore2 = minScore1
                    bestDeltaY2 = bestDeltaY1
                }
                minScore1 = score
                bestDeltaY1 = deltaY
            } else if (score < minScore2 && kotlin.math.abs(deltaY - bestDeltaY1) >= 15) {
                minScore2 = score
                bestDeltaY2 = deltaY
            }
        }

        // Laplace smoothed ambiguity ratio: ratio >= 0.85 means best score is within 15% of second best
        val ambiguityRatio = if (minScore2 < Float.MAX_VALUE) {
            (minScore1 + 1.0f) / (minScore2 + 1.0f)
        } else {
            0.0f
        }

        var finalDeltaY = bestDeltaY1
        var finalScore = minScore1

        // If ambiguous (ambiguityRatio >= 0.85) and we have a priorDeltaY, disambiguate using Gaussian prior penalty
        if (ambiguityRatio >= 0.85f && priorDeltaY != null) {
            var minPenalizedScore = Float.MAX_VALUE
            val threshold = minScore1 * 1.25f + 5.0f
            for (deltaY in 0..maxShift step searchStep) {
                val s = scores[deltaY]
                if (s <= threshold) {
                    val diff = deltaY - priorDeltaY
                    val penalized = s + 0.04f * (diff * diff)
                    if (penalized < minPenalizedScore) {
                        minPenalizedScore = penalized
                        finalDeltaY = deltaY
                        finalScore = s
                    }
                }
            }
        }

        val isBottomReached = (finalDeltaY <= 2 && finalScore <= 15.0f)

        return MatchResult(
            deltaY = finalDeltaY,
            sadScore = finalScore,
            ambiguityRatio = ambiguityRatio,
            isBottomReached = isBottomReached,
            isSecureBlocked = false
        )
    }

    private fun computeTrimmedSad(
        sigPrev: LumaSignature, startPrev: Int,
        sigNext: LumaSignature, startNext: Int,
        tH: Int, rowMask: BooleanArray?
    ): Float {
        var validCount = 0

        for (j in 0 until tH) {
            val yPrev = startPrev + j
            val yNext = startNext + j

            if (rowMask != null) {
                val isNextStatic = yNext < rowMask.size && !rowMask[yNext]
                val isPrevStatic = yPrev < rowMask.size && !rowMask[yPrev]
                if (isNextStatic || isPrevStatic) {
                    continue // Skip static rows (e.g. AppBar / TabBar)
                }
            }

            val diffLeft = kotlin.math.abs(sigPrev.leftLuma[yPrev] - sigNext.leftLuma[yNext])
            val diffCenter = kotlin.math.abs(sigPrev.centerLuma[yPrev] - sigNext.centerLuma[yNext])
            val diffRight = kotlin.math.abs(sigPrev.rightLuma[yPrev] - sigNext.rightLuma[yNext])

            val diffTotal = minOf(1023, diffLeft + 2 * diffCenter + diffRight)
            scratchResiduals[validCount++] = diffTotal
        }

        if (validCount == 0) return Float.MAX_VALUE

        val keepCount = maxOf(1, (validCount * (1.0f - trimRatio)).toInt())

        // P1 Fix: Zero-allocation counting sort selection
        Arrays.fill(scratchCounts, 0)
        for (i in 0 until validCount) {
            scratchCounts[scratchResiduals[i]]++
        }

        var accumulated = 0
        var cutoff = 1023
        for (v in 0..1023) {
            accumulated += scratchCounts[v]
            if (accumulated >= keepCount) {
                cutoff = v
                break
            }
        }

        var sumInliers = 0L
        var countInliers = 0
        for (i in 0 until validCount) {
            val r = scratchResiduals[i]
            if (r < cutoff && countInliers < keepCount) {
                sumInliers += r
                countInliers++
            }
        }
        while (countInliers < keepCount) {
            sumInliers += cutoff
            countInliers++
        }

        return sumInliers.toFloat() / keepCount
    }

    private fun isFrameAllBlack(slice: PixelSlice): Boolean {
        val stepX = maxOf(1, slice.width / 30)
        val stepY = maxOf(1, slice.height / 40)
        var total = 0
        var black = 0
        for (y in 0 until slice.height step stepY) {
            for (x in 0 until slice.width step stepX) {
                total++
                val pixel = slice.getPixel(x, y)
                val r = (pixel ushr 16) and 0xFF
                val g = (pixel ushr 8) and 0xFF
                val b = pixel and 0xFF
                if (r <= 2 && g <= 2 && b <= 2) {
                    black++
                }
            }
        }
        return total > 0 && black == total
    }
}
