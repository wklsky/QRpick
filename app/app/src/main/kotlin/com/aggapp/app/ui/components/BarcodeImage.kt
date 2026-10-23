package com.aggapp.app.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @Author: wj
 * @FilePath: app/app/src/main/kotlin/com/aggapp/app/ui/components/BarcodeImage.kt
 * @Description: CODE128 条码的异步渲染与缓存，替代原先在主线程逐像素绘制 Bitmap 的实现
 */

/** 身份码卡片的标准出图尺寸：够大以便扫码枪识别，又不至于占用过多内存 */
const val BARCODE_WIDTH = 1000
const val BARCODE_HEIGHT = 300

/** 全屏亮码需更高分辨率，否则拉伸铺满后边缘会糊 */
const val BARCODE_FULLSCREEN_WIDTH = 1400
const val BARCODE_FULLSCREEN_HEIGHT = 420

/**
 * 条码渲染结果。
 *
 * 必须区分 Loading 与 Failed：原实现把"还没画出来"和"画失败了"都当成 null，
 * 异步化后若不区分，界面会在加载的一瞬间闪一下"条码渲染失败"，是明显的体验缺陷。
 */
sealed interface BarcodeState {
    data object Loading : BarcodeState
    data class Ready(val bitmap: ImageBitmap) : BarcodeState
    data object Failed : BarcodeState
}

/**
 * 异步渲染并缓存条码。
 *
 * 性能背景：zxing 生成 1000x300 位图后若逐像素 setPixel，需要 30 万次跨 JNI 调用，
 * 且原实现在组合阶段同步执行——"全部平台码"弹窗一次会触发 5 次，直接造成掉帧。
 * 这里做两件事：切到 Default 调度器执行，并以 content + 尺寸为 key 复用结果。
 */
@Composable
fun rememberBarcode(
    content: String,
    width: Int = BARCODE_WIDTH,
    height: Int = BARCODE_HEIGHT,
): State<BarcodeState> {
    return produceState<BarcodeState>(BarcodeState.Loading, content, width, height) {
        value = withContext(Dispatchers.Default) {
            renderCode128(content, width, height)
                ?.let { BarcodeState.Ready(it.asImageBitmap()) }
                ?: BarcodeState.Failed
        }
    }
}

/**
 * 生成 CODE128 位图。
 *
 * 实现要点：先整块填充 IntArray 再单次 setPixels，把 30 万次 JNI 调用压成 1 次。
 * 仍保留逐点判断的黑白取值逻辑，因为 zxing 的 BitMatrix 本身就是按位存储，
 * 没有更省事的批量转换接口。
 */
fun renderCode128(content: String, width: Int, height: Int): Bitmap? {
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 10)
        val matrix: BitMatrix =
            Code128Writer().encode(content, BarcodeFormat.CODE_128, width, height, hints)
        val black = android.graphics.Color.BLACK
        val white = android.graphics.Color.WHITE
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                pixels[rowOffset + x] = if (matrix[x, y]) black else white
            }
        }
        Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).also {
            it.setPixels(pixels, 0, width, 0, 0, width, height)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
