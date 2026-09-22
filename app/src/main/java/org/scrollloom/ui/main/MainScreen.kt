package org.scrollloom.ui.main

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.scrollloom.domain.model.WeavingState
import org.scrollloom.ui.common.theme.ScrollLoomTheme
import org.scrollloom.ui.main.components.MainHeader
import org.scrollloom.ui.main.components.MediaProjectionStatusCard
import org.scrollloom.ui.main.components.PrivacyBadgesSection
import org.scrollloom.ui.main.components.RestrictedSettingsCard
import org.scrollloom.ui.main.components.ServiceStatusCard
import org.scrollloom.ui.main.components.UsageGuideCard

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
            Spacer(modifier = Modifier.height(4.dp))

            // 1. 48dp 织梭品牌徽标与标语区 (紧凑高度预算 ~112dp)
            MainHeader()

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 无障碍核心开关状态卡片
            ServiceStatusCard(
                isConnected = uiState.isServiceConnected,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings
            )

            // 3. Android 10 录屏授权卡片 (仅在 Android 10 下展现)
            if (uiState.showMediaProjectionCard) {
                Spacer(modifier = Modifier.height(14.dp))
                MediaProjectionStatusCard(
                    isGranted = uiState.isProjectionGranted,
                    onRequestPermission = onRequestMediaProjection
                )
            }

            // 4. Android 13+ 受限制设置引导卡片 (仅在服务未开启且 Android 13+ 下展现)
            if (uiState.showRestrictedSettingsCard) {
                Spacer(modifier = Modifier.height(14.dp))
                RestrictedSettingsCard(
                    onOpenAppDetailsSettings = onOpenAppDetailsSettings,
                    onOpenBatteryOptimization = onOpenBatteryOptimization,
                    onCopyAdbCommand = onCopyAdbCommand
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. 使用指引向导
            UsageGuideCard()

            Spacer(modifier = Modifier.height(20.dp))

            // 6. 架构级隐私安全承诺微标区
            PrivacyBadgesSection()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==================== Preview 区域 ====================

@Preview(name = "1. 主屏 - 服务已就绪 (Light)", group = "MainScreen", showBackground = true)
@Preview(name = "1. 主屏 - 服务已就绪 (Dark)", group = "MainScreen", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
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

@Preview(name = "2. 主屏 - Android 14 首次受限制设置 (Light)", group = "MainScreen", showBackground = true)
@Preview(name = "2. 主屏 - Android 14 首次受限制设置 (Dark)", group = "MainScreen", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
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

@Preview(name = "3. 主屏 - Android 10 未授权录屏 (Light)", group = "MainScreen", showBackground = true)
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
