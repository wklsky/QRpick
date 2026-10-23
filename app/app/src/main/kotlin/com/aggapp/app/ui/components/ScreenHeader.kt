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
import androidx.compose.ui.unit.dp
import com.aggapp.app.ui.theme.AggType
import com.aggapp.app.ui.theme.Dimen

/**
 * @Author: wj
 * @FilePath: app/app/src/main/kotlin/com/aggapp/app/ui/components/ScreenHeader.kt
 * @Description: 三页统一的页头（单据抬头：色标小标签 + 大标题 + 底部细线）
 *
 * 刻意不提供整页 Scaffold：
 * 1. 快递页的窗口避让由 MainActivity 的 NavHost 提供，再嵌一层 Scaffold 会双重 padding；
 * 2. 身份码页与我的页的页头处于滚动容器内（滚走即消失），快递页页头固定，
 *    用整页骨架会把前两页的页头改成吸顶，属于超出「统一头部」范围的行为变更。
 *
 * 与旧版的差别：取消整块 surface 色带，页头直接落在台面底色上，
 * 只保留一条底部细线与内容分层——避免「白标题带 + 灰内容带」的断层。
 *
 * @param eyebrow 单据小标签（如「取件凭证」），宽字距，配一道信号色标
 * @param title 页面主标题，固定 AggType.ScreenTitle，不开放 style 以免字号再次分叉
 * @param subtitle 副标题，传 null 则整行不渲染（身份码页即无副标题）
 * @param actions 标题行右侧动作区（刷新 / 全屏），由各页自行提供
 * @param headerBottom 标题行下方的附加控件（身份码页的平台切换）
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    eyebrow: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    headerBottom: @Composable ColumnScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.CardPaddingCompact),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    if (eyebrow != null) {
                        EyebrowRow(eyebrow)
                        Spacer(Modifier.height(Dimen.SpaceS))
                    }
                    Text(
                        title,
                        color = MaterialTheme.colorScheme.onBackground,
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
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}
