package org.scrollloom.ui.floating

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.scrollloom.R
import org.scrollloom.domain.model.WeavingState
import org.scrollloom.ui.common.theme.LoomGreen
import org.scrollloom.ui.common.theme.LoomRed
import org.scrollloom.ui.common.theme.ScrollLoomTheme

// 赛博曜石磨砂玻璃质感基色
private val ObsidianGlassBg = Color(0xF00B1120)
private val ObsidianGlassCollapsedBg = Color(0xA00B1120)
private val SpecularRimBorder = Color(0x3338BDF8)
private val TextWhite = Color(0xFFF1F5F9)
private val TextMuted = Color(0xFF94A3B8)

@Composable
fun LoomFloatingBubble(
    state: WeavingState,
    isCollapsed: Boolean = false,
    isDockedOnRight: Boolean = true,
    onExpandRequest: () -> Unit = {},
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 编织中脉冲动效
    val transition = rememberInfiniteTransition(label = "recordingPulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    AnimatedContent(
        targetState = isCollapsed,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
        label = "bubbleCollapseTransition",
        modifier = modifier
    ) { collapsed ->
        if (collapsed) {
            // ==================== 1. 空闲半收缩微标 (Micro Shuttle Tab) ====================
            val tabShape = if (isDockedOnRight) {
                RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 0.dp, bottomEnd = 0.dp)
            } else {
                RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 20.dp, bottomEnd = 20.dp)
            }

            Surface(
                modifier = Modifier
                    .width(36.dp)
                    .height(44.dp)
                    .clickable(onClick = onExpandRequest),
                shape = tabShape,
                color = ObsidianGlassCollapsedBg,
                border = BorderStroke(1.dp, SpecularRimBorder),
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.65f },
                    contentAlignment = Alignment.Center
                ) {
                    if (state is WeavingState.Weaving) {
                        // 正在录制中的迷你指示点
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .graphicsLayer { alpha = pulseAlpha }
                                .background(LoomRed, CircleShape)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_main_header_shuttle),
                            contentDescription = "展开悬浮窗",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        } else {
            // ==================== 2. 全功能展开操作台 (Expanded HUD Pill) ====================
            Surface(
                modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                shape = RoundedCornerShape(22.dp),
                color = ObsidianGlassBg,
                border = BorderStroke(1.dp, SpecularRimBorder),
                shadowElevation = 8.dp
            ) {
                when (state) {
                    is WeavingState.Idle -> {
                        Row(
                            modifier = Modifier
                                .clickable(onClick = onStartClick)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_main_header_shuttle),
                                contentDescription = stringResource(R.string.bubble_desc_idle),
                                modifier = Modifier.size(20.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "开始长卷",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }
                    }

                    is WeavingState.Weaving -> {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 动态录制脉冲光点
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .graphicsLayer { alpha = pulseAlpha }
                                    .background(LoomRed, CircleShape)
                            )
                            Text(
                                text = "已织 ${state.frameCount} 屏",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextWhite
                            )
                            // 独立触控结束按键 (明确的物理打击面)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(LoomRed.copy(alpha = 0.25f))
                                    .clickable(onClick = onStopClick)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "■ 结束",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LoomRed
                                )
                            }
                        }
                    }

                    is WeavingState.Preview -> {
                        Row(
                            modifier = Modifier
                                .clickable(onClick = onPreviewClick)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_bubble_finished),
                                contentDescription = stringResource(R.string.bubble_desc_finished),
                                modifier = Modifier.size(18.dp),
                                tint = LoomGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "长卷就绪 · 轻触查看 ›",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LoomGreen
                            )
                        }
                    }

                    is WeavingState.Error -> {
                        Row(
                            modifier = Modifier
                                .clickable(onClick = onStartClick)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_bubble_error),
                                contentDescription = stringResource(R.string.bubble_desc_error),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "拼接中断 · 重试",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== Preview 区域 ====================

@Preview(name = "1. 折叠微标态 - 右贴边", group = "FloatingBubble", showBackground = true)
@Composable
private fun PreviewLoomFloatingBubbleCollapsedRight() {
    ScrollLoomTheme(dynamicColor = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            LoomFloatingBubble(
                state = WeavingState.Idle(isServiceConnected = true),
                isCollapsed = true,
                isDockedOnRight = true,
                onStartClick = {},
                onStopClick = {},
                onPreviewClick = {}
            )
        }
    }
}

@Preview(name = "2. 空闲就绪态 (HUD 展开)", group = "FloatingBubble", showBackground = true)
@Composable
private fun PreviewLoomFloatingBubbleIdle() {
    ScrollLoomTheme(dynamicColor = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            LoomFloatingBubble(
                state = WeavingState.Idle(isServiceConnected = true),
                isCollapsed = false,
                onStartClick = {},
                onStopClick = {},
                onPreviewClick = {}
            )
        }
    }
}

@Preview(name = "3. 编织录制中态 (带结束按键)", group = "FloatingBubble", showBackground = true)
@Composable
private fun PreviewLoomFloatingBubbleWeaving() {
    ScrollLoomTheme(dynamicColor = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            LoomFloatingBubble(
                state = WeavingState.Weaving(frameCount = 5),
                isCollapsed = false,
                onStartClick = {},
                onStopClick = {},
                onPreviewClick = {}
            )
        }
    }
}
