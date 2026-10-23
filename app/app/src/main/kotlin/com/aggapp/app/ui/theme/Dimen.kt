package com.aggapp.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * @Author: wj
 * @FilePath: app/app/src/main/kotlin/com/aggapp/app/ui/theme/Dimen.kt
 * @Description: 尺寸令牌：统一页面外边距、间隙梯度、圆角与最小触控区
 */

/**
 * 页面原有 16dp / 20dp 两种外边距并存、卡片内边距 14dp / 16dp 并存，
 * 视觉上同层级元素的留白不一致。此处收敛为单一取值，页面只准引用令牌。
 */
object Dimen {
    /** 页面水平外边距 */
    val ScreenPadding = 20.dp
    /** 卡片内边距 */
    val CardPadding = 16.dp
    /** 列表项等紧凑卡片的内边距（沿用历史 14dp，避免行高突变） */
    val CardPaddingCompact = 14.dp

    /** 间隙梯度，全部取 4 的倍数 */
    val SpaceXS = 4.dp
    val SpaceS = 8.dp
    val SpaceM = 12.dp
    val SpaceL = 16.dp
    val SpaceXL = 24.dp

    val RadiusSmall = 8.dp
    /** 输入框 / 按钮 / 分段轨道（原 RoundedCornerShape(12.dp) 重复 6 次） */
    val RadiusControl = 12.dp
    val RadiusCard = 16.dp
    val RadiusSheet = 24.dp

    /**
     * Material 规范要求可点击元素触控区不低于 48dp。
     * 图标按钮若视觉尺寸小于此值，必须靠 padding 补足而非直接缩小。
     */
    val MinTouchTarget = 48.dp
}
