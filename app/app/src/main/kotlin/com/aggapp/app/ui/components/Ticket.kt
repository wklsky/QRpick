package com.aggapp.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.aggapp.app.ui.theme.AggColor
import com.aggapp.app.ui.theme.AggType
import com.aggapp.app.ui.theme.CardShapeMedium
import com.aggapp.app.ui.theme.Dimen
import com.aggapp.app.ui.theme.ShapeControl
import com.aggapp.app.ui.theme.ShapeStamp

/**
 * 「取件凭证」体系的基础零件。
 *
 * 这一组组件的共同前提：界面模拟的是一张热敏纸单据，而不是一块玻璃。
 * 因此卡片一律「纸面 + 1dp 描边 + 零阴影」，可点区域靠描边与留白区分层级，
 * 而不是靠 Material 的 elevation 阴影——阴影会立刻把界面拉回通用模板。
 */

/** 单据小标签：前置一道信号色竖条，像货架标签的色标 */
@Composable
fun EyebrowRow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Spacer(
            Modifier
                .width(3.dp)
                .height(11.dp)
                .background(AggColor.signal),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = AggType.Eyebrow,
            color = color,
        )
    }
}

/** 纸面卡片：零阴影，只有描边与底色 */
@Composable
fun TicketCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShapeMedium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape),
        content = content,
    )
}

/**
 * 虚线撕裂线 + 两侧半圆缺口。
 *
 * 缺口圆用页面底色填充，并依靠卡片自身的圆角裁剪切成半圆——
 * 视觉上就是一张被打了孔的取件单，撕下票根后仍能对齐。
 *
 * @param notchColor 必须传入卡片所在页面的底色，否则缺口会变成一块脏色
 */
@Composable
fun PerforatedDivider(
    notchColor: Color,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.outline,
) {
    Canvas(modifier.fillMaxWidth().height(22.dp)) {
        val centerY = size.height / 2f
        val radius = 7.dp.toPx()
        drawLine(
            color = lineColor,
            start = Offset(radius + 6.dp.toPx(), centerY),
            end = Offset(size.width - radius - 6.dp.toPx(), centerY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(5.dp.toPx(), 5.dp.toPx()),
            ),
        )
        drawCircle(notchColor, radius = radius, center = Offset(0f, centerY))
        drawCircle(notchColor, radius = radius, center = Offset(size.width, centerY))
    }
}

/**
 * 条码字形：空态/占位用的竖条图案。
 *
 * 比通用图标更贴合主题——用户看到的是「没有可出示的码」，
 * 而不是一个与业务无关的收件箱图标。
 */
@Composable
fun BarcodeGlyph(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
) {
    Canvas(modifier) {
        // 固定的宽窄序列：模拟真实的 CODE128 黑白条节奏，避免随机导致每次重组跳动
        val pattern = listOf(2f, 1f, 1f, 3f, 1f, 1f, 2f, 1f, 1f, 3f, 1f, 2f, 1f)
        val gapRatio = 1.4f
        val totalUnits = pattern.sum() + (pattern.size - 1) * gapRatio
        val unit = size.width / totalUnits
        var x = 0f
        pattern.forEachIndexed { index, weight ->
            val barWidth = weight * unit
            drawRect(
                color = color,
                topLeft = Offset(x, 0f),
                size = Size(barWidth, size.height),
            )
            x += barWidth
            if (index < pattern.lastIndex) x += gapRatio * unit
        }
    }
}

/** 印章风格的状态标记：描边近方章，而不是圆润的胶囊 */
@Composable
fun StatusStamp(
    text: String,
    modifier: Modifier = Modifier,
    tone: StampTone = StampTone.Neutral,
) {
    val contentColor = when (tone) {
        StampTone.Signal -> AggColor.OnSignal
        StampTone.Alert -> MaterialTheme.colorScheme.error
        StampTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val containerColor = when (tone) {
        StampTone.Signal -> AggColor.signal
        StampTone.Alert -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        StampTone.Neutral -> Color.Transparent
    }
    val borderColor = when (tone) {
        StampTone.Signal -> Color.Transparent
        StampTone.Alert -> MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
        StampTone.Neutral -> MaterialTheme.colorScheme.outline
    }
    Row(
        modifier
            .clip(ShapeStamp)
            .background(containerColor)
            .border(1.dp, borderColor, ShapeStamp)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text, style = AggType.Stamp, color = contentColor)
    }
}

enum class StampTone { Signal, Neutral, Alert }

/** 主行动按钮：信号黄底 + 近黑文字，像仓库里贴的高可见度操作标 */
@Composable
fun SignalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val signal = AggColor.signal
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = ShapeControl,
        colors = ButtonDefaults.buttonColors(
            containerColor = signal,
            contentColor = AggColor.OnSignal,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        ),
        contentPadding = PaddingValues(horizontal = Dimen.CardPadding, vertical = 10.dp),
        content = content,
    )
}

/** 卡片内的分隔线：与描边同色，保持纸面安静 */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}
