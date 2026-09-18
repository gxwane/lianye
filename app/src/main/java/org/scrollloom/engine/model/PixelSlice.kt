package org.scrollloom.engine.model

data class PixelSlice(
    val pixels: IntArray,
    val width: Int,
    val height: Int,
    val stride: Int = width,
    val offset: Int = 0
) {
    inline fun getPixel(x: Int, y: Int): Int = pixels[offset + y * stride + x]

    init {
        require(width >= 32) { "Width $width is too small for 3-column analysis (minimum 32px)" }
        require(height > 0) { "Height must be positive" }
        require(offset + (height - 1) * stride + width <= pixels.size) {
            "Pixel array size ${pixels.size} is smaller than slice buffer footprint"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PixelSlice
        if (width != other.width) return false
        if (height != other.height) return false
        if (stride != other.stride) return false
        if (offset != other.offset) return false
        return pixels.contentEquals(other.pixels)
    }

    override fun hashCode(): Int {
        var result = pixels.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + stride
        result = 31 * result + offset
        return result
    }
}
