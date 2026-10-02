package org.scrollloom.ui.main.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.scrollloom.R
import org.scrollloom.ui.common.theme.LoomGreen
import org.scrollloom.ui.common.theme.ScrollLoomTheme

/**
 * 单一决定性触控核心 (Master Hero Control)。
 *
 * 遵循交互设计单一可信来源（Single Source of Truth），
 * 彻底消除并存的 Switch 开关与全宽大按钮，实现 0 冗余控制。
 */
@Composable
fun MasterHeroControl(
    isConnected: Boolean,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateColor by animateColorAsState(
        targetValue = if (isConnected) LoomGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        label = "stateColorAnimation"
    )

    // 呼吸微光动效 (仅在就绪态激活)
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = if (isConnected) 0.35f else 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlowAlpha"
    )

    val (titleRes, descRes, hintRes) = if (isConnected) {
        Triple(
            R.string.status_service_active,
            R.string.status_service_desc_active,
            R.string.action_hint_disable
        )
    } else {
        Triple(
            R.string.status_service_inactive,
            R.string.status_service_desc_inactive,
            R.string.action_hint_enable
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. 唯一决定性触控核心盘 (Hero Tactile Disc)
        Box(
            modifier = Modifier.size(164.dp),
            contentAlignment = Alignment.Center
        ) {
            // 外围微光光晕 (就绪时呈现翡翠绿呼吸光晕)
            if (isConnected) {
                Box(
                    modifier = Modifier
                        .size(164.dp)
                        .background(
                            color = stateColor.copy(alpha = pulseGlowAlpha),
                            shape = CircleShape
                        )
                )
            }

            // 主盘面
            Surface(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (isConnected) 2.5.dp else 1.5.dp,
                    color = stateColor
                ),
                shadowElevation = if (isConnected) 8.dp else 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_main_header_shuttle),
                        contentDescription = stringResource(id = titleRes),
                        modifier = Modifier.size(56.dp),
                        tint = Color.Unspecified
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(id = hintRes),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = if (isConnected) stateColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 状态标题与状态点
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = stateColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = titleRes),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. 单一清晰的人话说明
        Text(
            text = stringResource(id = descRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
            lineHeight = 20.sp
        )
    }
}

// ==================== Previews ====================

@Preview(name = "1. 未启用态 (Light)", group = "MasterHeroControl", showBackground = true)
@Preview(name = "1. 未启用态 (Dark)", group = "MasterHeroControl", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewMasterHeroControlInactive() {
    ScrollLoomTheme(dynamicColor = false) {
        MasterHeroControl(
            isConnected = false,
            onPrimaryAction = {}
        )
    }
}

@Preview(name = "2. 就绪态 (Light)", group = "MasterHeroControl", showBackground = true)
@Preview(name = "2. 就绪态 (Dark)", group = "MasterHeroControl", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewMasterHeroControlActive() {
    ScrollLoomTheme(dynamicColor = false) {
        MasterHeroControl(
            isConnected = true,
            onPrimaryAction = {}
        )
    }
}
