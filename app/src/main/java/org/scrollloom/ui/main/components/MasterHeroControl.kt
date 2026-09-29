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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    isRestricted: Boolean,
    onPrimaryAction: () -> Unit,
    onShowAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateColor by animateColorAsState(
        targetValue = when {
            isRestricted -> Color(0xFFF59E0B) // 琥珀警示黄
            isConnected -> LoomGreen          // 就绪翡翠绿
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        },
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

    val (titleRes, descRes, hintRes) = when {
        isRestricted -> Triple(
            R.string.status_service_restricted,
            R.string.status_service_desc_restricted,
            R.string.action_hint_restricted
        )
        isConnected -> Triple(
            R.string.status_service_active,
            R.string.status_service_desc_active,
            R.string.action_hint_disable
        )
        else -> Triple(
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
            // 外围微光光晕 (不抢眼，克制优雅)
            if (isConnected || isRestricted) {
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
                    width = if (isConnected || isRestricted) 2.5.dp else 1.5.dp,
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
                        color = if (isConnected || isRestricted) stateColor else MaterialTheme.colorScheme.onSurfaceVariant
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

        // 4. 受限状态下的排障小药丸 (仅受限时展现，平时 0 噪音)
        if (isRestricted) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                onClick = onShowAbout,
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
            ) {
                Text(
                    text = stringResource(id = R.string.btn_restricted_guide_pill),
                    color = Color(0xFFD97706),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
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
            isRestricted = false,
            onPrimaryAction = {},
            onShowAbout = {}
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
            isRestricted = false,
            onPrimaryAction = {},
            onShowAbout = {}
        )
    }
}

@Preview(name = "3. 受限态 (Light)", group = "MasterHeroControl", showBackground = true)
@Preview(name = "3. 受限态 (Dark)", group = "MasterHeroControl", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewMasterHeroControlRestricted() {
    ScrollLoomTheme(dynamicColor = false) {
        MasterHeroControl(
            isConnected = false,
            isRestricted = true,
            onPrimaryAction = {},
            onShowAbout = {}
        )
    }
}
