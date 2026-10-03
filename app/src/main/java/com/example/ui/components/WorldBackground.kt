package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.WorldBeachSky
import com.example.ui.theme.WorldBedroomSky
import com.example.ui.theme.WorldForestSky
import com.example.ui.theme.WorldGardenSky
import com.example.ui.theme.WorldNightSky
import com.example.ui.theme.WorldParkSky
import kotlin.math.sin

@Composable
fun WorldBackground(
    worldId: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "worldAmbient")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambientWave"
    )

    val twinkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (worldId) {
            "world_bedroom", "BEDROOM" -> drawBedroomWorld(w, h)
            "world_garden", "GARDEN" -> drawGardenWorld(w, h, waveOffset)
            "world_park", "PARK" -> drawParkWorld(w, h, waveOffset)
            "world_beach", "BEACH" -> drawBeachWorld(w, h, waveOffset)
            "world_forest", "FOREST" -> drawForestWorld(w, h, waveOffset)
            "world_night", "NIGHT" -> drawNightWorld(w, h, twinkleAlpha, waveOffset)
            else -> drawBedroomWorld(w, h)
        }
    }
}

fun DrawScope.drawBedroomWorld(w: Float, h: Float) {
    // Warm deep purple bedroom walls
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(WorldBedroomSky, DarkBackground),
            startY = 0f,
            endY = h * 0.7f
        ),
        size = Size(w, h * 0.7f)
    )

    // Floor (wooden planks tone)
    drawRect(
        color = Color(0xFF1E1333),
        topLeft = Offset(0f, h * 0.7f),
        size = Size(w, h * 0.3f)
    )
    drawLine(
        color = Color(0xFF3B2566),
        start = Offset(0f, h * 0.7f),
        end = Offset(w, h * 0.7f),
        strokeWidth = 3f
    )

    // Window on the left
    val winX = w * 0.12f
    val winY = h * 0.12f
    val winW = w * 0.35f
    val winH = h * 0.28f

    // Window glow & glass
    drawRoundRect(
        color = Color(0xFF140D28),
        topLeft = Offset(winX, winY),
        size = Size(winW, winH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )
    // Outside stars in window
    drawCircle(GoldCoin, radius = 2.5f, center = Offset(winX + winW * 0.3f, winY + winH * 0.3f))
    drawCircle(GoldCoin, radius = 3.5f, center = Offset(winX + winW * 0.7f, winY + winH * 0.25f))
    drawCircle(Color(0xFFE2E8F0), radius = 2f, center = Offset(winX + winW * 0.5f, winY + winH * 0.6f))
    // Window frame
    drawLine(Color(0xFF4C2D80), Offset(winX + winW / 2f, winY), Offset(winX + winW / 2f, winY + winH), strokeWidth = 3f)
    drawLine(Color(0xFF4C2D80), Offset(winX, winY + winH / 2f), Offset(winX + winW, winY + winH / 2f), strokeWidth = 3f)

    // Cozy Rug under the pet
    drawOval(
        color = Color(0xFF371E68),
        topLeft = Offset(w * 0.15f, h * 0.68f),
        size = Size(w * 0.7f, h * 0.14f)
    )
    drawOval(
        color = NeonPurple.copy(alpha = 0.4f),
        topLeft = Offset(w * 0.22f, h * 0.70f),
        size = Size(w * 0.56f, h * 0.10f)
    )
}

fun DrawScope.drawGardenWorld(w: Float, h: Float, wave: Float) {
    // Sky gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(WorldGardenSky, Color(0xFF0F241A)),
            startY = 0f,
            endY = h * 0.65f
        ),
        size = Size(w, h * 0.65f)
    )

    // Soft sun
    drawCircle(
        color = Color(0x33FFE600),
        radius = 55f,
        center = Offset(w * 0.82f, h * 0.18f)
    )
    drawCircle(
        color = Color(0x80FFE600),
        radius = 35f,
        center = Offset(w * 0.82f, h * 0.18f)
    )

    // Grass hills
    val hill1 = Path().apply {
        moveTo(0f, h * 0.62f)
        cubicTo(w * 0.3f, h * 0.55f, w * 0.7f, h * 0.68f, w, h * 0.58f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(hill1, Color(0xFF1B4D35))

    val hill2 = Path().apply {
        moveTo(0f, h * 0.68f)
        cubicTo(w * 0.4f, h * 0.73f, w * 0.6f, h * 0.64f, w, h * 0.70f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(hill2, Color(0xFF133827))

    // Animated flowers
    for (i in 1..5) {
        val fx = (w * 0.16f) * i
        val fy = h * 0.74f + sin(wave + i).toFloat() * 3f
        drawCircle(GoldCoin, radius = 6f, center = Offset(fx, fy))
        drawCircle(NeonMint, radius = 3f, center = Offset(fx, fy))
    }
}

fun DrawScope.drawParkWorld(w: Float, h: Float, wave: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(WorldParkSky, Color(0xFF0C2422)),
            startY = 0f,
            endY = h * 0.65f
        ),
        size = Size(w, h * 0.65f)
    )

    // Distant park trees
    drawCircle(Color(0xFF1B4B47), radius = 60f, center = Offset(w * 0.18f, h * 0.56f))
    drawCircle(Color(0xFF225B56), radius = 75f, center = Offset(w * 0.85f, h * 0.52f))

    // Lawn
    drawRect(
        color = Color(0xFF113835),
        topLeft = Offset(0f, h * 0.66f),
        size = Size(w, h * 0.34f)
    )

    // Paved stone pathway
    val path = Path().apply {
        moveTo(w * 0.35f, h * 0.66f)
        lineTo(w * 0.65f, h * 0.66f)
        lineTo(w * 0.85f, h)
        lineTo(w * 0.15f, h)
        close()
    }
    drawPath(path, Color(0xFF223835))
}

fun DrawScope.drawBeachWorld(w: Float, h: Float, wave: Float) {
    // Ocean sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(WorldBeachSky, Color(0xFF08273A)),
            startY = 0f,
            endY = h * 0.55f
        ),
        size = Size(w, h * 0.55f)
    )

    // Animated Ocean waves
    val wavePath = Path().apply {
        moveTo(0f, h * 0.55f)
        for (step in 0..10) {
            val wx = (w / 10f) * step
            val wy = h * 0.55f + sin(wave + step * 0.6f).toFloat() * 8f
            lineTo(wx, wy)
        }
        lineTo(w, h * 0.70f)
        lineTo(0f, h * 0.70f)
        close()
    }
    drawPath(wavePath, Color(0xFF0E5275))

    // Sandy beach shore
    drawRect(
        color = Color(0xFF785E3A),
        topLeft = Offset(0f, h * 0.68f),
        size = Size(w, h * 0.32f)
    )
    drawOval(
        color = Color(0xFF917246),
        topLeft = Offset(w * 0.2f, h * 0.71f),
        size = Size(w * 0.6f, h * 0.12f)
    )
}

fun DrawScope.drawForestWorld(w: Float, h: Float, wave: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(WorldForestSky, Color(0xFF0C1F15)),
            startY = 0f,
            endY = h * 0.65f
        ),
        size = Size(w, h * 0.65f)
    )

    // Pine trees silhouettes
    fun drawPine(px: Float, py: Float, scale: Float) {
        val pine = Path().apply {
            moveTo(px, py - 90f * scale)
            lineTo(px - 35f * scale, py - 40f * scale)
            lineTo(px - 20f * scale, py - 40f * scale)
            lineTo(px - 45f * scale, py)
            lineTo(px + 45f * scale, py)
            lineTo(px + 20f * scale, py - 40f * scale)
            lineTo(px + 35f * scale, py - 40f * scale)
            close()
        }
        drawPath(pine, Color(0xFF133623))
    }

    drawPine(w * 0.14f, h * 0.66f, 0.8f)
    drawPine(w * 0.86f, h * 0.65f, 0.9f)
    drawPine(w * 0.3f, h * 0.64f, 0.65f)

    // Forest floor with moss
    drawRect(
        color = Color(0xFF0F2619),
        topLeft = Offset(0f, h * 0.66f),
        size = Size(w, h * 0.34f)
    )
    drawOval(
        color = Color(0xFF1A452C),
        topLeft = Offset(w * 0.15f, h * 0.69f),
        size = Size(w * 0.7f, h * 0.15f)
    )
}

fun DrawScope.drawNightWorld(w: Float, h: Float, twinkle: Float, wave: Float) {
    // Deep midnight sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(WorldNightSky, Color(0xFF140832)),
            startY = 0f,
            endY = h * 0.7f
        ),
        size = Size(w, h * 0.7f)
    )

    // Glowing Moon
    drawCircle(
        color = Color(0x33DDD6FE),
        radius = 55f,
        center = Offset(w * 0.8f, h * 0.18f)
    )
    drawCircle(
        color = Color(0xFFEDE9FE),
        radius = 32f,
        center = Offset(w * 0.8f, h * 0.18f)
    )

    // Twinkling stars
    val starCoords = listOf(
        Pair(0.15f, 0.12f), Pair(0.32f, 0.08f), Pair(0.48f, 0.22f),
        Pair(0.65f, 0.14f), Pair(0.24f, 0.35f), Pair(0.55f, 0.40f)
    )
    starCoords.forEach { (sx, sy) ->
        drawCircle(
            color = Color.White.copy(alpha = twinkle),
            radius = 2.8f,
            center = Offset(w * sx, h * sy)
        )
    }

    // Floating fireflies
    for (i in 0..4) {
        val ffx = w * (0.2f + (i * 0.15f)) + sin(wave + i).toFloat() * 12f
        val ffy = h * 0.62f + sin(wave * 2f + i).toFloat() * 8f
        drawCircle(NeonMint.copy(alpha = 0.3f), radius = 9f, center = Offset(ffx, ffy))
        drawCircle(GoldCoin, radius = 3.5f, center = Offset(ffx, ffy))
    }

    // Ground
    drawRect(
        color = Color(0xFF0F0726),
        topLeft = Offset(0f, h * 0.7f),
        size = Size(w, h * 0.3f)
    )
    drawOval(
        color = Color(0xFF221152),
        topLeft = Offset(w * 0.15f, h * 0.68f),
        size = Size(w * 0.7f, h * 0.15f)
    )
}
