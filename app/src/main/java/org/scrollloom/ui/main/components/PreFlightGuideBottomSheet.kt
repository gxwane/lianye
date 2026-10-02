package org.scrollloom.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.scrollloom.R
import org.scrollloom.platform.DeviceVendor
import org.scrollloom.platform.DeviceVendorDetector
import org.scrollloom.ui.common.theme.LoomGreen
import org.scrollloom.ui.common.theme.ScrollLoomTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PreFlightGuideBottomSheet(
    vendor: DeviceVendor = DeviceVendorDetector.currentVendor,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // 头部标题与微标
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_main_header_shuttle),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.preflight_guide_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.preflight_guide_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 路径面包屑导航卡片 (Breadcrumbs)
            val pathSteps = getVendorPathSteps(vendor)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pathSteps.forEachIndexed { index, step ->
                            val isLast = index == pathSteps.lastIndex
                            val isHighlight = step.highlight
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    isLast -> LoomGreen.copy(alpha = 0.15f)
                                    isHighlight -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ) {
                                Text(
                                    text = step.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isLast || isHighlight) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isLast -> LoomGreen
                                        isHighlight -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            if (!isLast) {
                                Text(
                                    text = "›",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 安全背书微贴士
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(LoomGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.preflight_privacy_note),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 单一明确的确认动作按钮
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.preflight_btn_confirm),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

private data class PathStep(val name: String, val highlight: Boolean = false)

private fun getVendorPathSteps(vendor: DeviceVendor): List<PathStep> {
    return when (vendor) {
        DeviceVendor.HUAWEI -> listOf(
            PathStep("设置"),
            PathStep("辅助功能"),
            PathStep("无障碍"),
            PathStep("已安装的服务", highlight = true),
            PathStep("ScrollLoom")
        )
        DeviceVendor.XIAOMI -> listOf(
            PathStep("设置"),
            PathStep("更多设置"),
            PathStep("无障碍"),
            PathStep("通用", highlight = true),
            PathStep("已下载的服务"),
            PathStep("ScrollLoom")
        )
        DeviceVendor.OPPO -> listOf(
            PathStep("设置"),
            PathStep("其他设置"),
            PathStep("无障碍"),
            PathStep("通用", highlight = true),
            PathStep("已下载应用"),
            PathStep("ScrollLoom")
        )
        DeviceVendor.VIVO -> listOf(
            PathStep("设置"),
            PathStep("快捷与辅助"),
            PathStep("无障碍"),
            PathStep("已下载的服务", highlight = true),
            PathStep("ScrollLoom")
        )
        DeviceVendor.SAMSUNG -> listOf(
            PathStep("设置"),
            PathStep("辅助功能"),
            PathStep("已安装的应用程序", highlight = true),
            PathStep("ScrollLoom")
        )
        DeviceVendor.AOSP -> listOf(
            PathStep("设置"),
            PathStep("无障碍"),
            PathStep("已下载的应用", highlight = true),
            PathStep("ScrollLoom")
        )
    }
}

// ==================== Preview ====================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Huawei Path Guide", group = "PreFlightGuide", showBackground = true)
@Composable
private fun PreviewPreFlightGuideHuawei() {
    ScrollLoomTheme(dynamicColor = false) {
        PreFlightGuideBottomSheet(
            vendor = DeviceVendor.HUAWEI,
            onDismissRequest = {},
            onConfirm = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Xiaomi Path Guide", group = "PreFlightGuide", showBackground = true)
@Composable
private fun PreviewPreFlightGuideXiaomi() {
    ScrollLoomTheme(dynamicColor = false) {
        PreFlightGuideBottomSheet(
            vendor = DeviceVendor.XIAOMI,
            onDismissRequest = {},
            onConfirm = {}
        )
    }
}
