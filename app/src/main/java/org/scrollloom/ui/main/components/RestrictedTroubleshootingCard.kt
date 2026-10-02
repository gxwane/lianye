package org.scrollloom.ui.main.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import org.scrollloom.ui.common.theme.ScrollLoomTheme
import org.scrollloom.ui.main.RestrictedCardPhase

/**
 * 双向穿梭受限设置智能排障卡片。
 *
 * 1. [RestrictedCardPhase.BLOCKED_GUIDE]: 放行前引导直达应用信息页开启"允许受限制的设置"；
 * 2. [RestrictedCardPhase.ALLOWED_READY_RETURN]: 放行后鼓励并提供一键重返无障碍页面开启服务。
 */
@Composable
fun RestrictedTroubleshootingCard(
    phase: RestrictedCardPhase,
    onOpenAppDetailsSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onCopyAdbCommand: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (phase == RestrictedCardPhase.HIDDEN) return

    val isAllowed = phase == RestrictedCardPhase.ALLOWED_READY_RETURN
    val emeraldGreen = Color(0xFF10B981)

    val containerColor by animateColorAsState(
        targetValue = if (isAllowed) {
            emeraldGreen.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(400),
        label = "containerColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(
                        id = if (isAllowed) R.drawable.ic_guide_sparkle else R.drawable.ic_shield_secure
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isAllowed) emeraldGreen else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        if (isAllowed) R.string.troubleshoot_title_allowed else R.string.troubleshoot_title_blocked
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isAllowed) emeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    if (isAllowed) R.string.troubleshoot_desc_allowed else R.string.troubleshoot_desc_blocked
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )

            if (!isAllowed) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stringResource(R.string.troubleshoot_step_1)}\n${stringResource(R.string.troubleshoot_step_2)}\n${stringResource(R.string.troubleshoot_step_3)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenAppDetailsSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.troubleshoot_btn_grant))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onCopyAdbCommand,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.btn_copy_adb_command), fontSize = 11.sp)
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldGreen)
                ) {
                    Text(text = stringResource(R.string.troubleshoot_btn_return_a11y), color = Color.White)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.troubleshoot_return_hint),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Preview(name = "Blocked Guide", showBackground = true)
@Composable
private fun PreviewTroubleshootingBlocked() {
    ScrollLoomTheme(dynamicColor = false) {
        RestrictedTroubleshootingCard(
            phase = RestrictedCardPhase.BLOCKED_GUIDE,
            onOpenAppDetailsSettings = {},
            onOpenAccessibilitySettings = {},
            onCopyAdbCommand = {}
        )
    }
}

@Preview(name = "Allowed Ready Return", showBackground = true)
@Preview(name = "Allowed Ready Return (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewTroubleshootingAllowed() {
    ScrollLoomTheme(dynamicColor = false) {
        RestrictedTroubleshootingCard(
            phase = RestrictedCardPhase.ALLOWED_READY_RETURN,
            onOpenAppDetailsSettings = {},
            onOpenAccessibilitySettings = {},
            onCopyAdbCommand = {}
        )
    }
}
