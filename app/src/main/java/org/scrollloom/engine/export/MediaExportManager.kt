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
    fun saveTilesToGallery(
        tiles: List<TileMetadata>,
        title: String = "ScrollLoom_${System.currentTimeMillis()}"
    ): Uri? {
        if (tiles.isEmpty()) return null

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$title.png")
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
                contentResolver.delete(uri, null, null)
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
