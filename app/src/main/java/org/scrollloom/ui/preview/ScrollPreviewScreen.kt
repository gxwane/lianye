package org.scrollloom.ui.preview

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import org.scrollloom.engine.model.TileMetadata
import org.scrollloom.ui.common.theme.ScrollLoomTheme
import androidx.activity.compose.BackHandler
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollPreviewScreen(
    tiles: List<TileMetadata>,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!LocalInspectionMode.current) {
        BackHandler {
            onBackClick()
        }
    }

    val totalHeight = tiles.sumOf { it.height }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("长卷预览 (${totalHeight}px)") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text("存入相册", maxLines = 1)
                }
                Spacer(modifier = Modifier.width(12.dp))
                FilledTonalButton(
                    onClick = onShareClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("分享长卷", maxLines = 1)
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(tiles, key = { it.index }) { tile ->
                TileItem(tile = tile)
            }
        }
    }
}

@Composable
private fun TileItem(tile: TileMetadata) {
    val aspect = if (tile.height > 0) tile.width.toFloat() / tile.height.toFloat() else 1f

    if (LocalInspectionMode.current) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspect)
                .background(
                    if (tile.index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surface
                )
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tile #${tile.index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${tile.width} × ${tile.height} px (Y: ${tile.startY})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        return
    }

    val bitmapState by produceState<ImageBitmap?>(initialValue = null, tile) {
        value = withContext(Dispatchers.IO) {
            loadTileBitmap(tile)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        bitmapState?.let { imageBitmap ->
            Image(
                bitmap = imageBitmap,
                contentDescription = "Tile ${tile.index}",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun loadTileBitmap(tile: TileMetadata): ImageBitmap? {
    if (!tile.file.exists() || tile.width <= 0 || tile.height <= 0) return null
    return try {
        val pixelCount = tile.width * tile.height
        val buffer = ByteBuffer.allocate(pixelCount * 4)
        FileInputStream(tile.file).use { input ->
            val channel = input.channel
            while (buffer.hasRemaining()) {
                val read = channel.read(buffer)
                if (read < 0) break
            }
        }
        buffer.flip()
        val pixels = IntArray(pixelCount)
        buffer.asIntBuffer().get(pixels)
        val bitmap = Bitmap.createBitmap(pixels, tile.width, tile.height, Bitmap.Config.ARGB_8888)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

// ==================== Preview 区域 ====================

private fun createMockTiles(): List<TileMetadata> = listOf(
    TileMetadata(0, File("mock_0.raw"), 1080, 800, 0),
    TileMetadata(1, File("mock_1.raw"), 1080, 800, 800),
    TileMetadata(2, File("mock_2.raw"), 1080, 800, 1600)
)

@Preview(name = "长卷预览 - 浅色", group = "Screens", showSystemUi = true)
@Preview(name = "长卷预览 - 深色", group = "Screens", uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun PreviewScrollPreviewScreen() {
    ScrollLoomTheme(dynamicColor = false) {
        ScrollPreviewScreen(
            tiles = createMockTiles(),
            onSaveClick = {},
            onShareClick = {},
            onBackClick = {}
        )
    }
}

