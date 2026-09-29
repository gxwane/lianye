package org.scrollloom.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.scrollloom.R
import org.scrollloom.ui.common.theme.ScrollLoomTheme

/**
 * 规范应用顶部导航栏 (Canonical MainTopBar)。
 *
 * 居左：品牌 Logo 与名称。
 * 居右：唯一的全局二级介绍入口 (ⓘ)。
 */
@Composable
fun MainTopBar(
    onShowAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_main_header_shuttle),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

        IconButton(
            onClick = onShowAbout,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_info_circle),
                contentDescription = stringResource(R.string.about_title),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * 兼容旧组件引用的别名或基础 Header
 */
@Composable
fun MainHeader(
    modifier: Modifier = Modifier,
    onShowAbout: () -> Unit = {}
) {
    MainTopBar(onShowAbout = onShowAbout, modifier = modifier)
}

@Preview(name = "MainTopBar (Light)", group = "MainHeader", showBackground = true)
@Preview(name = "MainTopBar (Dark)", group = "MainHeader", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewMainTopBar() {
    ScrollLoomTheme(dynamicColor = false) {
        MainTopBar(onShowAbout = {})
    }
}
