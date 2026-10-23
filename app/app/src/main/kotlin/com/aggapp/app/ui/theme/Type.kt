package com.aggapp.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * @Author: wj
 * @FilePath: app/app/src/main/kotlin/com/aggapp/app/ui/theme/Type.kt
 * @Description: 排版令牌，集中定义字号梯度，替换页面散落的 fontSize = X.sp
 */

/**
 * 标准角色保持与 Material3 默认值一致——本次只做"集中声明"，不改字号，
 * 避免设计系统落地时产生预期外的视觉回归。后续要调整梯度只改此处。
 */
val AggTypography = Typography(
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp),
)

/**
 * 非标准语义的专属字号。
 *
 * M3 标准角色没有"取件码 / 身份码"这类需要在驿站隔着玻璃快速辨认的超大等宽数字档位，
 * 历史上各页直接用 fontSize = 20/22/24/26/30.sp 硬编码，导致同层级数字字号不统一。
 * 此处按"展示场景"命名而非按"字号"命名，避免出现 CodeText24 这种无语义的令牌。
 */
object AggType {
    /** 身份码卡片内的码号 */
    val CodeNumber = TextStyle(
        fontSize = 20.sp,
        letterSpacing = 3.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
    )

    /** 全屏亮码页的码号：需远距离可读，取最大档 */
    val CodeNumberFullscreen = TextStyle(
        fontSize = 26.sp,
        letterSpacing = 3.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
    )

    /** 全部平台码弹窗中的码号：一屏五码，字号需收窄以容纳 */
    val CodeNumberCompact = TextStyle(
        fontSize = 13.sp,
        letterSpacing = 2.sp,
        fontFamily = FontFamily.Monospace,
    )

    /** 搜索结果区的取件码：单条突出展示，全页最大字 */
    val PickupCodeHero = TextStyle(
        fontSize = 30.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
    )

    /** 列表行内的取件码 */
    val PickupCodeRow = TextStyle(
        fontSize = 24.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
    )

    /** 详情卡内的取件码 */
    val PickupCodeDetail = TextStyle(
        fontSize = 22.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
    )

    /** 运单号等长串编号：等宽 + 小字号，靠字形对齐而非字号突出 */
    val TrackingNo = TextStyle(
        fontSize = 14.sp,
        fontFamily = FontFamily.Monospace,
    )

    /** 平台名（徽标行、分段控件） */
    val PlatformLabel = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
    )

    /** 分段控件项：五项等分屏宽，字号需保守以防大字体截断 */
    val SegmentedLabel = TextStyle(fontSize = 13.sp)

    /** 全屏页平台名 */
    val FullscreenPlatformLabel = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )

    /** 页面主标题 */
    val ScreenTitle = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
    )

    /** 页面副标题 */
    val ScreenSubtitle = TextStyle(fontSize = 13.sp)

    /** 辅助说明 / 提示文案 */
    val Caption = TextStyle(fontSize = 13.sp)
}
