package com.aggapp.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.aggapp.app.R
import com.aggapp.app.ui.theme.AggType
import com.aggapp.app.ui.theme.Brand
import com.aggapp.app.ui.theme.Dimen
import com.aggapp.app.ui.theme.ShapeControl

/**
 * @Author: wj
 * @FilePath: app/app/src/main/kotlin/com/aggapp/app/ui/components/StateViews.kt
 * @Description: 页面三态（加载 / 空 / 错误）统一视图，替代各页各拼一套的 Text + 进度圈
 *
 * 三个组件都只是「内容块」，不带 fillMaxSize 与居中策略：
 * 是否需要占满剩余空间、是否垂直居中，由调用处按该页布局决定，
 * 否则在卡片内使用时会被强行撑满。
 */

/** 首屏加载 / 重试中：居中进度圈 + 可选说明文案 */
@Composable
fun LoadingState(modifier: Modifier = Modifier, text: String? = null) {
    Column(
        modifier.fillMaxWidth().padding(vertical = Dimen.SpaceXL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = Brand.Indigo)
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
 * 空态：图标 + 说明 + 可选主行动。
 * 必须带图标位——纯一行文字的空态在视觉上无法与「加载失败」区分开。
 */
@Composable
fun EmptyState(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = Dimen.SpaceXL, horizontal = Dimen.CardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 装饰性图标不参与朗读，避免 TalkBack 只念「图标」而漏掉真正的说明文案
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(Dimen.MinTouchTarget),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(Dimen.SpaceM))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(Dimen.SpaceS))
            TextButton(onClick = onAction) {
                Text(actionText, style = AggType.Caption)
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
        Icon(
            Icons.Outlined.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(Dimen.MinTouchTarget),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(Dimen.SpaceM))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Dimen.SpaceM))
        Button(
            onClick = onRetry,
            shape = ShapeControl,
            colors = ButtonDefaults.buttonColors(containerColor = Brand.Indigo),
        ) {
            Text(stringResource(R.string.action_retry))
        }
    }
}
