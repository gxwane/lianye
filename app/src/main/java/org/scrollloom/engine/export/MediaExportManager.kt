package org.scrollloom.engine.export

import android.content.ClipData
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import org.scrollloom.engine.StreamingPngAssembler
import org.scrollloom.engine.model.TileMetadata

class MediaExportManager(
    private val contentResolver: ContentResolver,
    private val assembler: StreamingPngAssembler = StreamingPngAssembler()
) {
    companion object {
        const val PAGINATION_THRESHOLD_PX = 30_000
    }

    /**
     * 保存瓦片至系统相册。
     * 当总高度超过 [PAGINATION_THRESHOLD_PX] (30,000px) 时，
     * 自动启用安全分卷策略，输出多张独立合规 PNG (如 _part1, _part2)，
     * 彻底解决部分系统相册与第三方软件因巨图纹理溢出导致崩溃的问题。
     */
    fun saveTilesToGallery(
        tiles: List<TileMetadata>,
        title: String = "ScrollLoom_${System.currentTimeMillis()}"
    ): Uri? {
        val uris = saveAllTilesToGallery(tiles, title)
        return uris.firstOrNull()
    }

    fun saveAllTilesToGallery(
        tiles: List<TileMetadata>,
        title: String = "ScrollLoom_${System.currentTimeMillis()}"
    ): List<Uri> {
        if (tiles.isEmpty()) return emptyList()

        val totalHeight = tiles.sumOf { it.height }
        if (totalHeight <= PAGINATION_THRESHOLD_PX) {
            val singleUri = saveSingleImage(tiles, "$title.png")
            return if (singleUri != null) listOf(singleUri) else emptyList()
        }

        // 超出 30,000px 启用智能分卷直写 (Part 1, Part 2...)
        val savedUris = mutableListOf<Uri>()
        val pendingUris = mutableListOf<Uri>()
        var writeAllSuccess = false

        try {
            assembler.assembleWithPagination(tiles, PAGINATION_THRESHOLD_PX) { segmentIndex ->
                val segmentName = "${title}_part${segmentIndex + 1}.png"
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, segmentName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ScrollLoom")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val segmentUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: throw java.io.IOException("Failed to create MediaStore record for $segmentName")
                pendingUris.add(segmentUri)
                contentResolver.openOutputStream(segmentUri, "w")
                    ?: throw java.io.IOException("Failed to open output stream for $segmentUri")
            }

            // 全部成功写出后，统一撤销 IS_PENDING
            val finalValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            for (uri in pendingUris) {
                contentResolver.update(uri, finalValues, null, null)
                savedUris.add(uri)
            }
            writeAllSuccess = true
        } catch (e: Exception) {
            writeAllSuccess = false
        } finally {
            if (!writeAllSuccess) {
                for (uri in pendingUris) {
                    try {
                        contentResolver.delete(uri, null, null)
                    } catch (_: Exception) {}
                }
                savedUris.clear()
            }
        }

        return savedUris
    }

    private fun saveSingleImage(tiles: List<TileMetadata>, fileName: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ScrollLoom")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        var writeSuccess = false

        return try {
            contentResolver.openOutputStream(uri, "w")?.use { out ->
                assembler.assemble(tiles, out)
            }
            writeSuccess = true
            val finalValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            contentResolver.update(uri, finalValues, null, null)
            uri
        } finally {
            if (!writeSuccess) {
                try {
                    contentResolver.delete(uri, null, null)
                } catch (_: Exception) {}
            }
        }
    }

    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(contentResolver, "ScrollLoom", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
