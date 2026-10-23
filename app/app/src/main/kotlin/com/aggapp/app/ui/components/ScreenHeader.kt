package com.aggapp.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aggapp.app.ui.theme.AggType
import com.aggapp.app.ui.theme.Dimen

/**
 * @Author: wj
 * @FilePath: app/app/src/main/kotlin/com/aggapp/app/ui/components/ScreenHeader.kt
 * @Description: 三页统一的页头（surface 底 + 状态栏避让 + 主标题/副标题 + 右侧动作区）
 *
 * 刻意不提供整页 Scaffold：
 * 1. 快递页的窗口避让由 MainActivity 的 NavHost 提供，再嵌一层 Scaffold 会双重 padding；
 * 2. 身份码页与我的页的页头处于滚动容器内（滚走即消失），快递页页头固定，
 *    用整页骨架会把前两页的页头改成吸顶，属于超出「统一头部」范围的行为变更。
 *
 * @param title 页面主标题，固定 AggType.ScreenTitle，不开放 style 以免字号再次分叉
 * @param subtitle 副标题，传 null 则整行不渲染（身份码页即无副标题）
 * @param actions 标题行右侧动作区（刷新 / 全屏），由各页自行提供
 * @param headerBottom 标题行下方、仍属同一块 surface 背景的附加控件（身份码页的平台分段控件）
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    headerBottom: @Composable ColumnScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            // 底色必须在 statusBarsPadding 之前：否则状态栏区域会露出页面底色，形成一条色带
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.CardPaddingCompact),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = AggType.ScreenTitle,
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(Dimen.SpaceXS))
                    Text(
                        subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = AggType.ScreenSubtitle,
                    )
                }
            }
            actions()
        }
        headerBottom()
    }
}
