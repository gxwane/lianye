package org.scrollloom.engine

import org.scrollloom.engine.model.TileMetadata
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.OutputStream
import java.util.zip.CRC32
import java.util.zip.Deflater

class StreamingPngAssembler {

    companion object {
        private const val CHUNK_BUFFER_SIZE = 32 * 1024
        private val PNG_SIGNATURE = byteArrayOf(
            0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(),
            0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte()
        )
    }

    fun assemble(tiles: List<TileMetadata>, outputStream: OutputStream) {
        require(tiles.isNotEmpty()) { "Tiles cannot be empty for PNG assembly" }
        val width = tiles[0].width
        val totalHeight = tiles.sumOf { it.height }
        val crc = CRC32()
        val deflater = Deflater(1)

        try {
            outputStream.write(PNG_SIGNATURE)
            writeIhdr(outputStream, width, totalHeight, crc)
            streamIdatChunks(outputStream, tiles, width, deflater, crc)
            writeIend(outputStream, crc)
            outputStream.flush()
        } finally {
            deflater.end()
        }
    }

    fun assembleWithPagination(
        tiles: List<TileMetadata>,
        maxSegmentHeight: Int,
        outputFactory: (segmentIndex: Int) -> OutputStream
    ): List<Long> {
        require(tiles.isNotEmpty()) { "Tiles cannot be empty for pagination" }
        val segmentHeights = mutableListOf<Long>()
        var currentSegmentTiles = mutableListOf<TileMetadata>()
        var currentHeight = 0L
        var segmentIndex = 0

        for (tile in tiles) {
            if (currentHeight > 0 && currentHeight + tile.height > maxSegmentHeight) {
                assembleSegment(currentSegmentTiles, outputFactory(segmentIndex++))
                segmentHeights.add(currentHeight)
                currentSegmentTiles = mutableListOf()
                currentHeight = 0L
            }
            currentSegmentTiles.add(tile)
            currentHeight += tile.height
        }

        if (currentSegmentTiles.isNotEmpty()) {
            assembleSegment(currentSegmentTiles, outputFactory(segmentIndex))
            segmentHeights.add(currentHeight)
        }

        return segmentHeights
    }

    private fun assembleSegment(segmentTiles: List<TileMetadata>, outputStream: OutputStream) {
        outputStream.use { out ->
            assemble(segmentTiles, out)
        }
    }

    private fun writeIhdr(out: OutputStream, w: Int, h: Int, crc: CRC32) {
        val data = ByteArray(13)
        writeInt32BE(data, 0, w)
        writeInt32BE(data, 4, h)
        data[8] = 8.toByte()  // Bit depth
        data[9] = 6.toByte()  // Color type: RGBA
        data[10] = 0.toByte() // Compression: Deflate
        data[11] = 0.toByte() // Filter: None
        data[12] = 0.toByte() // Interlace: None
        writeChunk(out, "IHDR", data, 0, data.size, crc)
    }

    private fun streamIdatChunks(
        out: OutputStream,
        tiles: List<TileMetadata>,
        width: Int,
        deflater: Deflater,
        crc: CRC32
    ) {
        val rawRow = ByteArray(width * 4)
        val scanline = ByteArray(1 + width * 4)
        scanline[0] = 0x00 // Filter None
        val chunkBuffer = ByteArray(CHUNK_BUFFER_SIZE)

        for (tile in tiles) {
            BufferedInputStream(FileInputStream(tile.file), 64 * 1024).use { input ->
                for (y in 0 until tile.height) {
                    readFullRow(input, rawRow)
                    reorderArgbToRgba(rawRow, scanline, width)
                    deflater.setInput(scanline, 0, scanline.size)
                    drainDeflater(out, deflater, chunkBuffer, crc)
                }
            }
        }

        deflater.finish()
        drainDeflaterUntilFinished(out, deflater, chunkBuffer, crc)
    }

    private fun readFullRow(input: BufferedInputStream, rawRow: ByteArray) {
        var bytesRead = 0
        while (bytesRead < rawRow.size) {
            val count = input.read(rawRow, bytesRead, rawRow.size - bytesRead)
            if (count < 0) {
                throw java.io.EOFException("Unexpected end of tile file: read $bytesRead of ${rawRow.size} bytes")
            }
            bytesRead += count
        }
    }

    private fun reorderArgbToRgba(raw: ByteArray, scanline: ByteArray, width: Int) {
        var src = 0
        var dst = 1
        for (x in 0 until width) {
            val a = raw[src++]
            val r = raw[src++]
            val g = raw[src++]
            val b = raw[src++]
            scanline[dst++] = r
            scanline[dst++] = g
            scanline[dst++] = b
            scanline[dst++] = a
        }
    }

    private fun drainDeflater(
        out: OutputStream,
        deflater: Deflater,
        buffer: ByteArray,
        crc: CRC32
    ) {
        while (!deflater.needsInput()) {
            val bytes = deflater.deflate(buffer, 0, buffer.size)
            if (bytes <= 0) break
            writeChunk(out, "IDAT", buffer, 0, bytes, crc)
        }
    }

    private fun drainDeflaterUntilFinished(
        out: OutputStream,
        deflater: Deflater,
        buffer: ByteArray,
        crc: CRC32
    ) {
        while (!deflater.finished()) {
            val bytes = deflater.deflate(buffer, 0, buffer.size)
            if (bytes <= 0) break
            writeChunk(out, "IDAT", buffer, 0, bytes, crc)
        }
    }

    private fun writeIend(out: OutputStream, crc: CRC32) {
        writeChunk(out, "IEND", ByteArray(0), 0, 0, crc)
    }

    private fun writeChunk(
        out: OutputStream,
        typeStr: String,
        data: ByteArray,
        offset: Int,
        len: Int,
        crc: CRC32
    ) {
        writeUInt32BE(out, len.toLong())
        val typeBytes = typeStr.toByteArray(Charsets.US_ASCII)
        crc.reset()
        crc.update(typeBytes)
        if (len > 0) {
            crc.update(data, offset, len)
        }
        out.write(typeBytes)
        if (len > 0) {
            out.write(data, offset, len)
        }
        writeUInt32BE(out, crc.value)
    }

    private fun writeUInt32BE(out: OutputStream, value: Long) {
        out.write(((value ushr 24) and 0xFF).toInt())
        out.write(((value ushr 16) and 0xFF).toInt())
        out.write(((value ushr 8) and 0xFF).toInt())
        out.write((value and 0xFF).toInt())
    }

    private fun writeInt32BE(target: ByteArray, offset: Int, value: Int) {
        target[offset] = ((value ushr 24) and 0xFF).toByte()
        target[offset + 1] = ((value ushr 16) and 0xFF).toByte()
        target[offset + 2] = ((value ushr 8) and 0xFF).toByte()
        target[offset + 3] = (value and 0xFF).toByte()
    }
}
