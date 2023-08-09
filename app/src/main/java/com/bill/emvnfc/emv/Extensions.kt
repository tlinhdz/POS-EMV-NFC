package com.bill.emvnfc.emv

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.math.sqrt

fun String.formatDateTime() =
    "${this.substring(4, 6)}/${this.substring(2, 4)}/${this.substring(0, 2)}"

val String.isRspCodeSuccess
    get() = this == "9000"

fun String.formatExpireDate(): String {
    return this.substring(2, 4) + "/" + this.substring(0, 2)
}

fun String.formatCardNumber(): String {
    return this.chunked(4).joinToString(" ")
}
@OptIn(ExperimentalTextApi::class)
fun Modifier.drawDiagonalLabel(
    text: String, color: Color, style: TextStyle = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White
    ), labelTextRatio: Float = 7f
) = composed(factory = {

    val textMeasurer = rememberTextMeasurer()
    val textLayoutResult: TextLayoutResult = remember {
        textMeasurer.measure(text = AnnotatedString(text), style = style)
    }

    Modifier
        .clipToBounds()
        .drawWithContent {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val textSize = textLayoutResult.size
            val textWidth = textSize.width
            val textHeight = textSize.height

            val rectWidth = textWidth * labelTextRatio
            val rectHeight = textHeight * 1.1f

            val rect = Rect(
                offset = Offset(canvasWidth - rectWidth, 0f), size = Size(rectWidth, rectHeight)
            )

            val sqrt = sqrt(rectWidth / 2f)
            val translatePos = sqrt * sqrt

            drawContent()
            withTransform({
                rotate(
                    degrees = 45f, pivot = Offset(
                        canvasWidth - rectWidth / 2, translatePos
                    )
                )
            }) {
                drawRect(
                    color = color, topLeft = rect.topLeft, size = rect.size
                )
                drawText(
                    textMeasurer = textMeasurer, text = text, style = style, topLeft = Offset(
                        rect.left + (rectWidth - textWidth) / 2f,
                        rect.top + (rect.bottom - textHeight) / 2f
                    )
                )
            }
        }
})

@OptIn(ExperimentalTextApi::class)
fun Modifier.drawDiagonalShimmerLabel(
    text: String,
    color: Color,
    style: TextStyle = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White
    ),
    labelTextRatio: Float = 7f,
) = composed(factory = {
    val textMeasurer = rememberTextMeasurer()
    val textLayoutResult: TextLayoutResult = remember {
        textMeasurer.measure(text = AnnotatedString(text), style = style)
    }

    val transition = rememberInfiniteTransition(label = "")

    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Modifier
        .clipToBounds()
        .drawWithContent {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val textSize = textLayoutResult.size
            val textWidth = textSize.width
            val textHeight = textSize.height

            val rectWidth = textWidth * labelTextRatio
            val rectHeight = textHeight * 1.1f

            val rect = Rect(
                offset = Offset(canvasWidth - rectWidth, 0f), size = Size(rectWidth, rectHeight)
            )

            val sqrt = sqrt(rectWidth / 2f)
            val translatePos = sqrt * sqrt

            val brush = Brush.linearGradient(
                colors = listOf(
                    color,
                    style.color,
                    color,
                ),
                start = Offset(progress * canvasWidth, progress * canvasHeight),
                end = Offset(
                    x = progress * canvasWidth + rectHeight,
                    y = progress * canvasHeight + rectHeight
                ),
            )

            drawContent()
            withTransform({
                rotate(
                    degrees = 45f, pivot = Offset(
                        canvasWidth - rectWidth / 2, translatePos
                    )
                )
            }) {
                drawRect(
                    brush = brush, topLeft = rect.topLeft, size = rect.size
                )
                drawText(
                    textMeasurer = textMeasurer, text = text, style = style, topLeft = Offset(
                        rect.left + (rectWidth - textWidth) / 2f,
                        rect.top + (rect.bottom - textHeight) / 2f
                    )
                )
            }
        }
})


fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun ByteArray.toHexString(): String {
    val result = StringBuilder(size * 2)
    forEach { byte ->
        result.append(String.format("%02x", byte))
    }
    return result.toString().uppercase()
}
