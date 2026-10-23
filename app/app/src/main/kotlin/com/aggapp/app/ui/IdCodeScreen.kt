package com.aggapp.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aggapp.app.R
import com.aggapp.app.ui.components.BARCODE_FULLSCREEN_HEIGHT
import com.aggapp.app.ui.components.BARCODE_FULLSCREEN_WIDTH
import com.aggapp.app.ui.components.BARCODE_HEIGHT
import com.aggapp.app.ui.components.BARCODE_WIDTH
import com.aggapp.app.ui.components.BarcodeState
import com.aggapp.app.ui.components.EmptyState
import com.aggapp.app.ui.components.LoadingState
import com.aggapp.app.ui.components.PerforatedDivider
import com.aggapp.app.ui.components.ScreenHeader
import com.aggapp.app.ui.components.SignalButton
import com.aggapp.app.ui.components.StatusStamp
import com.aggapp.app.ui.components.StampTone
import com.aggapp.app.ui.components.TicketCard
import com.aggapp.app.ui.components.rememberBarcode
import com.aggapp.app.ui.theme.AggColor
import com.aggapp.app.ui.theme.AggType
import com.aggapp.app.ui.theme.Brand
import com.aggapp.app.ui.theme.Dimen
import com.aggapp.app.ui.theme.ShapeControl
import com.aggapp.core.model.Platform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdCodeScreen(vm: MainViewModel) {
    val state by vm.state.collectAsState()
    val platforms = Platform.entries.toList()

    // 双击全屏显示当前平台身份码（单击全屏任意处退出）
    var fullscreen by remember { mutableStateOf(false) }
    // 全屏显示全部平台身份码（「全部平台码」按钮进入）
    var showAllCodes by remember { mutableStateOf(false) }

    // 每次打开页面刷新（如意邻里身份码非纯静态，回到本页时重新拉取）
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refreshAll()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = { vm.refreshAll(force = true) },
            modifier = Modifier.padding(pad).fillMaxSize(),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                ScreenHeader(
                    eyebrow = stringResource(R.string.eyebrow_idcode),
                    title = stringResource(R.string.idcode_title),
                    actions = {
                        IconButton(onClick = { vm.refreshAll(force = true) }) {
                            if (state.loading) {
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
                    headerBottom = {
                        PlatformTabs(
                            platforms = platforms,
                            selected = state.selected,
                            onSelect = vm::select,
                            modifier = Modifier.padding(top = Dimen.SpaceM),
                        )
                    },
                )

                val code = state.codes[state.selected]
                if (code == null) {
                    TicketCard(
                        Modifier
                            .padding(horizontal = Dimen.ScreenPadding)
                            .padding(top = Dimen.SpaceL),
                    ) {
                        Box(
                            Modifier.fillMaxWidth().heightIn(min = 240.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            // 首屏拉取未完成时先给加载态，否则会先闪一下「暂无身份码」再出现码图
                            if (state.loading) {
                                LoadingState()
                            } else {
                                EmptyState(
                                    text = stringResource(R.string.idcode_empty),
                                    actionText = stringResource(R.string.action_refresh),
                                    onAction = { vm.refreshAll(force = true) },
                                )
                            }
                        }
                    }
                } else {
                    Column(Modifier.padding(Dimen.ScreenPadding)) {
                        PickupPassCard(
                            platform = state.selected,
                            content = code.content,
                            isReal = state.isReal[state.selected] != false,
                            countdownSec = state.countdownSec,
                            onTap = { vm.refreshAll(force = true) },
                            onDoubleTap = { fullscreen = true },
                        )

                        // 未登录提示：单行警示条，而不是夹在卡片下方的一行小字
                        if (state.isReal[state.selected] == false) {
                            Spacer(Modifier.height(Dimen.SpaceM))
                            NoticeStrip(stringResource(R.string.idcode_not_logged_in))
                        }

                        Spacer(Modifier.height(Dimen.SpaceL))

                        Row(horizontalArrangement = Arrangement.spacedBy(Dimen.SpaceM)) {
                            SignalButton(
                                onClick = { fullscreen = true },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    Icons.Outlined.Fullscreen,
                                    stringResource(R.string.cd_fullscreen),
                                    Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(Dimen.SpaceS))
                                Text(stringResource(R.string.idcode_action_fullscreen))
                            }
                            OutlinedButton(
                                onClick = { showAllCodes = true },
                                modifier = Modifier.weight(1f),
                                shape = ShapeControl,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            ) {
                                Icon(
                                    Icons.Filled.GridView,
                                    stringResource(R.string.cd_all_platforms),
                                    Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(Dimen.SpaceS))
                                Text(stringResource(R.string.idcode_action_all))
                            }
                        }
                    }
                }
            }
        }
    }

    // ===== 全屏全部平台码（按钮进入；单击任意处退出；五码均分屏高，免滚动全部可见）=====
    if (showAllCodes) {
        Dialog(
            onDismissRequest = { showAllCodes = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(AggColor.BrightCodeBackdrop)
                    .pointerInput(Unit) { detectTapGestures(onTap = { showAllCodes = false }) }
                    .padding(vertical = Dimen.SpaceXL),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        Modifier.padding(Dimen.SpaceM),
                        color = AggColor.signal,
                    )
                }
                // 每个平台均分剩余高度（weight），条码 ContentScale.Fit 自适应，
                // 免 verticalScroll——任何屏幕都无需上下滑动即可看到全部五码
                platforms.forEach { p ->
                    val code = state.codes[p]
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            p.label,
                            style = AggType.FullscreenPlatformLabel,
                            color = AggColor.OnBrightCode,
                        )
                        if (code != null) {
                            val barcode = rememberBarcode(code.content).value
                            if (barcode is BarcodeState.Ready) {
                                Image(
                                    bitmap = barcode.bitmap,
                                    contentDescription = stringResource(R.string.cd_barcode_of, p.label),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(horizontal = Dimen.SpaceXL),
                                )
                            } else {
                                // 占住 weight 分配的剩余高度，避免五个平台码图陆续加载时整体重排
                                Spacer(Modifier.weight(1f))
                            }
                            Text(
                                code.content,
                                style = AggType.CodeNumberCompact,
                                color = AggColor.OnBrightCode,
                                maxLines = 1,
                            )
                        } else {
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    stringResource(R.string.idcode_all_empty),
                                    style = AggType.Caption,
                                    color = AggColor.OnBrightCodeMuted,
                                )
                            }
                        }
                    }
                }
                Text(
                    stringResource(R.string.idcode_exit_all),
                    style = AggType.Caption,
                    color = AggColor.OnBrightCodeMuted,
                )
            }
        }
    }

    // ===== 全屏身份码（双击进入；单击任意处退出）=====
    if (fullscreen) {
        val code = state.codes[state.selected]
        Dialog(
            onDismissRequest = { fullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(AggColor.BrightCodeBackdrop)
                    .pointerInput(Unit) { detectTapGestures(onTap = { fullscreen = false }) },
                contentAlignment = Alignment.Center,
            ) {
                if (code != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.selected.label,
                            style = AggType.FullscreenPlatformLabel,
                            color = AggColor.signal,
                        )
                        Spacer(Modifier.height(Dimen.SpaceL))
                        val barcode = rememberBarcode(
                            code.content,
                            BARCODE_FULLSCREEN_WIDTH,
                            BARCODE_FULLSCREEN_HEIGHT,
                        ).value
                        if (barcode is BarcodeState.Ready) {
                            Image(
                                bitmap = barcode.bitmap,
                                contentDescription = stringResource(R.string.cd_barcode_fullscreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimen.SpaceXL)
                                    .aspectRatio(10f / 3f),
                                contentScale = ContentScale.FillWidth,
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            code.content,
                            style = AggType.CodeNumberFullscreen,
                            color = AggColor.OnBrightCode,
                        )
                        Spacer(Modifier.height(Dimen.SpaceM))
                        Text(
                            stringResource(R.string.idcode_exit_fullscreen),
                            style = AggType.Caption,
                            color = AggColor.OnBrightCodeMuted,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 平台切换：单据分栏风格的下划线标签，替代旧版圆角分段胶囊。
 *
 * 五个中文平台名等分屏宽，选中态只靠「墨色加粗 + 信号色下划线」，
 * 不用整块色底——避免与身份码卡片的视觉重量打架。
 */
@Composable
private fun PlatformTabs(
    platforms: List<Platform>,
    selected: Platform,
    onSelect: (Platform) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth()) {
        platforms.forEach { p ->
            val isSelected = p == selected
            Column(
                Modifier
                    .weight(1f)
                    .clip(ShapeControl)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onSelect(p) },
                    )
                    .padding(vertical = Dimen.SpaceS),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    p.label,
                    style = AggType.SegmentedLabel,
                    maxLines = 1,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(Modifier.height(Dimen.SpaceXS + 2.dp))
                Box(
                    Modifier
                        .width(18.dp)
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) AggColor.signal else Color.Transparent),
                )
            }
        }
    }
}

/**
 * 取件凭证卡：整张卡锁定浅色（白纸黑条），不参与深色反转。
 *
 * 结构自上而下：平台行 → 条码 → 码号 → 撕裂线 → 票根（提示 + 自动刷新倒计时）。
 * 撕裂线两端用页面底色打出半圆缺口，是整套界面唯一的"形状记忆点"。
 */
@Composable
private fun PickupPassCard(
    platform: Platform,
    content: String,
    isReal: Boolean,
    countdownSec: Long?,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit,
) {
    val accent = Brand.PlatformColor[platform] ?: Color(0xFF4F5BD5)
    TicketCard(
        modifier = Modifier.pointerInput(content) {
            detectTapGestures(onTap = { onTap() }, onDoubleTap = { onDoubleTap() })
        },
        containerColor = AggColor.BarcodeCanvas,
        borderColor = Color(0xFFE2E1E8),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.CardPadding)
                .padding(top = Dimen.CardPadding, bottom = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(accent, CircleShape),
                )
                Spacer(Modifier.width(Dimen.SpaceS))
                Text(
                    platform.label,
                    style = AggType.PlatformLabel,
                    color = AggColor.BarcodeInk,
                )
                Spacer(Modifier.weight(1f))
                if (isReal) {
                    StatusStamp(
                        text = stringResource(R.string.idcode_status_ready),
                        tone = StampTone.Signal,
                    )
                }
            }

            Spacer(Modifier.height(Dimen.SpaceM))

            when (val barcode = rememberBarcode(content).value) {
                is BarcodeState.Ready -> Image(
                    bitmap = barcode.bitmap,
                    contentDescription = stringResource(R.string.cd_barcode),
                    modifier = Modifier.fillMaxWidth(),
                )

                BarcodeState.Failed -> Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(AggColor.BarcodePlaceholder, ShapeControl),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.idcode_render_failed),
                        color = AggColor.BarcodeBadgeFg,
                    )
                }

                // 异步渲染未完成时按出图比例占位，保证码图出现时卡片高度不跳变
                BarcodeState.Loading -> Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(BARCODE_WIDTH.toFloat() / BARCODE_HEIGHT),
                )
            }

            Spacer(Modifier.height(Dimen.SpaceS))
            // 码号文字单独显示（对齐 H5 原版 displayValue:false）
            Text(
                content,
                style = AggType.CodeNumber,
                color = AggColor.BarcodeInk,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.idcode_tap_hint),
                style = AggType.Caption,
                color = AggColor.BarcodeBadgeFg,
            )
            Spacer(Modifier.height(Dimen.SpaceS))
        }

        PerforatedDivider(
            notchColor = MaterialTheme.colorScheme.background,
            lineColor = Color(0xFFD9D8DF),
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.CardPadding, vertical = Dimen.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.idcode_stub_title),
                    style = AggType.Eyebrow,
                    color = AggColor.BarcodeBadgeFg,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    stringResource(R.string.idcode_stub_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = AggColor.BarcodeInk,
                )
            }
            if (countdownSec != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(
                            R.string.idcode_countdown,
                            countdownSec / 60,
                            countdownSec % 60,
                        ),
                        style = AggType.MonoData,
                        color = AggColor.BarcodeInk,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.idcode_stub_refresh),
                        style = AggType.Caption,
                        color = AggColor.BarcodeBadgeFg,
                    )
                }
            }
        }
    }
}

/** 警示条：左侧信号色竖条 + 说明文字，用于「未登录」等需要用户注意的单行信息 */
@Composable
private fun NoticeStrip(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(ShapeControl)
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f))
            .padding(horizontal = Dimen.SpaceM, vertical = Dimen.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            Modifier
                .width(3.dp)
                .height(14.dp)
                .background(MaterialTheme.colorScheme.error),
        )
        Spacer(Modifier.width(Dimen.SpaceS))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
