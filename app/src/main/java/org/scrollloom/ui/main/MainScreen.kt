package org.scrollloom.ui.main

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.scrollloom.R
import org.scrollloom.ui.common.theme.ScrollLoomTheme
import org.scrollloom.ui.main.components.AboutBottomSheet
import org.scrollloom.ui.main.components.MainTopBar
import org.scrollloom.ui.main.components.MasterHeroControl
import org.scrollloom.ui.main.components.MediaProjectionStatusCard
import org.scrollloom.ui.main.components.RestrictedTroubleshootingCard

/**
 * 零冗余极简主控屏 (Zero-Redundancy Primary Console)。
 *
 * 1. 顶部：规范 TopAppBar，唯一正规二级入口 (ⓘ)；
 * 2. 居中：单一决定性触控核心 (MasterHeroControl)，绝无打架并存控件；
 *    - 未就绪态：轻触开启服务；
 *    - 已就绪态：轻触就地主动注销服务 (disableSelf())，0ms 应用内生效，不跳设置；
 * 3. 辅助：双向穿梭受限排障卡片 (RestrictedTroubleshootingCard) 仅在受阻时渐进展现；
 * 4. 二级：关于与运行说明收敛于清晰的 AboutBottomSheet。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onRequestMediaProjection: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onOpenAppDetailsSettings: () -> Unit,
    onOpenBatteryOptimization: () -> Unit,
    onCopyAdbCommand: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAboutSheet by remember { mutableStateOf(false) }

    // 触觉正反馈上升沿监听：从未连接跃迁至连接时触发机械触感震动
    var wasConnected by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.isServiceConnected) {
        if (wasConnected == false && uiState.isServiceConnected) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasConnected = uiState.isServiceConnected
    }

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
                // Android 10 屏幕捕获授权卡片 (授权成功后平滑淡出与收起，绝不常驻污染主界面)
                AnimatedVisibility(
                    visible = uiState.showMediaProjectionCard,
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        MediaProjectionStatusCard(
                            isGranted = uiState.isProjectionGranted,
                            onRequestPermission = onRequestMediaProjection
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // 核心交互：单一决定性触控核心仪 (MasterHeroControl)
                // 就绪态时点击就地停用服务 (disableSelf())，未就绪态时开启服务
                MasterHeroControl(
                    isConnected = uiState.isServiceConnected,
                    onPrimaryAction = {
                        if (uiState.isServiceConnected) {
                            onStopService()
                        } else {
                            onStartService()
                        }
                    }
                )

                // 渐进式排障：双向穿梭受限制排障卡片 (仅受阻时平滑展现)
                if (uiState.restrictedCardPhase != RestrictedCardPhase.HIDDEN) {
                    Spacer(modifier = Modifier.height(16.dp))
                    RestrictedTroubleshootingCard(
                        phase = uiState.restrictedCardPhase,
                        onOpenAppDetailsSettings = onOpenAppDetailsSettings,
                        onOpenAccessibilitySettings = onStartService,
                        onCopyAdbCommand = onCopyAdbCommand
                    )
                }
            }
        }

        // 二级关于与说明底板
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
                restrictedCardPhase = RestrictedCardPhase.HIDDEN,
                preFlightHintRes = R.string.vendor_hint_aosp
            ),
            onRequestMediaProjection = {},
            onStartService = {},
            onStopService = {},
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
                restrictedCardPhase = RestrictedCardPhase.HIDDEN,
                preFlightHintRes = R.string.vendor_hint_aosp
            ),
            onRequestMediaProjection = {},
            onStartService = {},
            onStopService = {},
            onOpenAppDetailsSettings = {},
            onOpenBatteryOptimization = {},
            onCopyAdbCommand = {}
        )
    }
}

@Preview(name = "3. 主屏 - 遭遇受限排障态 (Light)", group = "MainScreen", showBackground = true)
@Preview(name = "3. 主屏 - 遭遇受限排障态 (Dark)", group = "MainScreen", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewMainScreenRestrictedBlocked() {
    ScrollLoomTheme(dynamicColor = false) {
        MainScreen(
            uiState = MainUiState(
                isServiceConnected = false,
                isProjectionGranted = true,
                showMediaProjectionCard = false,
                restrictedCardPhase = RestrictedCardPhase.BLOCKED_GUIDE,
                preFlightHintRes = R.string.vendor_hint_xiaomi
            ),
            onRequestMediaProjection = {},
            onStartService = {},
            onStopService = {},
            onOpenAppDetailsSettings = {},
            onOpenBatteryOptimization = {},
            onCopyAdbCommand = {}
        )
    }
}

@Preview(name = "4. 主屏 - 受限已解除准备返回态 (Light)", group = "MainScreen", showBackground = true)
@Composable
private fun PreviewMainScreenRestrictedAllowed() {
    ScrollLoomTheme(dynamicColor = false) {
        MainScreen(
            uiState = MainUiState(
                isServiceConnected = false,
                isProjectionGranted = true,
                showMediaProjectionCard = false,
                restrictedCardPhase = RestrictedCardPhase.ALLOWED_READY_RETURN,
                preFlightHintRes = R.string.vendor_hint_oppo
            ),
            onRequestMediaProjection = {},
            onStartService = {},
            onStopService = {},
            onOpenAppDetailsSettings = {},
            onOpenBatteryOptimization = {},
            onCopyAdbCommand = {}
        )
    }
}
