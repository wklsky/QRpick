package com.aggapp.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import com.aggapp.app.R
import com.aggapp.app.ui.components.BARCODE_FULLSCREEN_HEIGHT
import com.aggapp.app.ui.components.BARCODE_FULLSCREEN_WIDTH
import com.aggapp.app.ui.components.BARCODE_HEIGHT
import com.aggapp.app.ui.components.BARCODE_WIDTH
import com.aggapp.app.ui.components.BarcodeState
import com.aggapp.app.ui.components.rememberBarcode
import com.aggapp.app.ui.theme.AggColor
import com.aggapp.app.ui.theme.AggType
import com.aggapp.app.ui.theme.Brand
import com.aggapp.app.ui.theme.Dimen
import com.aggapp.app.ui.theme.ShapeControl
import com.aggapp.app.ui.theme.ShapePill
import com.aggapp.core.model.Platform

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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
                // ===== 简洁头部（surface 底 + 主色点缀，与底部导航栏同风格）=====
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                        .padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SpaceM),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.idcode_title),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = AggType.ScreenTitle,
                        )
                        IconButton(onClick = { vm.refreshAll(force = true) }) {
                            if (state.loading) {
                                CircularProgressIndicator(
                                    Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.5.dp,
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Refresh,
                                    stringResource(R.string.action_refresh),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ===== 平台切换：浅灰轨道分段控件（选中白底浮起）=====
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(ShapeControl)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(Dimen.SpaceXS),
                        horizontalArrangement = Arrangement.spacedBy(Dimen.SpaceXS),
                    ) {
                        for (p in platforms) {
                            val selected = p == state.selected
                            Row(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Dimen.RadiusSmall))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
                                    )
                                    .clickable { vm.select(p) }
                                    .padding(vertical = Dimen.SpaceS),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    p.label,
                                    style = AggType.SegmentedLabel,
                                    maxLines = 1,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                }

                val code = state.codes[state.selected]
                if (code == null) {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(Dimen.ScreenPadding),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Box(
                            Modifier.fillMaxWidth().height(220.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(R.string.idcode_empty),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    Column(Modifier.padding(Dimen.ScreenPadding)) {
                        // ===== 身份码大卡 =====
                        // 整卡及卡内全部配色锁定为浅色（AggColor.Barcode*），不参与深色反转：
                        // 驿站扫码枪依赖白底黑条的最大对比度，深底浅条在部分老设备上直接读不出码。
                        // 因此卡内的平台名、码号、徽章一律不能用 colorScheme，否则深色下会反向失真。
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .pointerInput(code.content) {
                                    detectTapGestures(
                                        onTap = { vm.refreshAll(force = true) },
                                        onDoubleTap = { fullscreen = true },
                                    )
                                },
                            shape = MaterialTheme.shapes.large,
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = AggColor.BarcodeCanvas),
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimen.SpaceXL, horizontal = Dimen.CardPadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // 平台徽标行
                                val accent = Brand.PlatformColor[state.selected] ?: Brand.Indigo
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(10.dp)
                                            .background(accent, CircleShape),
                                    )
                                    Spacer(Modifier.width(Dimen.SpaceXS + 2.dp))
                                    Text(
                                        state.selected.label,
                                        style = AggType.PlatformLabel,
                                        color = AggColor.BarcodeInk,
                                    )
                                }
                                Spacer(Modifier.height(Dimen.SpaceL))

                                when (val barcode = rememberBarcode(code.content).value) {
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

                                    // 异步渲染未完成时按出图比例占位，
                                    // 保证码图出现时卡片高度不跳变
                                    BarcodeState.Loading -> Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(BARCODE_WIDTH.toFloat() / BARCODE_HEIGHT),
                                    )
                                }
                                Spacer(Modifier.height(Dimen.SpaceL))
                                // 码号文字单独显示（对齐 H5 原版 displayValue:false）
                                Text(
                                    code.content,
                                    style = AggType.CodeNumber,
                                    color = AggColor.BarcodeInk,
                                )
                                Spacer(Modifier.height(Dimen.SpaceM))
                                Row(horizontalArrangement = Arrangement.spacedBy(Dimen.SpaceS)) {
                                    // 倒计时徽章
                                    state.countdownSec?.let { sec ->
                                        Box(
                                            Modifier
                                                .background(AggColor.BarcodeBadgeBg, ShapePill)
                                                .padding(horizontal = Dimen.SpaceM, vertical = 5.dp),
                                        ) {
                                            Text(
                                                stringResource(R.string.idcode_countdown, sec / 60, sec % 60),
                                                style = AggType.Caption,
                                                color = AggColor.BarcodeBadgeFg,
                                            )
                                        }
                                    }
                                    Box(
                                        Modifier
                                            .background(AggColor.BarcodeBadgeBg, ShapePill)
                                            .padding(horizontal = Dimen.SpaceM, vertical = 5.dp),
                                    ) {
                                        Text(
                                            stringResource(R.string.idcode_tap_hint),
                                            style = AggType.Caption,
                                            color = AggColor.BarcodeBadgeFg,
                                        )
                                    }
                                }
                            }
                        }

                        // 未登录提示
                        if (state.isReal[state.selected] == false) {
                            Spacer(Modifier.height(Dimen.SpaceM))
                            Text(
                                stringResource(R.string.idcode_not_logged_in),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        Spacer(Modifier.height(Dimen.SpaceL))

                        // ===== 快捷操作行 =====
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimen.SpaceM)) {
                            Button(
                                onClick = { fullscreen = true },
                                modifier = Modifier.weight(1f),
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Brand.Indigo,
                                ),
                            ) {
                                Icon(
                                    Icons.Outlined.Fullscreen,
                                    stringResource(R.string.cd_fullscreen),
                                    Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(Dimen.SpaceXS + 2.dp))
                                Text(stringResource(R.string.idcode_action_fullscreen))
                            }
                            Button(
                                onClick = { showAllCodes = true },
                                modifier = Modifier.weight(1f),
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Brand.Violet,
                                ),
                            ) {
                                Icon(
                                    Icons.Filled.GridView,
                                    stringResource(R.string.cd_all_platforms),
                                    Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(Dimen.SpaceXS + 2.dp))
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
                        color = AggColor.OnBrightCode,
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
                            style = AggType.PlatformLabel,
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
                            color = AggColor.OnBrightCode,
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
