package com.aggapp.app.ui

import android.widget.Toast
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import com.aggapp.app.R
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aggapp.app.ui.theme.AggColor
import com.aggapp.app.ui.theme.AggType
import com.aggapp.app.ui.theme.Dimen
import com.aggapp.app.ui.theme.ShapeControl
import com.aggapp.core.model.Platform

/**
 * 「我的」页（2026-09-29 布局重构）：
 * - 顶部一行 4 个平台登录态卡片（点击切换下方展开对应渠道的登录表单）；
 * - 登录表单区：手机号+验证码协议登录为主，兜底粘贴入口折叠；
 * - 平台登录态总览卡片（各平台协议直连登录/兜底粘贴）。
 */
/**
 * 五张平台登录态卡片并排展示，圆角必须完全一致否则视觉参差。
 * 提到文件级常量而非写在 Composable 内：后者会在每次重组时重建 RoundedCornerShape。
 */
private val PlatformCardShape = RoundedCornerShape(Dimen.RadiusControl)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(vm: MineViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    var phone by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var jdPtKey by remember { mutableStateOf("") }
    var jdPin by remember { mutableStateOf("") }
    var sfToken by remember { mutableStateOf("") }

    // 当前展开登录表单的平台（默认淘宝）
    var expanded by remember { mutableStateOf(Platform.TAOBAO) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.consumeMessage()
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ===== 简洁头部（surface 底，与身份码页/底部导航同风格）=====
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    Modifier
                        .statusBarsPadding()
                        .padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.CardPaddingCompact),
                ) {
                    Text(
                        stringResource(R.string.mine_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = AggType.ScreenTitle,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.mine_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = AggType.ScreenSubtitle,
                    )
                }
            }
            Column(
                Modifier.padding(horizontal = Dimen.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            // ===== 顶部：5 平台登录态总览（点击展开对应登录表单）=====
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimen.SpaceS)) {
                Platform.entries.forEach { p ->
                    val ls = state.loginStates[p]
                    val selected = expanded == p
                    val accent = com.aggapp.app.ui.theme.Brand.PlatformColor[p]
                        ?: MaterialTheme.colorScheme.primary
                    Card(
                        onClick = { expanded = p },
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (ls?.loggedIn == true) {
                                    // 已登录：绿色描边 + 绿色光晕
                                    Modifier
                                        .shadow(
                                            6.dp, PlatformCardShape, clip = false,
                                            ambientColor = AggColor.loggedIn,
                                            spotColor = AggColor.loggedIn,
                                        )
                                } else Modifier
                            ),
                        shape = PlatformCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                selected -> MaterialTheme.colorScheme.primaryContainer
                                ls?.loggedIn == true -> MaterialTheme.colorScheme.surface
                                ls?.needRelogin == true -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surface
                            },
                        ),
                        border = when {
                            ls?.loggedIn == true ->
                                androidx.compose.foundation.BorderStroke(1.5.dp, AggColor.loggedIn)
                            else -> null
                        },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // 平台品牌色圆点
                            Box(
                                Modifier
                                    .width(8.dp)
                                    .height(8.dp)
                                    .background(accent, androidx.compose.foundation.shape.CircleShape),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(p.label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                when {
                                    ls == null -> stringResource(R.string.mine_login_state_unchecked)
                                    ls.loggedIn -> stringResource(R.string.mine_login_state_logged_in)
                                    ls.needRelogin -> stringResource(R.string.mine_login_state_need_relogin)
                                    else -> stringResource(R.string.mine_login_state_logged_out)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    ls?.loggedIn == true -> MaterialTheme.colorScheme.primary
                                    ls?.needRelogin == true -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = { vm.refreshLoginStates() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.mine_recheck_all)) }

            // ===== 展开的平台登录表单 =====
            when (expanded) {
                Platform.TAOBAO -> {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimen.CardPadding)) {
                            Text(
                                stringResource(R.string.mine_form_taobao_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                stringResource(R.string.mine_form_taobao_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Dimen.SpaceS))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(stringResource(R.string.label_phone)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Spacer(Modifier.height(Dimen.SpaceS))
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimen.SpaceS)) {
                                OutlinedTextField(
                                    value = smsCode,
                                    onValueChange = { smsCode = it },
                                    label = { Text(stringResource(R.string.label_sms_code)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                )
                                Button(
                                    onClick = { vm.sendTaobaoSms(phone) },
                                    enabled = !state.busy,
                                ) { Text(stringResource(R.string.action_send_code)) }
                            }
                            Spacer(Modifier.height(Dimen.SpaceS))
                            Button(
                                onClick = { vm.loginTaobao(smsCode) },
                                enabled = !state.busy && state.smsSent && smsCode.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(stringResource(R.string.action_login)) }
                            LogoutRow(state.loginStates[Platform.TAOBAO]) { vm.logout(Platform.TAOBAO) }
                        }
                    }
                }

                Platform.JD -> {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimen.CardPadding)) {
                            Text(
                                stringResource(R.string.mine_form_jd_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                stringResource(R.string.mine_form_jd_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Dimen.SpaceS))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(stringResource(R.string.label_phone)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Spacer(Modifier.height(Dimen.SpaceS))
                            Button(
                                onClick = { vm.jdPrepareSms(phone) },
                                enabled = !state.busy && phone.length == 11,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(stringResource(R.string.action_send_code)) }
                            if (state.jdSmsSent) {
                                Spacer(Modifier.height(Dimen.SpaceS))
                                OutlinedTextField(
                                    value = smsCode,
                                    onValueChange = { smsCode = it },
                                    label = { Text(stringResource(R.string.label_sms_code_full)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                                Spacer(Modifier.height(Dimen.SpaceS))
                                Button(
                                    onClick = { vm.jdVerifySms(smsCode) },
                                    enabled = !state.busy && smsCode.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(stringResource(R.string.action_login)) }
                            }
                            state.jdCaptchaUrl?.let { url ->
                                // 2026-10-21：本设备 Compose 容器内 WebView 不出帧，改用
                                // 纯 View 原生 Activity 承载滑块，结果经静态回调回传 VM
                                val ctx = androidx.compose.ui.platform.LocalContext.current
                                LaunchedEffect(url) {
                                    CaptchaActivity.onResult = { json ->
                                        if (json == null) vm.jdDismissCaptcha() else vm.jdOnCaptchaResult(json)
                                    }
                                    ctx.startActivity(android.content.Intent(ctx, CaptchaActivity::class.java).putExtra(CaptchaActivity.EXTRA_URL, url))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            var showJdPaste by remember { mutableStateOf(false) }
                            TextButton(onClick = { showJdPaste = !showJdPaste }) {
                                Text(
                                    if (showJdPaste) stringResource(R.string.mine_form_paste_collapse)
                                    else stringResource(R.string.mine_form_jd_paste_toggle),
                                )
                            }
                            if (showJdPaste) {
                                OutlinedTextField(
                                    value = jdPtKey,
                                    onValueChange = { jdPtKey = it },
                                    label = { Text(stringResource(R.string.mine_form_jd_pt_key)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                                Spacer(Modifier.height(Dimen.SpaceS))
                                Row(horizontalArrangement = Arrangement.spacedBy(Dimen.SpaceS)) {
                                    OutlinedTextField(
                                        value = jdPin,
                                        onValueChange = { jdPin = it },
                                        label = { Text(stringResource(R.string.mine_form_jd_pin)) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                    )
                                    Button(
                                        onClick = { vm.loginJd(jdPtKey, jdPin) },
                                        enabled = !state.busy,
                                    ) { Text(stringResource(R.string.action_save)) }
                                }
                            }
                            LogoutRow(state.loginStates[Platform.JD]) { vm.logout(Platform.JD) }
                        }
                    }
                }

                Platform.SF -> {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimen.CardPadding)) {
                            Text(
                                stringResource(R.string.mine_form_sf_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                stringResource(R.string.mine_form_sf_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Dimen.SpaceS))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(stringResource(R.string.label_phone)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Spacer(Modifier.height(Dimen.SpaceS))
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimen.SpaceS)) {
                                OutlinedTextField(
                                    value = smsCode,
                                    onValueChange = { smsCode = it },
                                    label = { Text(stringResource(R.string.label_sms_code)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                )
                                Button(
                                    onClick = { vm.sfStartCaptcha(phone) },
                                    enabled = !state.busy,
                                ) { Text(stringResource(R.string.action_send_code)) }
                            }
                            Spacer(Modifier.height(Dimen.SpaceS))
                            Button(
                                onClick = { vm.loginSfSms(smsCode) },
                                enabled = !state.busy && state.sfSmsSent && smsCode.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(stringResource(R.string.action_login)) }
                            Spacer(Modifier.height(4.dp))
                            var showSfPaste by remember { mutableStateOf(false) }
                            TextButton(onClick = { showSfPaste = !showSfPaste }) {
                                Text(
                                    if (showSfPaste) stringResource(R.string.mine_form_paste_collapse)
                                    else stringResource(R.string.mine_form_sf_paste_toggle),
                                )
                            }
                            if (showSfPaste) {
                                OutlinedTextField(
                                    value = sfToken,
                                    onValueChange = { sfToken = it },
                                    label = { Text(stringResource(R.string.mine_form_sf_token)) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(Dimen.SpaceS))
                                Button(
                                    onClick = { vm.loginSf(sfToken) },
                                    enabled = !state.busy,
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(stringResource(R.string.action_save)) }
                            }
                            LogoutRow(state.loginStates[Platform.SF]) { vm.logout(Platform.SF) }
                        }
                    }
                    // 顺丰极验滑块：改用原生 Activity（Compose 容器内 WebView 不出帧），成功后自动发码
                    val sfCtx = androidx.compose.ui.platform.LocalContext.current
                    LaunchedEffect(state.sfGeetestOpen) {
                        if (state.sfGeetestOpen) {
                            CaptchaActivity.onResult = { json ->
                                if (json == null) vm.sfDismissCaptcha() else vm.sfOnCaptchaResult(json)
                            }
                            sfCtx.startActivity(
                                android.content.Intent(sfCtx, CaptchaActivity::class.java)
                                    .putExtra(CaptchaActivity.EXTRA_URL, "")
                                    .putExtra(CaptchaActivity.EXTRA_ASSET, true),
                            )
                        }
                    }
                }

                Platform.PDD -> {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimen.CardPadding)) {
                            Text(
                                stringResource(R.string.mine_form_pdd_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                stringResource(R.string.mine_form_pdd_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Dimen.SpaceS))
                            Button(
                                onClick = { vm.startPddQrLogin() },
                                enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(stringResource(R.string.mine_form_pdd_action)) }
                            LogoutRow(state.loginStates[Platform.PDD]) { vm.logout(Platform.PDD) }
                        }
                    }
                }

                Platform.RYLL -> {
                    var ryllCaptcha by remember { mutableStateOf("") }
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimen.CardPadding)) {
                            Text(
                                stringResource(R.string.mine_form_ryll_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                stringResource(R.string.mine_form_ryll_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Dimen.SpaceS))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(stringResource(R.string.label_phone)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Spacer(Modifier.height(Dimen.SpaceS))
                            // 图形验证码：点图换一张（发短信前置，README §3.2）
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                state.ryllVcodeBase64?.let { dataUrl ->
                                    val bmp = remember(dataUrl) { decodeDataUrlBitmap(dataUrl) }
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = stringResource(R.string.cd_ryll_captcha),
                                            modifier = Modifier
                                                .width(132.dp)
                                                .height(44.dp)
                                                .androidClickable { vm.ryllFetchVcode() },
                                        )
                                    } else {
                                        Text(
                                            stringResource(R.string.mine_form_ryll_captcha_failed),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                } ?: run {
                                    OutlinedButton(
                                        onClick = { vm.ryllFetchVcode() },
                                        enabled = !state.busy,
                                    ) { Text(stringResource(R.string.mine_form_ryll_fetch_captcha)) }
                                }
                                Spacer(Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = ryllCaptcha,
                                    onValueChange = { ryllCaptcha = it },
                                    label = { Text(stringResource(R.string.mine_form_ryll_captcha_label)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                )
                            }
                            Spacer(Modifier.height(Dimen.SpaceS))
                            Button(
                                onClick = { vm.ryllSendSms(phone, ryllCaptcha) },
                                enabled = !state.busy && phone.length == 11 && ryllCaptcha.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(stringResource(R.string.mine_form_ryll_send_sms)) }
                            if (state.ryllSmsSent) {
                                Spacer(Modifier.height(Dimen.SpaceS))
                                OutlinedTextField(
                                    value = smsCode,
                                    onValueChange = { smsCode = it },
                                    label = { Text(stringResource(R.string.label_sms_code_full)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                                Spacer(Modifier.height(Dimen.SpaceS))
                                Button(
                                    onClick = { vm.ryllLogin(smsCode) },
                                    enabled = !state.busy && smsCode.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(stringResource(R.string.action_login)) }
                            }
                            LogoutRow(state.loginStates[Platform.RYLL]) { vm.logout(Platform.RYLL) }
                        }
                    }
                }
            }

            // ===== 联系/赞助作者 =====
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                var showWx by remember { mutableStateOf(false) }
                var showSkm by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { showWx = true },
                    modifier = Modifier.weight(1f),
                    shape = ShapeControl,
                ) { Text(stringResource(R.string.mine_contact_author)) }
                Button(
                    onClick = { showSkm = true },
                    modifier = Modifier.weight(1f),
                    shape = ShapeControl,
                    colors = ButtonDefaults.buttonColors(containerColor = com.aggapp.app.ui.theme.Brand.Amber),
                ) { Text(stringResource(R.string.mine_sponsor)) }

                // 居中弹窗：微信二维码
                if (showWx) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showWx = false },
                        confirmButton = {
                            TextButton(onClick = { showWx = false }) { Text(stringResource(R.string.action_close)) }
                        },
                        title = {
                            Text(
                                stringResource(R.string.mine_contact_author),
                                Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painterResource(R.drawable.wx_qr),
                                    // 原描述写的是「QQ群二维码」，与弹窗标题「联系作者」不符，
                                    // 这里按弹窗语义统一，避免 TalkBack 播报与界面文案矛盾
                                    contentDescription = stringResource(R.string.cd_contact_qr),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(Dimen.SpaceS))
                                Text(
                                    stringResource(R.string.mine_contact_qr_tip),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                }
                // 居中弹窗：赞助码
                if (showSkm) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showSkm = false },
                        confirmButton = {
                            TextButton(onClick = { showSkm = false }) { Text(stringResource(R.string.action_close)) }
                        },
                        title = {
                            Text(
                                stringResource(R.string.mine_sponsor),
                                Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painterResource(R.drawable.skm_qr),
                                    contentDescription = stringResource(R.string.cd_sponsor_qr),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(Dimen.SpaceS))
                                Text(
                                    stringResource(R.string.mine_sponsor_tip),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                }
            }

            // ===== 调试日志导出（debug 包：操作日志 + 请求日志 + 登录态） =====
            OutlinedButton(
                onClick = { vm.exportDebugLog() },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeControl,
            ) { Text(stringResource(R.string.mine_export_log)) }

            // 淘宝多账号选择弹窗（手机号绑定多个淘宝账号，登录前需选择）
            if (state.tbAccounts.isNotEmpty()) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { vm.tbDismissAccountSelect() },
                    confirmButton = {
                        TextButton(onClick = { vm.tbDismissAccountSelect() }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    },
                    title = {
                        Text(
                            stringResource(R.string.mine_tb_account_select_title),
                            Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                stringResource(R.string.mine_tb_account_select_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            state.tbAccounts.forEach { acc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { vm.tbSelectAccount(acc.uniqId) }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    // 服务端偶尔返回空昵称，用占位文案兜底，避免列表出现空白行
                                    Text(
                                        if (acc.nick.isEmpty()) {
                                            stringResource(R.string.mine_tb_account_fallback)
                                        } else acc.nick,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (acc.mostFrequent) {
                                        Text(
                                            stringResource(R.string.mine_tb_account_most_frequent),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    } else if (acc.lastActiveTime.isNotEmpty()) {
                                        Text(
                                            stringResource(
                                                R.string.mine_tb_account_last_active,
                                                acc.lastActiveTime,
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    },
                )
            }

            // 淘宝 iv 风控 WebView 弹窗（多账号收尾命中风控：用户手动过风控，
            // 检测到登录态 Cookie（cookie2/unb）出现后自动重试协议收尾并关闭）
            if (state.tbRiskUrl != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { vm.tbDismissRiskCheck() },
                    confirmButton = {
                        TextButton(onClick = { vm.tbDismissRiskCheck() }) { Text(stringResource(R.string.action_close)) }
                    },
                    title = {
                        Text(
                            stringResource(R.string.mine_risk_verify_title),
                            Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    },
                    text = {
                        androidx.compose.ui.viewinterop.AndroidView(
                            modifier = Modifier.fillMaxWidth().height(420.dp),
                            factory = { ctx ->
                                android.webkit.WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                    val cm = android.webkit.CookieManager.getInstance()
                                    cm.setAcceptCookie(true)
                                    // 预置会话 Cookie（保持服务端上下文）
                                    state.tbWebviewCookies.forEach { (u, c) -> cm.setCookie(u, c) }
                                    cm.flush()
                                    val handler = android.os.Handler(android.os.Looper.getMainLooper())
                                    val check = object : Runnable {
                                        override fun run() {
                                            val ck = cm.getCookie("https://www.taobao.com/") ?: ""
                                            if (ck.contains("cookie2=") && ck.contains("unb=")) {
                                                vm.tbRiskCheckPassed(ck)
                                                return
                                            }
                                            handler.postDelayed(this, 1500)
                                        }
                                    }
                                    webViewClient = object : android.webkit.WebViewClient() {
                                        override fun onPageFinished(view: android.webkit.WebView, url: String) {
                                            handler.postDelayed(check, 1500)
                                        }
                                    }
                                    loadUrl(state.tbRiskUrl!!)
                                    handler.postDelayed(check, 3000)
                                }
                            },
                        )
                    },
                )
            }

            // 拼多多扫码登录弹窗（二维码展示 + 保存相册 + Text 二次验证原生输码）
            if (state.pddQrOpen) {
                PddQrLoginDialog(
                    state = state,
                    onVerifySms = vm::pddVerifySms,
                    onAnswerQuiz = vm::pddAnswerQuiz,
                    onDismiss = vm::dismissPddQr,
                )
            }

            // 拼多多二次验证：回到 App 内 WebView，但用 PC 桌面 Chrome UA（desktopUa）——
            // 老版本扫码登录页即此配置且正常渲染；移动 UA 下 PDD 风控做渲染层隐藏（白屏）。
            // Cookie 出现 PDDAccessToken 即自动落库；「收尾登录」按钮兜底。
            state.pddVerifyUrl?.let { url ->
                WebViewLoginDialog(
                    title = stringResource(R.string.mine_pdd_verify_title),
                    loginUrl = url,
                    checkSuccess = vm::pddDetectLogin,
                    onCredential = vm::onPddWebViewCredential,
                    onDismiss = vm::pddDismissVerifyUrl,
                    tip = null, // 动态显示页面提取的题目文字
                    desktopUa = true,
                    clearCookies = true,
                    actionLabel = stringResource(R.string.mine_pdd_verify_action),
                    onAction = vm::pddManualFinishVerify,
                )
            }

            }
        }
    }
}

/** 已登录平台在表单底部展示「登出该账号」 */
@Composable
private fun LogoutRow(ls: com.aggapp.core.model.PlatformLoginState?, onLogout: () -> Unit) {
    if (ls != null && (ls.loggedIn || ls.needRelogin)) {
        TextButton(onClick = onLogout) { Text(stringResource(R.string.action_logout)) }
    }
}

/** data:image/png;base64 → Bitmap（如意邻里图形验证码显示用） */
private fun decodeDataUrlBitmap(dataUrl: String): android.graphics.Bitmap? = try {
    val bytes = Base64.decode(dataUrl.substringAfter("base64,", ""), Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} catch (_: Exception) {
    null
}

/** clickable 的简易别名（避免与 material3 语义冲突的私有扩展） */
private fun Modifier.androidClickable(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
