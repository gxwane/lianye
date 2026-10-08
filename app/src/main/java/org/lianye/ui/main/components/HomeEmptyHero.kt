package org.lianye.ui.main.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** The same viewfinder and extending image relationship as the brand mark. */
@Composable
fun HomeEmptyHero(modifier: Modifier = Modifier) {
    LongCaptureIllustration(modifier.fillMaxWidth().height(344.dp))
}
