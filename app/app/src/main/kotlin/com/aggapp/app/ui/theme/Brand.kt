package com.aggapp.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 品牌设计令牌（2026-10 重构：取件凭证 / Pickup Pass）。
 *
 * 旧版的蓝青渐变、靛蓝与紫色已全部退役——本应用的核心动作是「把码举给扫码枪」，
 * 界面应当像一张贴在驿站柜台上的取件凭证：纸面、油墨、等宽数字、一条高可见度信号色。
 * 因此品牌层只保留两类东西：高可见度信号色（仓库地标黄）与各平台的身份色。
 */
object Brand {
    /** 信号色：取自仓库地面标线 / 警示带的高可见度黄，只用于主行动与「当前」标记 */
    val Signal = Color(0xFFFFC400)

    /** 信号色上的前景：恒为近黑，保证黄底黑字的最高对比度 */
    val SignalInk = Color(0xFF111318)

    /** 各平台品牌色（仅用于身份圆点，不作为界面装饰色） */
    val PlatformColor = mapOf(
        com.aggapp.core.model.Platform.PDD to Color(0xFFE02E24),
        com.aggapp.core.model.Platform.TAOBAO to Color(0xFFFF6A00),
        com.aggapp.core.model.Platform.JD to Color(0xFFE1251B),
        com.aggapp.core.model.Platform.SF to Color(0xFF00396F),
        com.aggapp.core.model.Platform.RYLL to Color(0xFF00A0E9),
    )
}
