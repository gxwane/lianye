package org.scrollloom.ui.floating

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.tooling.preview.Preview
import org.scrollloom.domain.model.WeavingState
import org.scrollloom.ui.common.theme.ScrollLoomTheme

@Composable
fun LoomFloatingBubble(
    state: WeavingState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 6.dp
    ) {
        when (state) {
            is WeavingState.Idle -> {
                IdleBubble(onClick = onStartClick)
            }
            is WeavingState.Weaving -> {
                WeavingBubble(
                    frameCount = state.frameCount,
                    onClick = onStopClick
                )
            }
            is WeavingState.Preview -> {
                FinishedBubble(onClick = onPreviewClick)
            }
            is WeavingState.Error -> {
                ErrorBubble(onClick = onStartClick)
            }
        }
    }
}

@Composable
private fun IdleBubble(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🧶",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "长卷",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun WeavingBubble(frameCount: Int, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer { this.alpha = alpha },
            shape = RoundedCornerShape(4.dp),
            color = Color.Red
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "已织 $frameCount 屏 (完成)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun FinishedBubble(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "✅", fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "查看长卷",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ErrorBubble(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "⚠️", fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "重试",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

// ==================== Preview 区域 ====================

@Preview(name = "1. 空闲态 (Light)", group = "FloatingBubble", showBackground = true)
@Preview(name = "1. 空闲态 (Dark)", group = "FloatingBubble", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewLoomFloatingBubbleIdle() {
    ScrollLoomTheme(dynamicColor = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            LoomFloatingBubble(
                state = WeavingState.Idle(isServiceConnected = true),
                onStartClick = {},
                onStopClick = {},
                onPreviewClick = {}
            )
        }
    }
}

@Preview(name = "2. 编织中 5屏 (Light)", group = "FloatingBubble", showBackground = true)
@Composable
private fun PreviewLoomFloatingBubbleWeaving() {
    ScrollLoomTheme(dynamicColor = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            LoomFloatingBubble(
                state = WeavingState.Weaving(frameCount = 5),
                onStartClick = {},
                onStopClick = {},
                onPreviewClick = {}
            )
        }
    }
}

@Preview(name = "3. 拼接完成 (Light)", group = "FloatingBubble", showBackground = true)
@Composable
private fun PreviewLoomFloatingBubbleFinished() {
    ScrollLoomTheme(dynamicColor = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            LoomFloatingBubble(
                state = WeavingState.Preview(emptyList()),
                onStartClick = {},
                onStopClick = {},
                onPreviewClick = {}
            )
        }
    }
}

@Preview(name = "4. 异常重试 (Light)", group = "FloatingBubble", showBackground = true)
@Composable
private fun PreviewLoomFloatingBubbleError() {
    ScrollLoomTheme(dynamicColor = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            LoomFloatingBubble(
                state = WeavingState.Error("拼接中断"),
                onStartClick = {},
                onStopClick = {},
                onPreviewClick = {}
            )
        }
    }
}

