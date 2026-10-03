package com.example.ui.pet2d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.data.model.PetEntity
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class Particle2D(
    val id: Long,
    val initialX: Float,
    val initialY: Float,
    val type: String, // "HEART", "BUBBLE", "CRUMB", "ZZZ", "CONFETTI", "SPARKLE"
    val color: Color,
    val size: Float = 14f,
    var progress: Float = 0f
)

@Composable
fun Pet2DView(
    pet: PetEntity,
    modifier: Modifier = Modifier,
    isBathActive: Boolean = false,
    isFeedingActive: Boolean = false,
    isGiftActive: Boolean = false,
    isLevelUpActive: Boolean = false,
    onPetTapped: () -> Unit = {}
) {
    // Interactive Look & Parallax Offsets
    var lookTargetX by remember { mutableFloatStateOf(0f) }
    var lookTargetY by remember { mutableFloatStateOf(0f) }
    var currentLookX by remember { mutableFloatStateOf(0f) }
    var currentLookY by remember { mutableFloatStateOf(0f) }

    // Natural Blinking timer
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2400..4200).random().toLong())
            if (!pet.isSleeping) {
                isBlinking = true
                delay(140)
                isBlinking = false
            }
        }
    }

    // Reaction timers: Loved & Surprised
    var isLoving by remember { mutableStateOf(false) }
    LaunchedEffect(isLoving) {
        if (isLoving) {
            delay(1800)
            isLoving = false
        }
    }

    var isSurprised by remember { mutableStateOf(false) }
    LaunchedEffect(isSurprised) {
        if (isSurprised) {
            delay(1200)
            isSurprised = false
        }
    }

    // Particles system
    val particles = remember { mutableStateListOf<Particle2D>() }
    var particleIdCounter by remember { mutableLongStateOf(0L) }

    fun addParticle(x: Float, y: Float, type: String, color: Color, size: Float = 14f) {
        particles.add(Particle2D(particleIdCounter++, x, y, type, color, size))
    }

    // Bath bubbles
    LaunchedEffect(isBathActive) {
        if (isBathActive) {
            while (true) {
                addParticle((100..420).random().toFloat(), (320..540).random().toFloat(), "BUBBLE", Color(0xFF67E8F9), (14..24).random().toFloat())
                delay(160)
            }
        }
    }

    // Food crumbs & sparkles
    LaunchedEffect(isFeedingActive) {
        if (isFeedingActive) {
            repeat(10) {
                addParticle((220..320).random().toFloat(), (350..430).random().toFloat(), "CRUMB", GoldCoin, 8f)
                addParticle((210..330).random().toFloat(), (330..420).random().toFloat(), "SPARKLE", Color(0xFFFDE047), 12f)
                delay(140)
            }
        }
    }

    // Zzz when sleeping
    LaunchedEffect(pet.isSleeping) {
        if (pet.isSleeping) {
            while (true) {
                addParticle((280..340).random().toFloat(), (240..280).random().toFloat(), "ZZZ", Color(0xFFDDD6FE), 15f)
                delay(1200)
            }
        }
    }

    // Confetti & Celebration when Gift or Level Up
    LaunchedEffect(isGiftActive, isLevelUpActive) {
        if (isGiftActive || isLevelUpActive) {
            val confettiColors = listOf(GoldCoin, NeonMint, NeonPink, NeonPurple, Color(0xFF38BDF8), Color(0xFFF97316))
            repeat(18) {
                addParticle(
                    (80..440).random().toFloat(),
                    (200..380).random().toFloat(),
                    "CONFETTI",
                    confettiColors.random(),
                    (10..16).random().toFloat()
                )
                addParticle(
                    (100..420).random().toFloat(),
                    (180..340).random().toFloat(),
                    "SPARKLE",
                    GoldCoin,
                    (12..18).random().toFloat()
                )
                delay(90)
            }
        }
    }

    // Continuous 60 FPS animation loop with delta-time
    var lastFrameNanos by remember { mutableLongStateOf(0L) }
    var totalTimeSeconds by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { nowNanos ->
                if (lastFrameNanos != 0L) {
                    val delta = ((nowNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    totalTimeSeconds += delta

                    // Smooth look-at tracking return
                    currentLookX += (lookTargetX - currentLookX) * (delta * 6f)
                    currentLookY += (lookTargetY - currentLookY) * (delta * 6f)
                    lookTargetX *= (1f - delta * 1.5f)
                    lookTargetY *= (1f - delta * 1.5f)

                    // Update particles
                    val toRemove = mutableListOf<Particle2D>()
                    for (i in 0 until particles.size) {
                        val p = particles[i]
                        p.progress += delta * 1.1f
                        if (p.progress >= 1f) {
                            toRemove.add(p)
                        }
                    }
                    particles.removeAll(toRemove)
                }
                lastFrameNanos = nowNanos
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(pet.id) {
                detectTapGestures(
                    onTap = { offset ->
                        lookTargetX = ((offset.x / size.width) - 0.5f) * 2f
                        lookTargetY = -((offset.y / size.height) - 0.5f) * 2f
                        isLoving = true
                        onPetTapped()

                        // Burst love hearts at tap location
                        addParticle(offset.x, offset.y, "HEART", NeonPink, 18f)
                        addParticle(offset.x + 28f, offset.y - 18f, "HEART", GoldCoin, 14f)
                        addParticle(offset.x - 22f, offset.y - 12f, "HEART", Color(0xFFF472B6), 16f)
                    },
                    onDoubleTap = {
                        isSurprised = true
                    }
                )
            }
            .pointerInput(pet.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        lookTargetX = (lookTargetX + dragAmount.x * 0.015f).coerceIn(-1f, 1f)
                        lookTargetY = (lookTargetY - dragAmount.y * 0.015f).coerceIn(-1f, 1f)
                        if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y) * 1.6f) {
                            change.consume()
                        }
                        if (dragAmount.getDistance() > 40f) {
                            isSurprised = true
                        }
                    }
                )
            }
            .testTag("pet_2d_hero_view"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Cozy Room Decor & Floor & Ground Contact Shadow
            Room2DDecor.drawCozyRoom(
                drawScope = this,
                w = w,
                h = h,
                isSleeping = pet.isSleeping,
                ambientTime = totalTimeSeconds,
                petType = pet.type
            )

            // 1b. Level Up / Celebration Golden Aura Glow
            if (isLevelUpActive || isGiftActive) {
                val auraPulse = sin(totalTimeSeconds * 8f) * 20f + 160f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x88FFD700), Color(0x33FFB703), Color(0x00000000)),
                        center = Offset(w * 0.5f, h * 0.54f),
                        radius = auraPulse
                    ),
                    radius = auraPulse,
                    center = Offset(w * 0.5f, h * 0.54f)
                )
            }

            // 2. Draw Layered 2.5D Animated Pet Character (Hero Scale: 1.38f)
            PetCharacterRenderer.drawPet(
                drawScope = this,
                pet = pet,
                centerX = w * 0.5f,
                centerY = h * 0.58f,
                animTime = totalTimeSeconds,
                lookTargetX = currentLookX,
                lookTargetY = currentLookY,
                isBlinking = isBlinking,
                isEating = isFeedingActive,
                isBathActive = isBathActive,
                isLoving = isLoving,
                isGiftActive = isGiftActive,
                isLevelUpActive = isLevelUpActive,
                isSurprised = isSurprised,
                scale = 1.38f
            )

            // 3. Draw Floating Action Particles (Hearts, Bubbles, Crumbs, Zzz, Confetti)
            particles.forEach { p ->
                val floatY = p.initialY - (p.progress * 140f)
                val floatX = p.initialX + sin(p.progress * 6f) * 18f
                val alpha = (1f - p.progress).coerceIn(0f, 1f)

                when (p.type) {
                    "HEART" -> drawHeart(floatX, floatY, p.size * (1f - p.progress * 0.2f), p.color.copy(alpha = alpha))
                    "BUBBLE" -> drawBubble(floatX, floatY, p.size, p.color.copy(alpha = alpha))
                    "ZZZ" -> drawZzz(floatX, floatY, p.size * (1f + p.progress * 0.35f), p.color.copy(alpha = alpha))
                    "CONFETTI" -> drawConfetti(floatX, floatY, p.size, p.color.copy(alpha = alpha), p.progress)
                    else -> drawStar(floatX, floatY, p.size, p.color.copy(alpha = alpha))
                }
            }
        }
    }
}

fun DrawScope.drawHeart(x: Float, y: Float, size: Float, color: Color) {
    val heart = Path().apply {
        moveTo(x, y + size * 0.5f)
        cubicTo(x - size, y - size * 0.2f, x - size, y - size * 0.8f, x, y - size * 0.3f)
        cubicTo(x + size, y - size * 0.8f, x + size, y - size * 0.2f, x, y + size * 0.5f)
        close()
    }
    drawPath(heart, color)
}

fun DrawScope.drawBubble(x: Float, y: Float, radius: Float, color: Color) {
    drawCircle(color.copy(alpha = color.alpha * 0.6f), radius = radius, center = Offset(x, y))
    drawCircle(Color.White.copy(alpha = color.alpha * 0.85f), radius = radius * 0.3f, center = Offset(x - radius * 0.35f, y - radius * 0.35f))
    drawCircle(color, radius = radius, center = Offset(x, y), style = Stroke(width = 2.5f))
}

fun DrawScope.drawStar(x: Float, y: Float, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(x, y - radius)
        lineTo(x + radius * 0.35f, y - radius * 0.35f)
        lineTo(x + radius, y)
        lineTo(x + radius * 0.35f, y + radius * 0.35f)
        lineTo(x, y + radius)
        lineTo(x - radius * 0.35f, y + radius * 0.35f)
        lineTo(x - radius, y)
        lineTo(x - radius * 0.35f, y - radius * 0.35f)
        close()
    }
    drawPath(path, color)
}

fun DrawScope.drawConfetti(x: Float, y: Float, size: Float, color: Color, progress: Float) {
    val rot = progress * 720f
    val cosR = cos(Math.toRadians(rot.toDouble())).toFloat()
    val sinR = sin(Math.toRadians(rot.toDouble())).toFloat()

    val path = Path().apply {
        moveTo(x - size * cosR, y - size * sinR)
        lineTo(x + size * cosR, y + size * sinR)
        lineTo(x + size * cosR - 4f * sinR, y + size * sinR + 4f * cosR)
        lineTo(x - size * cosR - 4f * sinR, y - size * sinR + 4f * cosR)
        close()
    }
    drawPath(path, color)
}

fun DrawScope.drawZzz(x: Float, y: Float, size: Float, color: Color) {
    val zPath = Path().apply {
        moveTo(x - size, y - size)
        lineTo(x + size, y - size)
        lineTo(x - size, y + size)
        lineTo(x + size, y + size)
    }
    drawPath(zPath, color, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
}
