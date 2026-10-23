package com.aggapp.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * @Author: wj
 * @FilePath: app/app/src/main/kotlin/com/aggapp/app/ui/theme/Shape.kt
 * @Description: 圆角令牌，原内联在 Theme.kt 尾部，迁出后供页面直接引用
 */

/** 大卡 / BottomSheet：24dp */
val CardShapeLarge = RoundedCornerShape(Dimen.RadiusSheet)

/** 中卡（列表项、查询卡）：16dp */
val CardShapeMedium = RoundedCornerShape(Dimen.RadiusCard)

/** 输入类控件（输入框、按钮、分段轨道）：12dp */
val ShapeControl = RoundedCornerShape(Dimen.RadiusControl)

/** 胶囊（状态徽章、倒计时）：代替原 RoundedCornerShape(50) 的魔法值 */
val ShapePill = RoundedCornerShape(percent = 50)

/**
 * 只覆写 medium / large 两档：页面目前仅通过 MaterialTheme.shapes.medium / large 取用，
 * 保留 M3 其余默认值以免引入预期外的形变。
 */
val AggShapes = Shapes(
    medium = CardShapeMedium,
    large = CardShapeLarge,
)
