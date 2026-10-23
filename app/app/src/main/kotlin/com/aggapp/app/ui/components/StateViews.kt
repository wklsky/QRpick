package com.aggapp.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aggapp.app.R
import com.aggapp.app.ui.theme.AggColor
import com.aggapp.app.ui.theme.AggType
import com.aggapp.app.ui.theme.Dimen

/**
 * @Author: wj
 * @FilePath: app/app/src/main/kotlin/com/aggapp/app/ui/components/StateViews.kt
 * @Description: 页面三态（加载 / 空 / 错误）统一视图，替代各页各拼一套的 Text + 进度圈
 *
 * 三个组件都只是「内容块」，不带 fillMaxSize 与居中策略：
 * 是否需要占满剩余空间、是否垂直居中，由调用处按该页布局决定，
 * 否则在卡片内使用时会被强行撑满。
 *
 * 空态统一使用条码字形而非通用图标：用户缺失的是「码」，不是「收件箱」。
 */

/** 首屏加载 / 重试中：居中进度圈 + 可选说明文案 */
@Composable
fun LoadingState(modifier: Modifier = Modifier, text: String? = null) {
    Column(
        modifier.fillMaxWidth().padding(vertical = Dimen.SpaceXL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            color = AggColor.signal,
            strokeWidth = 2.dp,
            modifier = Modifier.size(28.dp),
        )
        if (text != null) {
            Spacer(Modifier.height(Dimen.SpaceM))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 空态：条码字形 + 说明 + 可选主行动。
 * 必须带图形位——纯一行文字的空态在视觉上无法与「加载失败」区分开。
 */
@Composable
fun EmptyState(
    text: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = Dimen.SpaceXL, horizontal = Dimen.CardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 装饰性图形不参与朗读，避免 TalkBack 只念「图标」而漏掉真正的说明文案
        BarcodeGlyph(
            Modifier
                .size(width = 84.dp, height = 40.dp),
        )
        Spacer(Modifier.height(Dimen.SpaceL))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(Dimen.SpaceS))
            SignalButton(onClick = onAction) {
                Text(actionText, style = AggType.PlatformLabel)
            }
        }
    }
}

/** 错误态：错误文案 + 重试按钮。重试是唯一出口，不允许只展示错误不给出路 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = Dimen.SpaceXL, horizontal = Dimen.CardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BarcodeGlyph(
            Modifier.size(width = 84.dp, height = 40.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.55f),
        )
        Spacer(Modifier.height(Dimen.SpaceL))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Dimen.SpaceM))
        SignalButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}
