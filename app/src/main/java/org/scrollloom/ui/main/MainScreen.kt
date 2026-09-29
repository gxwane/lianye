package org.scrollloom.ui.main

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.scrollloom.domain.model.WeavingState
import org.scrollloom.ui.common.theme.ScrollLoomTheme
import org.scrollloom.ui.main.components.AboutBottomSheet
import org.scrollloom.ui.main.components.MainTopBar
import org.scrollloom.ui.main.components.MasterHeroControl
import org.scrollloom.ui.main.components.MediaProjectionStatusCard

/**
 * 纯粹 UI 状态模型，集中计算系统版本逻辑，彻底隔离平台依赖与 Compose 渲染。
 */
data class MainUiState(
    val isServiceConnected: Boolean,
    val isProjectionGranted: Boolean,
    val showMediaProjectionCard: Boolean,
    val showRestrictedSettingsCard: Boolean
) {
    companion object {
        fun create(
            weavingState: WeavingState,
            isProjectionGranted: Boolean
        ): MainUiState {
            val isConnected = (weavingState as? WeavingState.Idle)?.isServiceConnected ?: false
            return MainUiState(
                isServiceConnected = isConnected,
                isProjectionGranted = isProjectionGranted,
                showMediaProjectionCard = Build.VERSION.SDK_INT < Build.VERSION_CODES.R,
                showRestrictedSettingsCard = !isConnected && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            )
        }
    }
}

/**
 * 零冗余极简主控屏 (Zero-Redundancy Primary Console)。
 *
 * 1. 顶部：规范 TopAppBar，唯一正规二级入口 (ⓘ)；
 * 2. 居中：单一决定性触控核心 (MasterHeroControl)，绝无打架并存控件；
 * 3. 二级：受限排障与关于信息收敛于清晰的 AboutBottomSheet。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onRequestMediaProjection: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAppDetailsSettings: () -> Unit,
    onOpenBatteryOptimization: () -> Unit,
    onCopyAdbCommand: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAboutSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            MainTopBar(
                onShowAbout = { showAboutSheet = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Android 10 屏幕捕获授权卡片 (仅在 Android 10 且未授权时展示)
                if (uiState.showMediaProjectionCard) {
                    MediaProjectionStatusCard(
                        isGranted = uiState.isProjectionGranted,
                        onRequestPermission = onRequestMediaProjection
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 核心交互：单一决定性触控核心仪 (MasterHeroControl)
                MasterHeroControl(
                    isConnected = uiState.isServiceConnected,
                    isRestricted = uiState.showRestrictedSettingsCard,
                    onPrimaryAction = {
                        if (uiState.showRestrictedSettingsCard) {
                            onOpenAppDetailsSettings()
                        } else {
                            onOpenAccessibilitySettings()
                        }
                    },
                    onShowAbout = {
                        showAboutSheet = true
                    }
                )
            }
        }

        // 二级关于与说明底板 (承载必要结构化说明与受限解锁排障)
        if (showAboutSheet) {
            AboutBottomSheet(
                onDismissRequest = { showAboutSheet = false },
                onOpenAppDetailsSettings = onOpenAppDetailsSettings,
                onOpenBatteryOptimization = onOpenBatteryOptimization,
                onCopyAdbCommand = onCopyAdbCommand
            )
        }
    }
}

// ==================== Preview 区域 ====================

@Preview(name = "1. 主屏 - 未启用 (Light)", group = "MainScreen", showBackground = true)
@Preview(name = "1. 主屏 - 未启用 (Dark)", group = "MainScreen", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewMainScreenInactive() {
    ScrollLoomTheme(dynamicColor = false) {
        MainScreen(
            uiState = MainUiState(
                isServiceConnected = false,
                isProjectionGranted = true,
                showMediaProjectionCard = false,
                showRestrictedSettingsCard = false
            ),
            onRequestMediaProjection = {},
            onOpenAccessibilitySettings = {},
            onOpenAppDetailsSettings = {},
            onOpenBatteryOptimization = {},
            onCopyAdbCommand = {}
        )
    }
}

@Preview(name = "2. 主屏 - 服务已就绪 (Light)", group = "MainScreen", showBackground = true)
@Preview(name = "2. 主屏 - 服务已就绪 (Dark)", group = "MainScreen", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewMainScreenActive() {
    ScrollLoomTheme(dynamicColor = false) {
        MainScreen(
            uiState = MainUiState(
                isServiceConnected = true,
                isProjectionGranted = true,
                showMediaProjectionCard = false,
                showRestrictedSettingsCard = false
            ),
            onRequestMediaProjection = {},
            onOpenAccessibilitySettings = {},
            onOpenAppDetailsSettings = {},
            onOpenBatteryOptimization = {},
            onCopyAdbCommand = {}
        )
    }
}

@Preview(name = "3. 主屏 - Android 14 首次受限制设置 (Light)", group = "MainScreen", showBackground = true)
@Preview(name = "3. 主屏 - Android 14 首次受限制设置 (Dark)", group = "MainScreen", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewMainScreenRestrictedSettings() {
    ScrollLoomTheme(dynamicColor = false) {
        MainScreen(
            uiState = MainUiState(
                isServiceConnected = false,
                isProjectionGranted = true,
                showMediaProjectionCard = false,
                showRestrictedSettingsCard = true
            ),
            onRequestMediaProjection = {},
            onOpenAccessibilitySettings = {},
            onOpenAppDetailsSettings = {},
            onOpenBatteryOptimization = {},
            onCopyAdbCommand = {}
        )
    }
}

@Preview(name = "4. 主屏 - Android 10 未授权录屏 (Light)", group = "MainScreen", showBackground = true)
@Composable
private fun PreviewMainScreenAndroid10Unauthenticated() {
    ScrollLoomTheme(dynamicColor = false) {
        MainScreen(
            uiState = MainUiState(
                isServiceConnected = false,
                isProjectionGranted = false,
                showMediaProjectionCard = true,
                showRestrictedSettingsCard = false
            ),
            onRequestMediaProjection = {},
            onOpenAccessibilitySettings = {},
            onOpenAppDetailsSettings = {},
            onOpenBatteryOptimization = {},
            onCopyAdbCommand = {}
        )
    }
}
