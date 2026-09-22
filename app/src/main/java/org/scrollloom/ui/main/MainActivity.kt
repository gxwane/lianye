package org.scrollloom.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.scrollloom.App
import org.scrollloom.R
import org.scrollloom.domain.model.WeavingState
import org.scrollloom.engine.export.MediaExportManager
import org.scrollloom.service.capture.LoomMediaProjectionService
import org.scrollloom.ui.common.theme.ScrollLoomTheme
import org.scrollloom.ui.preview.ScrollPreviewScreen

class MainActivity : ComponentActivity() {

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            LoomMediaProjectionService.start(this, result.resultCode, result.data!!)
            Toast.makeText(this, getString(R.string.toast_media_projection_granted), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.toast_media_projection_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestMediaProjection() {
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjectionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScrollLoomTheme {
                val repository = App.instance.appComponent.loomRepository
                val weavingState by repository.weavingState.collectAsState()
                val isProjectionGranted by repository.isProjectionGranted.collectAsState()
                val exportManager = remember { MediaExportManager(contentResolver) }
                val context = LocalContext.current

                when (val current = weavingState) {
                    is WeavingState.Preview -> {
                        var exportedUri by remember { mutableStateOf<Uri?>(null) }
                        val coroutineScope = rememberCoroutineScope()

                        ScrollPreviewScreen(
                            tiles = current.tiles,
                            onSaveClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val uri = exportedUri ?: exportManager.saveTilesToGallery(current.tiles)
                                    withContext(Dispatchers.Main) {
                                        if (uri != null) {
                                            exportedUri = uri
                                            Toast.makeText(context, "长卷已成功存入相册！", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "保存失败，请检查存储权限", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onShareClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val uri = exportedUri ?: exportManager.saveTilesToGallery(current.tiles)
                                    withContext(Dispatchers.Main) {
                                        if (uri != null) {
                                            exportedUri = uri
                                            val shareIntent = exportManager.createShareIntent(uri)
                                            startActivity(Intent.createChooser(shareIntent, "分享长卷"))
                                        } else {
                                            Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onBackClick = {
                                repository.reset()
                            }
                        )
                    }
                    else -> {
                        MainScreen(
                            uiState = MainUiState.create(weavingState, isProjectionGranted),
                            onRequestMediaProjection = { requestMediaProjection() },
                            onOpenAccessibilitySettings = { openAccessibilitySettings() },
                            onOpenAppDetailsSettings = { openAppDetailsSettings() },
                            onOpenBatteryOptimization = { openBatteryOptimizationSettings() },
                            onCopyAdbCommand = { copyAdbCommand() }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val repository = App.instance.appComponent.loomRepository
        if (repository.weavingState.value is WeavingState.Preview) {
            repository.reset()
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun openAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }

    private fun copyAdbCommand() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            "ScrollLoom ADB Command",
            "adb shell appops set org.scrollloom ACCESS_RESTRICTED_SETTINGS allow"
        )
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.toast_adb_copied), Toast.LENGTH_SHORT).show()
    }
}
