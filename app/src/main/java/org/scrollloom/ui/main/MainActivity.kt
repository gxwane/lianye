package org.scrollloom.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.scrollloom.App
import org.scrollloom.R
import org.scrollloom.domain.model.WeavingState
import org.scrollloom.ui.common.theme.LoomGreen
import org.scrollloom.ui.common.theme.LoomRed
import org.scrollloom.ui.common.theme.ScrollLoomTheme

import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.media.projection.MediaProjectionManager
import androidx.activity.result.contract.ActivityResultContracts
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import org.scrollloom.engine.export.MediaExportManager
import org.scrollloom.service.capture.LoomMediaProjectionService
import org.scrollloom.ui.preview.ScrollPreviewScreen

class MainActivity : ComponentActivity() {

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            LoomMediaProjectionService.start(this, result.resultCode, result.data!!)
            Toast.makeText(this, "屏幕捕获已授权，服务已就绪", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "未授予截屏权限，长截屏无法启动", Toast.LENGTH_SHORT).show()
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
                            state = weavingState,
                            isProjectionGranted = isProjectionGranted,
                            onRequestMediaProjection = { requestMediaProjection() },
                            onOpenAccessibilitySettings = { openAccessibilitySettings() },
                            onOpenAppDetailsSettings = { openAppDetailsSettings() },
                            onOpenBatteryOptimization = { openBatteryOptimizationSettings() }
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
}

@Composable
fun MainScreen(
    state: WeavingState,
    isProjectionGranted: Boolean,
    onRequestMediaProjection: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAppDetailsSettings: () -> Unit,
    onOpenBatteryOptimization: () -> Unit,
    modifier: Modifier = Modifier,
    showMediaProjectionCard: Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.R,
    showRestrictedSettingsCard: Boolean = (!((state as? WeavingState.Idle)?.isServiceConnected ?: false)) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
) {
    val isServiceConnected = (state as? WeavingState.Idle)?.isServiceConnected ?: false

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.app_name_zh) + " • 离线纯粹长截屏",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ServiceStatusCard(
                isConnected = isServiceConnected,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings
            )

            if (showMediaProjectionCard) {
                Spacer(modifier = Modifier.height(14.dp))
                MediaProjectionStatusCard(
                    isGranted = isProjectionGranted,
                    onRequestPermission = onRequestMediaProjection
                )
            }

            if (showRestrictedSettingsCard) {
                Spacer(modifier = Modifier.height(14.dp))
                RestrictedSettingsCard(
                    onOpenAppDetailsSettings = onOpenAppDetailsSettings,
                    onOpenBatteryOptimization = onOpenBatteryOptimization
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            UsageGuideCard()

            Spacer(modifier = Modifier.height(24.dp))

            PrivacyBadgesSection()
        }
    }
}

@Composable
fun MediaProjectionStatusCard(
    isGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(12.dp)
                        .background(
                            color = if (isGranted) LoomGreen else LoomRed,
                            shape = RoundedCornerShape(6.dp)
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isGranted) "屏幕捕获已就绪 (Android 10)" else "屏幕捕获未授权 (Android 10)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Android 10 (API 29) 需一次性授予截屏授权以建立本地录制通道。ScrollLoom 绝不上网，100% 离线运行。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "授权屏幕捕获")
                }
            }
        }
    }
}

@Composable
fun ServiceStatusCard(
    isConnected: Boolean,
    onOpenAccessibilitySettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(12.dp)
                        .background(
                            color = if (isConnected) LoomGreen else LoomRed,
                            shape = RoundedCornerShape(6.dp)
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isConnected) {
                        stringResource(R.string.status_service_active)
                    } else {
                        stringResource(R.string.status_service_inactive)
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.accessibility_service_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (!isConnected) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.btn_enable_service))
                }
            }
        }
    }
}

@Composable
fun RestrictedSettingsCard(
    onOpenAppDetailsSettings: () -> Unit,
    onOpenBatteryOptimization: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val adbCommand = "adb shell appops set org.scrollloom ACCESS_RESTRICTED_SETTINGS allow"
    val copiedToastText = stringResource(R.string.toast_adb_copied)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🛡️ " + stringResource(R.string.btn_restricted_settings_guide),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "针对 Android 13+ 侧载应用，系统默认限制无障碍开关开启。若开关置灰不可点：\n1. 点击下方按钮直达「应用信息」；\n2. 点击右上角「⋮ 更多」；\n3. 选择「允许受限制的设置」并验证锁屏凭据。",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onOpenAppDetailsSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "打开应用信息以解锁")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(adbCommand))
                        Toast.makeText(context, copiedToastText, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.btn_copy_adb_command), fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onOpenBatteryOptimization,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.btn_battery_optimization), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun UsageGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "✨ " + stringResource(R.string.guide_title),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.guide_step_1),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.guide_step_2),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.guide_step_3),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.guide_step_4),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun PrivacyBadgesSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "架构级安全承诺",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BadgeItem(title = "零网络权限", subtitle = "无INTERNET")
            BadgeItem(title = "失明无障碍", subtitle = "无文本读取")
            BadgeItem(title = "100% 离线", subtitle = "本地瓦片")
        }
    }
}

@Composable
fun BadgeItem(title: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ==================== 卡片独立 Preview 区域 ====================

@Preview(name = "服务状态 - 激活与未激活", group = "Cards", showBackground = true)
@Composable
private fun PreviewServiceStatusCard() {
    ScrollLoomTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ServiceStatusCard(isConnected = true, onOpenAccessibilitySettings = {})
            ServiceStatusCard(isConnected = false, onOpenAccessibilitySettings = {})
        }
    }
}

@Preview(name = "录屏授权状态 (Android 10)", group = "Cards", showBackground = true)
@Composable
private fun PreviewMediaProjectionStatusCard() {
    ScrollLoomTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MediaProjectionStatusCard(isGranted = false, onRequestPermission = {})
            MediaProjectionStatusCard(isGranted = true, onRequestPermission = {})
        }
    }
}

@Preview(name = "受限设置向导 (Android 13+)", group = "Cards", showBackground = true)
@Composable
private fun PreviewRestrictedSettingsCard() {
    ScrollLoomTheme(dynamicColor = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            RestrictedSettingsCard(onOpenAppDetailsSettings = {}, onOpenBatteryOptimization = {})
        }
    }
}

@Preview(name = "上手指引与隐私徽章", group = "Cards", showBackground = true)
@Composable
private fun PreviewUsageGuideAndBadges() {
    ScrollLoomTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            UsageGuideCard()
            Spacer(modifier = Modifier.height(16.dp))
            PrivacyBadgesSection()
        }
    }
}

// ==================== 全屏场景 Preview 区域 ====================

@Preview(name = "主屏 - 服务已就绪 (Light)", group = "Screens", showSystemUi = true)
@Composable
private fun PreviewMainScreenConnected() {
    ScrollLoomTheme(dynamicColor = false) {
        MainScreen(
            state = WeavingState.Idle(isServiceConnected = true),
            isProjectionGranted = true,
            onRequestMediaProjection = {},
            onOpenAccessibilitySettings = {},
            onOpenAppDetailsSettings = {},
            onOpenBatteryOptimization = {},
            showMediaProjectionCard = false,
            showRestrictedSettingsCard = false
        )
    }
}

@Preview(name = "主屏 - 首次安装受限制 (Android 14)", group = "Screens", showSystemUi = true)
@Composable
private fun PreviewMainScreenRestricted() {
    ScrollLoomTheme(dynamicColor = false) {
        MainScreen(
            state = WeavingState.Idle(isServiceConnected = false),
            isProjectionGranted = false,
            onRequestMediaProjection = {},
            onOpenAccessibilitySettings = {},
            onOpenAppDetailsSettings = {},
            onOpenBatteryOptimization = {},
            showMediaProjectionCard = false,
            showRestrictedSettingsCard = true
        )
    }
}

@Preview(name = "主屏 - 深色模式", group = "Screens", uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun PreviewMainScreenDarkMode() {
    ScrollLoomTheme(darkTheme = true, dynamicColor = false) {
        MainScreen(
            state = WeavingState.Idle(isServiceConnected = true),
            isProjectionGranted = true,
            onRequestMediaProjection = {},
            onOpenAccessibilitySettings = {},
            onOpenAppDetailsSettings = {},
            onOpenBatteryOptimization = {},
            showMediaProjectionCard = false,
            showRestrictedSettingsCard = false
        )
    }
}

