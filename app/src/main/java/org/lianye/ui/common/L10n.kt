package org.lianye.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

@Composable
fun uiText(zh: String, en: String): String = localized(zh, en, LocalConfiguration.current.locales[0])

fun localized(zh: String, en: String, locale: Locale = Locale.getDefault()): String =
    if (locale.language == "zh") zh else en
