package com.aggapp.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aggapp.app.R
import com.aggapp.app.ui.components.EmptyState
import com.aggapp.app.ui.components.ErrorState
import com.aggapp.app.ui.components.EyebrowRow
import com.aggapp.app.ui.components.Hairline
import com.aggapp.app.ui.components.LoadingState
import com.aggapp.app.ui.components.ScreenHeader
import com.aggapp.app.ui.components.SignalButton
import com.aggapp.app.ui.components.StampTone
import com.aggapp.app.ui.components.StatusStamp
import com.aggapp.app.ui.components.TicketCard
import com.aggapp.app.ui.theme.AggColor
import com.aggapp.app.ui.theme.AggType
import com.aggapp.app.ui.theme.Dimen
import com.aggapp.app.ui.theme.ShapeControl
import com.aggapp.core.model.ExpressStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 快递页：顶部单据抬头 + 运单查询卡 + 按驿站归拢的取件凭证列表
 */
@Composable
fun ExpressScreen(viewModel: ExpressViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsState()
    var waybillInput by remember { mutableStateOf("") }
    var suffixInput by remember { mutableStateOf("") }

    // 每次进入本页自动刷新（VM 内 30s 节流 + 静默合并，不闪 loading）
    LaunchedEffect(Unit) { viewModel.refresh() }

    // 点击页面空白处清除查询结果展示（结果单号已加入跟踪列表）
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(interaction, indication = null) { viewModel.clearSearchResult() },
    ) {
        ScreenHeader(
            eyebrow = stringResource(R.string.eyebrow_express),
            title = stringResource(R.string.express_title),
            subtitle = stringResource(R.string.express_subtitle),
            actions = {
                IconButton(onClick = viewModel::refresh, enabled = !ui.loading) {
                    if (ui.loading) {
                        CircularProgressIndicator(
                            Modifier.size(20.dp),
                            color = AggColor.signal,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            Icons.Filled.Refresh,
                            stringResource(R.string.action_refresh),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = Dimen.ScreenPadding)
                // 尾号验证引导弹出时，点空白处收回（卡片内控件自行消费点击，不受影响）
                .clickable(enabled = ui.needSuffixFor != null) { viewModel.dismissSuffix() },
        ) {
            Spacer(Modifier.height(Dimen.SpaceL))

            // ============ 运单号查询 ============
            TicketCard {
                Column(Modifier.padding(Dimen.CardPaddingCompact)) {
                    EyebrowRow(stringResource(R.string.express_search_eyebrow))
                    Spacer(Modifier.height(Dimen.SpaceM))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = waybillInput,
                            onValueChange = { waybillInput = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.express_label_waybill)) },
                            singleLine = true,
                            shape = ShapeControl,
                            textStyle = AggType.TrackingNo,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                        Spacer(Modifier.width(Dimen.SpaceS))
                        SignalButton(
                            onClick = { viewModel.lookupByTrackingNo(waybillInput) },
                            enabled = waybillInput.isNotBlank() && !ui.searching,
                        ) {
                            if (ui.searching) {
                                CircularProgressIndicator(
                                    Modifier.size(16.dp),
                                    color = AggColor.OnSignal,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Search,
                                    stringResource(R.string.cd_search),
                                    Modifier.size(18.dp),
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.express_action_search))
                        }
                    }

                    ui.searchResult?.let { code ->
                        val copy = rememberCopy()
                        Spacer(Modifier.height(Dimen.SpaceM))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(ShapeControl)
                                .background(AggColor.signal.copy(alpha = 0.16f))
                                .clickable { copy(code.code) }
                                .padding(Dimen.SpaceM),
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.express_code_label),
                                    style = AggType.Eyebrow,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    code.code,
                                    style = AggType.PickupCodeHero,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    stringResource(R.string.express_pickup_code_waybill, code.expressId),
                                    style = AggType.MonoData,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    ui.searchDetail?.let { d ->
                        Spacer(Modifier.height(Dimen.SpaceS))
                        RyllDetailCard(d)
                    }
                    ui.searchNotice?.let { notice ->
                        Spacer(Modifier.height(Dimen.SpaceM))
                        Text(
                            notice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // 手机后4位验证（单号不在列表/详情无码时；可点关闭或空白处收回）
            ui.needSuffixFor?.let { no ->
                Spacer(Modifier.height(Dimen.SpaceM))
                TicketCard {
                    Column(
                        Modifier.padding(
                            start = Dimen.CardPaddingCompact,
                            top = Dimen.SpaceS,
                            end = Dimen.SpaceXS,
                            bottom = Dimen.CardPaddingCompact,
                        ),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 标题是完整句子，不能套 Eyebrow 的宽字距小字号，只沿用信号色标
                            Box(
                                Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .background(AggColor.signal),
                            )
                            Spacer(Modifier.width(Dimen.SpaceS))
                            Text(
                                stringResource(R.string.express_suffix_title, no),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            IconButton(onClick = viewModel::dismissSuffix) {
                                Icon(
                                    Icons.Filled.Close,
                                    stringResource(R.string.express_suffix_dismiss),
                                    Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(Modifier.height(Dimen.SpaceS))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = suffixInput,
                                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) suffixInput = it },
                                modifier = Modifier.weight(1f),
                                label = { Text(stringResource(R.string.express_suffix_label)) },
                                singleLine = true,
                                shape = ShapeControl,
                                textStyle = AggType.MonoData,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            )
                            Spacer(Modifier.width(Dimen.SpaceS))
                            SignalButton(
                                onClick = { viewModel.lookupWithSuffix(suffixInput) },
                                enabled = suffixInput.length == 4 && !ui.searching,
                            ) { Text(stringResource(R.string.express_suffix_action)) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Dimen.SpaceS))

            // ============ 包裹列表（按驿站分组） ============
            // 三态统一走 StateViews：加载居中占位、错误必须给重试出口、空态必须带图标位
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingState()
                    }

                    ui.error != null -> Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        ErrorState(
                            message = ui.error ?: stringResource(R.string.error_unknown),
                            onRetry = viewModel::refresh,
                        )
                    }

                    ui.stations.isEmpty() -> Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        EmptyState(stringResource(R.string.express_empty))
                    }

                    else -> LazyColumn(Modifier.fillMaxSize()) {
                        ui.stations.forEach { group ->
                            item(key = "header_${group.stationName}") {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = Dimen.SpaceL, bottom = Dimen.SpaceS),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        Modifier
                                            .width(3.dp)
                                            .height(12.dp)
                                            .background(AggColor.signal),
                                    )
                                    Spacer(Modifier.width(Dimen.SpaceS))
                                    Text(
                                        group.stationName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        stringResource(R.string.express_station_count, group.items.size),
                                        style = AggType.MonoData,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            items(group.items, key = { "pkg_${it.id}" }) { item ->
                                ExpressItemRow(item)
                            }
                        }
                        item { Spacer(Modifier.height(Dimen.SpaceXL)) }
                    }
                }
            }
        }
    }
}

/** 复制文本到剪贴板并 Toast 提示 */
@Composable
private fun rememberCopy(): (String) -> Unit {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    return { text ->
        val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newPlainText("code", text))
        // 此处是点击回调、已脱离组合作用域，只能用 getString 而非 stringResource
        android.widget.Toast.makeText(ctx, ctx.getString(R.string.msg_copied, text), android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun ExpressItemRow(item: com.aggapp.core.model.ExpressItem) {
    val copy = rememberCopy()
    TicketCard(Modifier.padding(vertical = 4.dp)) {
        Column(Modifier.padding(Dimen.CardPaddingCompact)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    item.trackingNo.orEmpty(),
                    style = AggType.TrackingNo,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clickable { copy(item.trackingNo.orEmpty()) },
                )
                StatusStamp(
                    text = when (item.status) {
                        ExpressStatus.WAIT_PICKUP -> stringResource(R.string.express_status_wait_pickup)
                        ExpressStatus.PICKED -> stringResource(R.string.express_status_picked)
                        ExpressStatus.TIMEOUT -> stringResource(R.string.express_status_timeout)
                        ExpressStatus.UNKNOWN -> stringResource(R.string.express_status_unknown)
                    },
                    tone = when (item.status) {
                        ExpressStatus.WAIT_PICKUP -> StampTone.Signal
                        ExpressStatus.TIMEOUT -> StampTone.Alert
                        else -> StampTone.Neutral
                    },
                )
            }
            Spacer(Modifier.height(Dimen.SpaceS))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // 先取到局部变量再判空：arrivedAt 是跨模块数据类字段，直接判空无法智能转换
                val arrivedAt = item.arrivedAt
                Text(
                    if (arrivedAt != null) {
                        stringResource(R.string.express_arrived_at, fmtTime(arrivedAt))
                    } else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                item.pickupCode?.let { pickupCode ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(R.string.express_code_label),
                            style = AggType.Eyebrow,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            pickupCode,
                            style = AggType.PickupCodeRow,
                            modifier = Modifier.clickable { copy(pickupCode) },
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        // 互补查询命中时标注取件码来源渠道（如「来自淘宝」）
                        val source = item.pickupCodeSource
                        if (source != null) {
                            Text(
                                stringResource(R.string.express_code_source, source.label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val timeFmt = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
private fun fmtTime(ms: Long): String = timeFmt.format(Date(ms))

/** 如意邻里查件详情卡（orderDetailByWayBillNo，2026-09-29 实测）：状态 + 收寄件 + 轨迹时间线 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RyllDetailCard(d: com.aggapp.platform.ryll.RyllOrderDetail) {
    var expanded by remember { mutableStateOf(false) }
    TicketCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(Dimen.CardPaddingCompact)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    d.mailNo,
                    style = AggType.TrackingNo,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    // 部分平台查询成功但不回状态名，用固定文案兜底而非显示空白
                    if (d.statusName.isBlank()) {
                        stringResource(R.string.express_search_ok)
                    } else d.statusName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (d.atStation) AggColor.signal else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Dimen.SpaceXS))
            // 收寄件信息可能整段缺失（部分平台不返回），逐项判空后再拼接，
            // 避免最终拼出「 → 」这种空壳分隔符
            val sender = d.senderName
            val receiver = d.receiverName
            Text(
                listOfNotNull(
                    if (d.fromCity != null || d.toCity != null) {
                        stringResource(R.string.express_route, d.fromCity.orEmpty(), d.toCity.orEmpty())
                    } else null,
                    if (sender != null) stringResource(R.string.express_sender, sender) else null,
                    if (receiver != null) stringResource(R.string.express_receiver, receiver) else null,
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            d.pickupCode?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(Dimen.SpaceXS))
                Text(
                    stringResource(R.string.express_pickup_code, it),
                    style = AggType.PickupCodeDetail,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(Dimen.SpaceS))
            Hairline()
            Spacer(Modifier.height(6.dp))
            val tracks = if (expanded) d.tracks else d.tracks.take(3)
            tracks.forEach { t ->
                Column(Modifier.padding(vertical = 4.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(t.opName.orEmpty(), style = MaterialTheme.typography.labelMedium)
                        Text(
                            t.opTime.orEmpty(),
                            style = AggType.MonoData,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    t.description?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (d.tracks.size > 3) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        if (expanded) stringResource(R.string.express_tracks_collapse)
                        else stringResource(R.string.express_tracks_expand, d.tracks.size),
                    )
                }
            }
        }
    }
}
