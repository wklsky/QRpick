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
 *
 * 「取件凭证」体系的排版不依赖外挂字体（中文字体包体积大且与系统字重不匹配），
 * 而是靠三组对比建立性格：
 * 1. 宽字距的中文小标签（Eyebrow / Stamp）——像货架标签与单据抬头；
 * 2. 等宽数字（所有单号、取件码、倒计时）——像热敏打印机的输出；
 * 3. 紧字距的大标题——与宽字距小标签形成一紧一松的节奏。
 */

val AggTypography = Typography(
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
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
        fontSize = 22.sp,
        letterSpacing = 2.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
    )

    /** 全屏亮码页的码号：需远距离可读，取最大档 */
    val CodeNumberFullscreen = TextStyle(
        fontSize = 28.sp,
        letterSpacing = 3.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
    )

    /** 全部平台码弹窗中的码号：一屏五码，字号需收窄以容纳 */
    val CodeNumberCompact = TextStyle(
        fontSize = 12.sp,
        letterSpacing = 1.5.sp,
        fontFamily = FontFamily.Monospace,
    )

    /** 搜索结果区的取件码：单条突出展示，全页最大字 */
    val PickupCodeHero = TextStyle(
        fontSize = 34.sp,
        letterSpacing = 1.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
    )

    /** 列表行内的取件码 */
    val PickupCodeRow = TextStyle(
        fontSize = 26.sp,
        letterSpacing = 1.sp,
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
        fontSize = 13.sp,
        letterSpacing = 0.4.sp,
        fontFamily = FontFamily.Monospace,
    )

    /** 倒计时、时间等短数据 */
    val MonoData = TextStyle(
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
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
        letterSpacing = 2.sp,
        fontWeight = FontWeight.Bold,
    )

    /** 页面主标题：紧字距大标题，与宽字距小标签形成节奏 */
    val ScreenTitle = TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        fontWeight = FontWeight.ExtraBold,
    )

    /** 页面副标题 */
    val ScreenSubtitle = TextStyle(fontSize = 13.sp, letterSpacing = 0.2.sp)

    /** 辅助说明 / 提示文案 */
    val Caption = TextStyle(fontSize = 12.sp)

    /** 单据抬头式小标签：宽字距，全部大写字感 */
    val Eyebrow = TextStyle(
        fontSize = 10.sp,
        letterSpacing = 3.sp,
        fontWeight = FontWeight.Medium,
    )

    /** 状态印章：小字号 + 微字距 + 描边胶囊 */
    val Stamp = TextStyle(
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        fontWeight = FontWeight.Medium,
    )
}
