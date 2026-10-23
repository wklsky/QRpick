package com.aggapp.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 取件凭证（Pickup Pass）配色。
 *
 * 整套界面只有两种"材质"：冷灰的台面（background）与白雪的标签纸（surface），
 * 文字一律是油墨黑/纸白；唯一的彩色是仓库地标黄（secondary）与平台身份色圆点。
 * 主色 primary 刻意设为中性墨色，让 Material 组件（输入框聚焦、文字按钮）自动保持安静，
 * 真正的主行动按钮再显式套用信号黄。
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF12151A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8EAEE),
    onPrimaryContainer = Color(0xFF12151A),
    secondary = Color(0xFFFFC400),
    onSecondary = Color(0xFF111318),
    secondaryContainer = Color(0xFFFFF0B8),
    onSecondaryContainer = Color(0xFF3A2E00),
    tertiary = Color(0xFFC4372B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF8E4E0),
    onTertiaryContainer = Color(0xFF4A100B),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF8E4E0),
    onErrorContainer = Color(0xFF4A100B),
    background = Color(0xFFECEEF1),
    onBackground = Color(0xFF12151A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF12151A),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF5B626C),
    outline = Color(0xFFC7CBD2),
    outlineVariant = Color(0xFFE2E5E9),
    surfaceTint = Color.Transparent,
    // 弹窗/菜单默认取 surfaceContainerHigh，必须显式中性化，
    // 否则会落到 Material 基线的淡紫色，与新配色打架
    surfaceDim = Color(0xFFDCDFE4),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F8FA),
    surfaceContainer = Color(0xFFF1F3F5),
    surfaceContainerHigh = Color(0xFFE9ECEF),
    surfaceContainerHighest = Color(0xFFE2E5E9),
    inverseSurface = Color(0xFF2A2E33),
    inverseOnSurface = Color(0xFFF2F3F5),
    inversePrimary = Color(0xFFFFD43B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF2F3F5),
    onPrimary = Color(0xFF0A0C0F),
    primaryContainer = Color(0xFF232830),
    onPrimaryContainer = Color(0xFFF2F3F5),
    secondary = Color(0xFFFFD43B),
    onSecondary = Color(0xFF141414),
    secondaryContainer = Color(0xFF3A3000),
    onSecondaryContainer = Color(0xFFFFE58A),
    tertiary = Color(0xFFFF6B5E),
    onTertiary = Color(0xFF2A0603),
    tertiaryContainer = Color(0xFF4A1712),
    onTertiaryContainer = Color(0xFFFFDAD5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0A0C0F),
    onBackground = Color(0xFFF2F3F5),
    surface = Color(0xFF15181D),
    onSurface = Color(0xFFF2F3F5),
    surfaceVariant = Color(0xFF1C2026),
    onSurfaceVariant = Color(0xFF9AA1AB),
    outline = Color(0xFF3A4049),
    outlineVariant = Color(0xFF262B32),
    surfaceTint = Color.Transparent,
    surfaceDim = Color(0xFF0A0C0F),
    surfaceBright = Color(0xFF2A3038),
    surfaceContainerLowest = Color(0xFF07090B),
    surfaceContainerLow = Color(0xFF121519),
    surfaceContainer = Color(0xFF171B20),
    surfaceContainerHigh = Color(0xFF1E2228),
    surfaceContainerHighest = Color(0xFF252A31),
    inverseSurface = Color(0xFFF2F3F5),
    inverseOnSurface = Color(0xFF12151A),
    inversePrimary = Color(0xFF12151A),
)

@Composable
fun AggTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // 状态栏与页面同色，避免旧主题留下一条灰带；图标颜色随深浅切换
            view.context.findActivity()?.window?.let { window ->
                window.statusBarColor = colors.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            }
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = AggTypography,
        shapes = AggShapes,
        content = content,
    )
}

/** Compose 的 LocalView.context 常被 ContextThemeWrapper 包裹，需逐层取出 Activity */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
