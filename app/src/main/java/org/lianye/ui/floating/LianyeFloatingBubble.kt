package org.lianye.ui.floating

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.lianye.R
import org.lianye.domain.model.WeavingState
import org.lianye.domain.model.ManualCaptureState
import org.lianye.domain.model.ManualGuide
import org.lianye.domain.model.ManualPhase
import org.lianye.ui.common.uiText

@Composable
fun LianyeFloatingBubble(
    state: WeavingState,
    modifier: Modifier = Modifier,
    isCollapsed: Boolean = false,
    isDockedOnRight: Boolean = true,
    onExpandRequest: () -> Unit = {},
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onPreviewClick: () -> Unit,
    hasDraft: Boolean = false,
    isStopping: Boolean = false,
    manualSession: ManualCaptureState? = null
) {
    val colors = MaterialTheme.colorScheme
    val capturing = if (manualSession != null) manualSession.phase in listOf(ManualPhase.TAKING_FIRST, ManualPhase.RECORDING, ManualPhase.GAP, ManualPhase.FINISHING)
        else state is WeavingState.Weaving
    val finishing = isStopping || manualSession?.phase == ManualPhase.FINISHING
    if (isCollapsed && !capturing) {
        Surface(
            onClick = onExpandRequest,
            modifier = modifier.size(48.dp),
            shape = if (isDockedOnRight) RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                else RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outlineVariant),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_main_header_shuttle), uiText("展开截图按钮", "Expand screenshot controls"), tint = colors.primary, modifier = Modifier.size(26.dp))
            }
        }
        return
    }
    if (capturing) {
        Surface(modifier = modifier, shape = RoundedCornerShape(20.dp),
            color = colors.primary, contentColor = colors.onPrimary, shadowElevation = 3.dp) {
            Column {
                val failure = when {
                    manualSession?.message == "temporarily_unmatched" ->
                        uiText("未接上", "Not joined") to uiText("向下滑回一点，稍停", "Swipe down a little; pause")
                    manualSession?.phase == ManualPhase.GAP ->
                        uiText("未接上", "Not joined") to uiText("已保留前段", "Previous content kept")
                    else -> null
                }
                if (failure != null && !finishing) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(failure.first, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        // Recovery instructions live beside the on-screen gesture endpoints.
                        if (manualSession?.guide != ManualGuide.RECOVERY)
                            Text(failure.second, fontSize = 12.sp, color = colors.onPrimary.copy(alpha = 0.8f))
                    }
                }
                TextButton(onClick = onStopClick, enabled = !finishing,
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.onPrimary,
                        disabledContentColor = colors.onPrimary.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 18.dp), modifier = Modifier.heightIn(min = 48.dp)) {
                    Box(Modifier.size(7.dp).background(colors.onPrimary, RoundedCornerShape(1.dp)))
                    Spacer(Modifier.width(9.dp))
                    Text(uiText("结束", "Finish"), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    } else {
        Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = colors.surface,
            contentColor = colors.onSurface, border = BorderStroke(0.5.dp, colors.outlineVariant), shadowElevation = 3.dp) {
            val openDraft = manualSession == null && hasDraft
            Column {
                if (manualSession?.phase == ManualPhase.FIRST_FAILED) {
                    Text(uiText("未截到画面", "No image captured"), fontSize = 12.sp,
                        modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp), color = colors.onSurfaceVariant)
                }
                TextButton(onClick = if (openDraft) onPreviewClick else onStartClick,
                    enabled = manualSession == null || manualSession.phase in listOf(ManualPhase.READY, ManualPhase.FIRST_FAILED),
                    modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 18.dp)) {
                    Icon(painterResource(if (openDraft) R.drawable.ic_bubble_finished else R.drawable.ic_bubble_idle), null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (openDraft) uiText("继续编辑", "Continue draft") else if (manualSession?.phase == ManualPhase.FIRST_FAILED || state is WeavingState.Error) uiText("重新截图", "Try again") else uiText("开始截图", "Capture"),
                        fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
