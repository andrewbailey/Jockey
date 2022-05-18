package dev.andrewbailey.music.util

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.MultiParagraph
import androidx.compose.ui.text.TextLayoutInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun rememberLetterPainter(
    text: String,
    textSize: TextUnit
): LetterPainter {
    val isLight = MaterialTheme.colors.isLight
    val fontLoader = LocalFontFamilyResolver.current
    return remember(text, isLight, textSize, fontLoader) {
        LetterPainter(text, isLight, textSize, fontLoader)
    }
}

class LetterPainter(
    private val text: String,
    private val isLight: Boolean,
    textSize: TextUnit,
    private val fontResolver: FontFamily.Resolver
) : Painter() {

    private val foregroundColor: Color = with(Random(seed = text.uppercase().hashCode() * 2)) {
        Color.hsl(
            hue = nextDouble(0.0, 360.0).toFloat(),
            saturation = if (isLight) { 0.9f } else { 0.7f },
            lightness = if (isLight) { 0.4f } else { 0.6f }
        )
    }

    private val backgroundColor: Color = foregroundColor.copy(
        saturation = if (isLight) 0.75f else 0.6f,
        luminance = if (isLight) 0.85f else 0.25f
    )

    private val style = TextStyle(
        color = foregroundColor,
        fontSize = textSize,
        textAlign = TextAlign.Center
    )

    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawRect(color = backgroundColor)
        drawIntoCanvas { canvas ->
            canvas.withSave {
                val layout = TextLayoutInput(
                    text = buildAnnotatedString { append(text) },
                    style = style,
                    placeholders = emptyList(),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    density = Density(density, fontScale),
                    layoutDirection = layoutDirection,
                    fontFamilyResolver = fontResolver,
                    constraints = Constraints.fixed(
                        width = size.width.roundToInt(),
                        height = size.height.roundToInt()
                    )
                )

                val paragraph = MultiParagraph(
                    annotatedString = buildAnnotatedString { append(text) },
                    style = style,
                    placeholders = emptyList(),
                    maxLines = 1,
                    ellipsis = false,
                    constraints = Constraints(maxWidth = ceil(size.width).toInt()),
                    density = Density(density, fontScale),
                    fontFamilyResolver = fontResolver
                )

                canvas.translate(dx = 0f, dy = (size.height - paragraph.height) / 2)

                TextPainter.paint(
                    canvas,
                    TextLayoutResult(
                        layoutInput = layout,
                        multiParagraph = paragraph,
                        size = IntSize(width = size.width.toInt(), height = size.height.toInt())
                    )
                )
            }
        }
    }

}
