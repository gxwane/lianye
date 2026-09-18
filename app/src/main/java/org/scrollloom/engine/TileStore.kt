package org.scrollloom.engine

import org.scrollloom.engine.model.PixelSlice
import org.scrollloom.engine.model.TileMetadata
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class TileStore(
    private val storageDir: File,
    val chunkTargetHeight: Int = 1024
) {
    private val tiles = mutableListOf<TileMetadata>()
    private var currentTileIndex = 0
    private var currentTileHeight = 0
    private var currentTileStartY = 0
    private var currentFile: File? = null
    private var currentStream: BufferedOutputStream? = null
    private var initialWidth = -1

    private var rowBuffer = ByteArray(0)

    init {
        require(chunkTargetHeight > 0) { "chunkTargetHeight must be positive" }
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }

    fun appendStrip(strip: PixelSlice) {
        if (strip.height <= 0) return
        validateWidth(strip.width)
        ensureRowBufferCapacity(strip.width * 4)

        for (y in 0 until strip.height) {
            if (shouldRotateTile()) {
                rotateTile()
            }
            writeRow(strip, y)
        }
    }

    fun finalizeTiles(): List<TileMetadata> {
        closeCurrentTile()
        return tiles.toList()
    }

    fun clear() {
        closeCurrentTile()
        for (tile in tiles) {
            if (tile.file.exists()) {
                tile.file.delete()
            }
        }
        currentFile?.let { if (it.exists()) it.delete() }
        tiles.clear()
        resetState()
    }

    private fun validateWidth(w: Int) {
        if (initialWidth == -1) {
            initialWidth = w
        } else {
            require(w == initialWidth) {
                "Strip width mismatch: expected $initialWidth but was $w"
            }
        }
    }

    private fun shouldRotateTile(): Boolean {
        return currentStream == null || currentTileHeight >= chunkTargetHeight
    }

    private fun rotateTile() {
        closeCurrentTile()
        val file = File(storageDir, String.format("tile_%04d.bin", currentTileIndex))
        currentFile = file
        currentStream = BufferedOutputStream(FileOutputStream(file), 64 * 1024)
        currentTileStartY = if (tiles.isEmpty()) 0 else tiles.last().startY + tiles.last().height
        currentTileHeight = 0
    }

    private fun closeCurrentTile() {
        currentStream?.let { stream ->
            stream.flush()
            stream.close()
            currentStream = null
            val file = currentFile ?: return
            if (currentTileHeight > 0) {
                tiles.add(
                    TileMetadata(
                        index = currentTileIndex++,
                        file = file,
                        width = initialWidth,
                        height = currentTileHeight,
                        startY = currentTileStartY
                    )
                )
            }
            currentFile = null
        }
    }

    private fun writeRow(strip: PixelSlice, y: Int) {
        var offset = 0
        for (x in 0 until strip.width) {
            val pixel = strip.getPixel(x, y)
            rowBuffer[offset++] = ((pixel ushr 24) and 0xFF).toByte()
            rowBuffer[offset++] = ((pixel ushr 16) and 0xFF).toByte()
            rowBuffer[offset++] = ((pixel ushr 8) and 0xFF).toByte()
            rowBuffer[offset++] = (pixel and 0xFF).toByte()
        }
        currentStream?.write(rowBuffer, 0, offset)
        currentTileHeight++
    }

    private fun ensureRowBufferCapacity(neededBytes: Int) {
        if (rowBuffer.size < neededBytes) {
            rowBuffer = ByteArray(neededBytes)
        }
    }

    private fun resetState() {
        currentTileIndex = 0
        currentTileHeight = 0
        currentTileStartY = 0
        currentFile = null
        currentStream = null
        initialWidth = -1
    }
}
