package com.aggapp.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * @Author: wj
 * @FilePath: app/app/src/main/kotlin/com/aggapp/app/ui/theme/Color.kt
 * @Description: 语义化颜色令牌，承接 colorScheme 未覆盖的业务态配色
 */

/**
 * 说明：Brand 存放"品牌色"（不随主题变化），本对象存放"语义色"（可随主题变化）。
 * 新增颜色必须先判断归属：能映射进 colorScheme 角色的一律走 colorScheme，
 * 只有 colorScheme 无对应语义的才在此登记，避免令牌表无限膨胀。
 */
object AggColor {
    /** 登录态"已登录"：与 error 明确区分，防止用户把绿色误读为异常态 */
    private val LoggedInLight = Color(0xFF34C759)

    /** 深色下的系统绿：亮绿在深底上会过于刺眼，改用低饱和档 */
    private val LoggedInDark = Color(0xFF30D158)

    /**
     * 条码画布：恒为白底黑条。
     *
     * 这是刻意违背深色模式的例外——驿站扫码枪依赖最大对比度识别，
     * 若跟随主题反转为深底浅条，部分老旧设备会直接读不出码。
     * Code Review 时请勿当成"深色适配遗漏"改回 colorScheme.surface。
     */
    val BarcodeCanvas = Color.White
    val BarcodeInk = Color.Black

    /**
     * 品牌色上的前景。
     *
     * Brand.Indigo / Violet 等品牌色本身不随主题变化，因此其上的文字与进度圈
     * 不能用 colorScheme.onPrimary——深色模式下的 onPrimary 是深蓝，压在靛蓝底上会糊。
     */
    val OnBrand = Color.White

    /**
     * 条码卡内部的中性配色。
     *
     * 由于整卡被锁定为浅色，卡内元素（平台名、码号、徽章）同样不能跟随主题——
     * 否则深色模式下会出现"深色徽章压在白卡上、浅色文字看不清"的反向错误。
     * 换言之：条码卡是一个完整的"浅色孤岛"，所有内部配色都必须走这一组令牌。
     */
    val BarcodeBadgeBg = Color(0xFFF1F0F8)
    val BarcodeBadgeFg = Color(0xFF46464F)
    /** 条码渲染失败时的占位块底色，同样需保持浅色 */
    val BarcodePlaceholder = Color(0xFFE1E1E8)

    /**
     * 全屏亮码背景：驿站环境常为暗光，用纯黑最大化条码与背景的对比，
     * 同时避免彩色背景干扰扫码枪的光学识别。
     */
    val BrightCodeBackdrop = Color.Black

    /** 全屏亮码上的文字：直接取白，因为背景恒为 BrightCodeBackdrop 而非主题色 */
    val OnBrightCode = Color.White

    /** 全屏亮码上的次要文字（退出提示、空态说明），压低亮度避免抢走条码注意力 */
    val OnBrightCodeMuted = Color(0xFFB0B0B0)

    val loggedIn: Color
        @Composable
        @ReadOnlyComposable
        get() = if (androidx.compose.foundation.isSystemInDarkTheme()) LoggedInDark else LoggedInLight
}
