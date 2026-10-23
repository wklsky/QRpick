package com.aggapp.app.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aggapp.app.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 拼多多扫码登录弹窗（2026-09-29 去短信登录，扫码不踢 App 下线）：
 * - 展示 AUTH_LOGIN 二维码（VM 3s 轮询，过期自动重刷），「保存到相册」→ 拼多多 App 扫一扫相册识别；
 * - 扫码确认后若触发 54002 二次验证：Text(短信码) 原生输码框直调协议（不展示官方页）；
 * - 其他验证类型由 VM 转 WebView 官方验证页（pddVerifyUrl），本弹窗自动关闭。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PddQrLoginDialog(
    state: com.aggapp.app.ui.MineUiState,
    onVerifySms: (String) -> Unit,
    onAnswerQuiz: (List<String>) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var smsCode by remember { mutableStateOf("") }

    // 二维码内容 → Bitmap（zxing core，与身份码条码渲染同库）
    val qrBitmap: Bitmap? = remember(state.pddQrContent) {
        state.pddQrContent?.let { content ->
            try {
                val size = 560
                val matrix = QRCodeWriter().encode(
                    content, BarcodeFormat.QR_CODE, size, size,
                    mapOf(EncodeHintType.MARGIN to 2),
                )
                Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also { bmp ->
                    for (x in 0 until size) for (y in 0 until size) {
                        bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.dialog_pdd_qr_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
            }
            Text(
                state.pddQrHint ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            // 进入二次验证（短信/答题）后不再显示登录二维码，聚焦验证操作
            if (!state.pddSmsVerifySent && state.pddQuiz == null) {
                qrBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = stringResource(R.string.cd_login_qr),
                        // 二维码底色必须纯白：深色模式下浅底会降低对比度，手机相机容易识别失败
                        modifier = Modifier.size(240.dp)
                            .background(androidx.compose.ui.graphics.Color.White),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val ok = saveQrToGallery(context, bmp)
                                // 协程内已脱离组合作用域，只能走 getString
                                Toast.makeText(
                                    context,
                                    context.getString(
                                        if (ok) R.string.msg_qr_saved else R.string.msg_qr_save_failed
                                    ),
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        },
                    ) { Text(stringResource(R.string.action_save_to_gallery)) }
                } ?: run {
                    Text(
                        stringResource(R.string.dialog_qr_generating),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // 二次验证为短信码：原生输码（协议直调，不展示官方页面）
            if (state.pddSmsVerifySent) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = smsCode,
                    onValueChange = { smsCode = it.take(6) },
                    label = { Text(stringResource(R.string.label_sms_code_full)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onVerifySms(smsCode) },
                    enabled = smsCode.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.action_verify_and_login)) }
            }

            // type=101 知识验证：原生选择题（challenge_captcha/challenge_verify，2026-10-21 实测协议）
            state.pddQuiz?.let { q ->
                val multi = q.correctAnswerCnt > 1
                var single by remember(q.questionId) { mutableStateOf<String?>(null) }
                var multiSet by remember(q.questionId) { mutableStateOf(setOf<String>()) }
                Spacer(Modifier.height(16.dp))
                // 题干偶发为空（服务端只下发选项图），用兜底文案 + 选择模式提示拼装，
                // 否则会渲染出一个空标题让用户不知道要做什么
                val baseTitle = if (q.desc.isBlank()) {
                    stringResource(R.string.dialog_quiz_pick_answer)
                } else {
                    q.desc
                }
                val modeHint = if (multi) {
                    stringResource(R.string.dialog_quiz_multi_hint, q.correctAnswerCnt)
                } else {
                    stringResource(R.string.dialog_quiz_single_hint)
                }
                val quizTitle = baseTitle + modeHint
                Text(quizTitle, style = MaterialTheme.typography.titleMedium)
                if (state.pddQuizMsg != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(state.pddQuizMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                val selected: List<String> = if (multi) multiSet.toList() else listOfNotNull(single)
                val canSubmit = selected.isNotEmpty() && (!multi || selected.size == q.correctAnswerCnt) && !state.busy
                val allImages = q.options.values.all { it.startsWith("http") }
                if (allImages) {
                    // 图片选项：3 列网格排布（9 宫格），点击整格切换选中，右上角打勾角标
                    val cols = 3
                    val rows = q.options.entries.chunked(cols)
                    for (row in rows) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (entry in row) {
                                val key = entry.key
                                val url = entry.value
                                val checked = if (multi) key in multiSet else single == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (checked) 2.dp else 1.dp,
                                            color = if (checked) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(8.dp),
                                        )
                                        .clickable {
                                            if (multi) {
                                                multiSet = if (key in multiSet) multiSet - key
                                                else if (multiSet.size < q.correctAnswerCnt) multiSet + key else multiSet
                                            } else single = key
                                        },
                                ) {
                                    coil.compose.AsyncImage(
                                        model = url,
                                        contentDescription = stringResource(R.string.cd_option_image),
                                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                        contentScale = ContentScale.Crop,
                                    )
                                    if (checked) {
                                        Box(
                                            Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text("✓", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                            // 补齐空位保持网格对齐
                            repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                } else {
                    for ((key, text) in q.options) {
                        val checked = if (multi) key in multiSet else single == key
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (multi) {
                                        multiSet = if (key in multiSet) multiSet - key
                                        else if (multiSet.size < q.correctAnswerCnt) multiSet + key else multiSet
                                    } else single = key
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                        ) {
                            if (multi) {
                                androidx.compose.material3.Checkbox(checked = checked, onCheckedChange = null)
                            } else {
                                RadioButton(selected = checked, onClick = null)
                            }
                            Spacer(Modifier.width(8.dp))
                            if (text.startsWith("http")) {
                                coil.compose.AsyncImage(
                                    model = text,
                                    contentDescription = stringResource(R.string.cd_option_image),
                                    modifier = Modifier.fillMaxWidth().height(96.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            } else {
                                Text(text, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onAnswerQuiz(selected) },
                    enabled = canSubmit,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (state.busy) {
                            stringResource(R.string.action_submitting)
                        } else if (multi) {
                            stringResource(
                                R.string.action_submit_progress,
                                selected.size,
                                q.correctAnswerCnt,
                            )
                        } else {
                            stringResource(R.string.action_submit_answer)
                        },
                    )
                }
            }
        }
    }
}

/** 二维码存入系统相册（MediaStore Pictures），成功返回 true */
private suspend fun saveQrToGallery(context: android.content.Context, bmp: Bitmap): Boolean =
    withContext(Dispatchers.IO) {
        try {
            val name = "pdd_login_qr_${System.currentTimeMillis()}.png"
            MediaStore.Images.Media.insertImage(
                context.contentResolver, bmp, name, "拼多多登录二维码",
            ) != null
        } catch (_: Exception) {
            false
        }
    }
