package com.foxnet.medications.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.foxnet.medications.ui.theme.spacing
import kotlin.math.sin

@Composable
fun WavyDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    thickness: Dp = 2.dp,
    waviness: Dp = 8.dp,
    isAnimated: Boolean = false,
    animationDurationMillis: Int = 2000
) {
    // 1. Calculate an infinite shift offset if animation is requested
    val infiniteTransition = rememberInfiniteTransition(label = "WaveTransition")
    val waveOffset by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(animationDurationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "WaveOffset"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // 2. Compute Canvas height to safely fit the wave amplitude + stroke thickness
    val totalHeight = waviness + thickness

    Canvas(
        modifier = modifier
            .height(totalHeight)
    ) {
        val widthPx = size.width
        val heightPx = size.height
        
        // Single waviness value controls both wavelength and amplitude for a constant look
        // Increasing the multiplier makes the waves wider (longer wavelength)
        val waveLengthPx = (waviness * 4f).toPx()
        val amplitudePx = waviness.toPx() / 2f
        val thicknessPx = thickness.toPx()

        if (waveLengthPx <= 0f) return@Canvas

        val midY = heightPx / 2f

        // 3. Build a continuous sine wave path
        val path = Path().apply {
            val phaseShift = waveOffset * 2f * Math.PI.toFloat()
            val stepPx = 1f
            var x = 0f
            
            val firstY = midY + amplitudePx * sin((x / waveLengthPx) * 2f * Math.PI.toFloat() - phaseShift)
            moveTo(x, firstY)

            while (x < widthPx) {
                x += stepPx
                val effectiveX = x.coerceAtMost(widthPx)
                val angle = (effectiveX / waveLengthPx) * 2f * Math.PI.toFloat() - phaseShift
                val y = midY + amplitudePx * sin(angle)
                lineTo(effectiveX, y)
            }
        }

        // 4. Render the path onto the canvas
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = thicknessPx)
        )
    }
}
