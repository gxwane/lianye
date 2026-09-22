package org.scrollloom.ui.main.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

@Composable
fun PrivacyBadgesSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.privacy_section_title),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BadgeItem(
                iconRes = R.drawable.ic_privacy_zero_net,
                title = stringResource(R.string.privacy_badge_zero_net),
                subtitle = stringResource(R.string.privacy_badge_zero_net_sub),
                modifier = Modifier.weight(1f)
            )
            BadgeItem(
                iconRes = R.drawable.ic_privacy_blind,
                title = stringResource(R.string.privacy_badge_blind),
                subtitle = stringResource(R.string.privacy_badge_blind_sub),
                modifier = Modifier.weight(1f)
            )
            BadgeItem(
                iconRes = R.drawable.ic_privacy_offline,
                title = stringResource(R.string.privacy_badge_local),
                subtitle = stringResource(R.string.privacy_badge_local_sub),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun BadgeItem(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Preview(name = "PrivacyBadgesSection (Light)", group = "PrivacyBadges", showBackground = true)
@Preview(name = "PrivacyBadgesSection (Dark)", group = "PrivacyBadges", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewPrivacyBadgesSection() {
    ScrollLoomTheme(dynamicColor = false) {
        PrivacyBadgesSection(modifier = Modifier.padding(16.dp))
    }
}
