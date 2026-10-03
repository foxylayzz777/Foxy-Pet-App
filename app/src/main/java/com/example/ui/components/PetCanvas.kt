package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.model.PetEntity
import com.example.data.model.PetMood
import com.example.data.model.PetType
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class FloatingParticle(
    val id: Long,
    val initialX: Float,
    val initialY: Float,
    val iconEmoji: String,
    val color: Color,
    val progress: Float = 0f
)

@Composable
fun PetCanvas(
    pet: PetEntity,
    modifier: Modifier = Modifier,
    isBathActive: Boolean = false,
    isFeedingActive: Boolean = false,
    onPetTapped: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "petAnimations")

    // Idle breathing vertical offset
    val breathingOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Tail wagging angle
    val tailAngle by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (pet.calculateMood() == PetMood.ECSTATIC) 250 else 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tailWag"
    )

    // Ear twitch
    val earTwitch by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "earTwitch"
    )

    // Blinking logic
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2500..5000).random().toLong())
            if (!pet.isSleeping) {
                isBlinking = true
                delay(160)
                isBlinking = false
            }
        }
    }

    // Interactive floating particles (hearts, bubbles, crumbs)
    val particles = remember { mutableStateListOf<FloatingParticle>() }
    var particleCounter by remember { mutableStateOf(0L) }

    fun addParticle(x: Float, y: Float, emoji: String, color: Color) {
        val p = FloatingParticle(particleCounter++, x, y, emoji, color)
        particles.add(p)
    }

    // Animate particles upward and fade
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    LaunchedEffect(particlePhase) {
        if (particles.isNotEmpty()) {
            val toRemove = mutableListOf<FloatingParticle>()
            for (i in 0 until particles.size) {
                val p = particles[i]
                val nextProgress = p.progress + 0.12f
                if (nextProgress >= 1f) {
                    toRemove.add(p)
                } else {
                    particles[i] = p.copy(progress = nextProgress)
                }
            }
            particles.removeAll(toRemove)
        }
    }

    // Add bath bubbles when bathing
    LaunchedEffect(isBathActive) {
        if (isBathActive) {
            while (true) {
                addParticle((100..400).random().toFloat(), (300..500).random().toFloat(), "🫧", Color(0xFF67E8F9))
                delay(200)
            }
        }
    }

    // Add food crumbs when feeding
    LaunchedEffect(isFeedingActive) {
        if (isFeedingActive) {
            for (i in 0..6) {
                addParticle((220..280).random().toFloat(), (350..420).random().toFloat(), "✨", GoldCoin)
                delay(150)
            }
        }
    }

    val mood = pet.calculateMood()

    Box(
        modifier = modifier
            .pointerInput(pet.id) {
                detectTapGestures(
                    onTap = { offset ->
                        onPetTapped()
                        // Spawn 2-3 love hearts near tap
                        addParticle(offset.x, offset.y, "❤️", NeonPink)
                        addParticle(offset.x + 30f, offset.y - 20f, "✨", GoldCoin)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f + breathingOffset

            // Skin palettes
            val skinPalette = getSkinPalette(pet.type, pet.skinId)

            // Draw accessories behind body (like wings)
            if (pet.selectedAccessory == "acc_wings") {
                drawAngelWings(centerX, centerY - 20f)
            }

            // Draw tail
            drawTail(pet.type, centerX, centerY, tailAngle, skinPalette)

            // Draw body shadow
            drawOval(
                color = Color(0x33000000),
                topLeft = Offset(centerX - 90f, centerY + 130f),
                size = Size(180f, 32f)
            )

            // Draw pet body / torso
            drawBody(pet.type, centerX, centerY, skinPalette, pet.selectedClothes)

            // Draw collar
            if (pet.selectedCollar != null) {
                drawCollar(centerX, centerY + 28f, pet.selectedCollar)
            }

            // Draw head
            drawHead(pet.type, centerX, centerY - 65f, earTwitch, skinPalette)

            // Draw face (eyes, nose, mouth, cheeks)
            drawFace(
                pet = pet,
                centerX = centerX,
                centerY = centerY - 65f,
                isBlinking = isBlinking || pet.isSleeping,
                isFeeding = isFeedingActive
            )

            // Draw glasses
            if (pet.selectedGlasses != null) {
                drawGlasses(centerX, centerY - 72f, pet.selectedGlasses)
            }

            // Draw hat
            if (pet.selectedHat != null) {
                drawHat(centerX, centerY - 145f, pet.selectedHat)
            }

            // Draw floating halo if equipped
            if (pet.selectedAccessory == "acc_halo") {
                drawHalo(centerX, centerY - 175f)
            }

            // Draw sleeping "Zzz" effect
            if (pet.isSleeping) {
                drawSleepingZzz(centerX + 65f, centerY - 130f, breathingOffset)
            }

            // Render active floating particles
            particles.forEach { p ->
                val floatY = p.initialY - (p.progress * 130f)
                val floatX = p.initialX + sin(p.progress * 6f) * 18f
                val alpha = (1f - p.progress).coerceIn(0f, 1f)

                if (p.iconEmoji == "❤️") {
                    drawHeart(floatX, floatY, 14f * (1f - p.progress * 0.3f), p.color.copy(alpha = alpha))
                } else if (p.iconEmoji == "🫧") {
                    drawBubble(floatX, floatY, 16f, p.color.copy(alpha = alpha))
                } else {
                    drawStarParticle(floatX, floatY, 8f, p.color.copy(alpha = alpha))
                }
            }
        }
    }
}

data class SkinPalette(
    val primary: Color,
    val secondary: Color,
    val detail: Color,
    val innerEar: Color,
    val chest: Color
)

fun getSkinPalette(type: PetType, skinId: String): SkinPalette {
    return when (skinId) {
        "skin_neon" -> SkinPalette(
            primary = Color(0xFF1E1B4B),
            secondary = NeonMint,
            detail = NeonPink,
            innerEar = NeonMint,
            chest = Color(0xFF0F172A)
        )
        "skin_galaxy" -> SkinPalette(
            primary = Color(0xFF31105E),
            secondary = Color(0xFFC084FC),
            detail = Color(0xFFE879F9),
            innerEar = Color(0xFF7E22CE),
            chest = Color(0xFF4C1D95)
        )
        "skin_sakura" -> SkinPalette(
            primary = Color(0xFFFFB7D5),
            secondary = Color(0xFFFFF0F5),
            detail = Color(0xFFFF4081),
            innerEar = Color(0xFFFF80AB),
            chest = Color(0xFFFFF5F9)
        )
        "skin_golden" -> SkinPalette(
            primary = Color(0xFFFFD166),
            secondary = Color(0xFFFFF3B0),
            detail = Color(0xFFD4A373),
            innerEar = Color(0xFFFFBE0B),
            chest = Color(0xFFFFFCF2)
        )
        else -> {
            // Default species colors
            when (type) {
                PetType.FOX -> SkinPalette(
                    primary = Color(0xFFFF6B35),
                    secondary = Color(0xFFFFF3E0),
                    detail = Color(0xFF3B1E08),
                    innerEar = Color(0xFFFFA07A),
                    chest = Color(0xFFFFF4E6)
                )
                PetType.CAT -> SkinPalette(
                    primary = Color(0xFFE2E8F0),
                    secondary = Color(0xFF64748B),
                    detail = Color(0xFF1E293B),
                    innerEar = Color(0xFFF472B6),
                    chest = Color(0xFFFFFFFF)
                )
                PetType.DOG -> SkinPalette(
                    primary = Color(0xFFD4A373),
                    secondary = Color(0xFFCCD5AE),
                    detail = Color(0xFF4A3728),
                    innerEar = Color(0xFFE9EDC9),
                    chest = Color(0xFFFAEDCD)
                )
            }
        }
    }
}

fun DrawScope.drawBody(
    type: PetType,
    centerX: Float,
    centerY: Float,
    palette: SkinPalette,
    clothesId: String?
) {
    val bodyWidth = 130f
    val bodyHeight = 110f

    // Main torso
    val bodyColor = when (clothesId) {
        "clothes_sweater" -> Color(0xFFF59E0B)
        "clothes_hoodie" -> NeonPurple
        "clothes_cape" -> Color(0xFFEF4444)
        "clothes_tux" -> Color(0xFF1E293B)
        else -> palette.primary
    }

    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(centerX - bodyWidth / 2f, centerY),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(45f, 45f)
    )

    // Chest fluff / patch if no heavy clothes
    if (clothesId == null) {
        drawOval(
            color = palette.chest,
            topLeft = Offset(centerX - 35f, centerY + 10f),
            size = Size(70f, 75f)
        )
    } else if (clothesId == "clothes_tux") {
        // White shirt and tie
        drawOval(
            color = Color.White,
            topLeft = Offset(centerX - 24f, centerY + 10f),
            size = Size(48f, 55f)
        )
        // Red bowtie
        drawOval(
            color = Color(0xFFDC2626),
            topLeft = Offset(centerX - 16f, centerY + 14f),
            size = Size(32f, 16f)
        )
    }

    // Paws (left & right)
    drawOval(
        color = palette.secondary,
        topLeft = Offset(centerX - 60f, centerY + 95f),
        size = Size(38f, 32f)
    )
    drawOval(
        color = palette.secondary,
        topLeft = Offset(centerX + 22f, centerY + 95f),
        size = Size(38f, 32f)
    )
}

fun DrawScope.drawTail(
    type: PetType,
    centerX: Float,
    centerY: Float,
    angle: Float,
    palette: SkinPalette
) {
    val tailPath = Path()
    val rad = Math.toRadians(angle.toDouble())
    val tailEndX = (centerX + 80f + cos(rad) * 45f).toFloat()
    val tailEndY = (centerY + 60f + sin(rad) * 35f).toFloat()

    when (type) {
        PetType.FOX -> {
            // Big fluffy bushy fox tail
            drawOval(
                color = palette.primary,
                topLeft = Offset(centerX + 40f, centerY + 30f),
                size = Size(85f, 50f)
            )
            // White tip
            drawOval(
                color = palette.secondary,
                topLeft = Offset(centerX + 90f, centerY + 25f),
                size = Size(45f, 38f)
            )
        }
        PetType.CAT -> {
            // Curved sleek cat tail
            tailPath.moveTo(centerX + 50f, centerY + 80f)
            tailPath.quadraticTo(
                centerX + 95f, centerY + 30f + sin(rad).toFloat() * 20f,
                tailEndX, tailEndY
            )
            drawPath(
                path = tailPath,
                color = palette.primary,
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )
        }
        PetType.DOG -> {
            // Happy wagging puppy tail
            drawOval(
                color = palette.primary,
                topLeft = Offset(centerX + 45f, centerY + 45f),
                size = Size(65f, 32f)
            )
        }
    }
}

fun DrawScope.drawHead(
    type: PetType,
    centerX: Float,
    centerY: Float,
    earTwitch: Float,
    palette: SkinPalette
) {
    // Ears
    when (type) {
        PetType.FOX -> {
            // Pointed fluffy triangular ears
            val leftEar = Path().apply {
                moveTo(centerX - 68f, centerY - 15f)
                lineTo(centerX - 42f, centerY - 95f + earTwitch)
                lineTo(centerX - 15f, centerY - 25f)
                close()
            }
            drawPath(leftEar, palette.primary)

            val leftInner = Path().apply {
                moveTo(centerX - 60f, centerY - 20f)
                lineTo(centerX - 42f, centerY - 82f + earTwitch)
                lineTo(centerX - 24f, centerY - 28f)
                close()
            }
            drawPath(leftInner, palette.innerEar)

            val rightEar = Path().apply {
                moveTo(centerX + 15f, centerY - 25f)
                lineTo(centerX + 42f, centerY - 95f - earTwitch)
                lineTo(centerX + 68f, centerY - 15f)
                close()
            }
            drawPath(rightEar, palette.primary)

            val rightInner = Path().apply {
                moveTo(centerX + 24f, centerY - 28f)
                lineTo(centerX + 42f, centerY - 82f - earTwitch)
                lineTo(centerX + 60f, centerY - 20f)
                close()
            }
            drawPath(rightInner, palette.innerEar)
        }
        PetType.CAT -> {
            // Perky triangular cat ears
            val leftEar = Path().apply {
                moveTo(centerX - 60f, centerY - 10f)
                lineTo(centerX - 42f, centerY - 80f + earTwitch)
                lineTo(centerX - 18f, centerY - 25f)
                close()
            }
            drawPath(leftEar, palette.primary)
            val leftInner = Path().apply {
                moveTo(centerX - 52f, centerY - 15f)
                lineTo(centerX - 42f, centerY - 70f + earTwitch)
                lineTo(centerX - 26f, centerY - 26f)
                close()
            }
            drawPath(leftInner, palette.innerEar)

            val rightEar = Path().apply {
                moveTo(centerX + 18f, centerY - 25f)
                lineTo(centerX + 42f, centerY - 80f - earTwitch)
                lineTo(centerX + 60f, centerY - 10f)
                close()
            }
            drawPath(rightEar, palette.primary)
            val rightInner = Path().apply {
                moveTo(centerX + 26f, centerY - 26f)
                lineTo(centerX + 42f, centerY - 70f - earTwitch)
                lineTo(centerX + 52f, centerY - 15f)
                close()
            }
            drawPath(rightInner, palette.innerEar)
        }
        PetType.DOG -> {
            // Floppy puppy ears hanging down
            drawOval(
                color = palette.detail,
                topLeft = Offset(centerX - 78f, centerY - 50f + earTwitch),
                size = Size(36f, 75f)
            )
            drawOval(
                color = palette.detail,
                topLeft = Offset(centerX + 42f, centerY - 50f - earTwitch),
                size = Size(36f, 75f)
            )
        }
    }

    // Main Head base
    drawOval(
        color = palette.primary,
        topLeft = Offset(centerX - 68f, centerY - 48f),
        size = Size(136f, 105f)
    )

    // Cheek fluff / Muzzle
    drawOval(
        color = palette.secondary,
        topLeft = Offset(centerX - 52f, centerY - 6f),
        size = Size(104f, 65f)
    )
}

fun DrawScope.drawFace(
    pet: PetEntity,
    centerX: Float,
    centerY: Float,
    isBlinking: Boolean,
    isFeeding: Boolean
) {
    val eyeY = centerY - 10f
    val eyeSpacing = 28f

    if (isBlinking || pet.isSleeping) {
        // Closed smiling eyes curves
        val leftEye = Path().apply {
            moveTo(centerX - eyeSpacing - 14f, eyeY)
            quadraticTo(centerX - eyeSpacing, eyeY + 10f, centerX - eyeSpacing + 14f, eyeY)
        }
        val rightEye = Path().apply {
            moveTo(centerX + eyeSpacing - 14f, eyeY)
            quadraticTo(centerX + eyeSpacing, eyeY + 10f, centerX + eyeSpacing + 14f, eyeY)
        }
        drawPath(leftEye, Color(0xFF1E1035), style = Stroke(width = 4f, cap = StrokeCap.Round))
        drawPath(rightEye, Color(0xFF1E1035), style = Stroke(width = 4f, cap = StrokeCap.Round))
    } else {
        // Big cute sparkling eyes
        drawOval(
            color = Color(0xFF1E1035),
            topLeft = Offset(centerX - eyeSpacing - 12f, eyeY - 14f),
            size = Size(24f, 28f)
        )
        // Highlights
        drawCircle(
            color = Color.White,
            radius = 5.5f,
            center = Offset(centerX - eyeSpacing - 4f, eyeY - 6f)
        )
        drawCircle(
            color = Color.White,
            radius = 2.5f,
            center = Offset(centerX - eyeSpacing + 3f, eyeY + 4f)
        )

        drawOval(
            color = Color(0xFF1E1035),
            topLeft = Offset(centerX + eyeSpacing - 12f, eyeY - 14f),
            size = Size(24f, 28f)
        )
        drawCircle(
            color = Color.White,
            radius = 5.5f,
            center = Offset(centerX + eyeSpacing - 4f, eyeY - 6f)
        )
        drawCircle(
            color = Color.White,
            radius = 2.5f,
            center = Offset(centerX + eyeSpacing + 3f, eyeY + 4f)
        )
    }

    // Rosy pink cheeks
    drawOval(
        color = Color(0x66FF2A85),
        topLeft = Offset(centerX - 56f, eyeY + 10f),
        size = Size(22f, 12f)
    )
    drawOval(
        color = Color(0x66FF2A85),
        topLeft = Offset(centerX + 34f, eyeY + 10f),
        size = Size(22f, 12f)
    )

    // Nose
    drawOval(
        color = Color(0xFF1E1035),
        topLeft = Offset(centerX - 7f, centerY + 8f),
        size = Size(14f, 10f)
    )

    // Mouth
    if (isFeeding) {
        // Chewing open mouth
        drawOval(
            color = Color(0xFFEF4444),
            topLeft = Offset(centerX - 9f, centerY + 20f),
            size = Size(18f, 14f)
        )
    } else if (pet.type == PetType.DOG && pet.calculateMood() == PetMood.ECSTATIC) {
        // Dog sticking tongue out!
        val mouthPath = Path().apply {
            moveTo(centerX - 10f, centerY + 20f)
            quadraticTo(centerX, centerY + 24f, centerX + 10f, centerY + 20f)
        }
        drawPath(mouthPath, Color(0xFF1E1035), style = Stroke(width = 3f, cap = StrokeCap.Round))
        drawOval(
            color = Color(0xFFFB7185),
            topLeft = Offset(centerX - 6f, centerY + 22f),
            size = Size(12f, 16f)
        )
    } else {
        // Cute smiling w-shaped mouth
        val mouthPath = Path().apply {
            moveTo(centerX - 10f, centerY + 20f)
            quadraticTo(centerX - 5f, centerY + 26f, centerX, centerY + 20f)
            quadraticTo(centerX + 5f, centerY + 26f, centerX + 10f, centerY + 20f)
        }
        drawPath(mouthPath, Color(0xFF1E1035), style = Stroke(width = 3f, cap = StrokeCap.Round))
    }

    // Cat whiskers if Cat
    if (pet.type == PetType.CAT) {
        drawLine(Color(0xFF64748B), Offset(centerX - 35f, centerY + 14f), Offset(centerX - 65f, centerY + 10f), strokeWidth = 2.5f)
        drawLine(Color(0xFF64748B), Offset(centerX - 35f, centerY + 22f), Offset(centerX - 65f, centerY + 24f), strokeWidth = 2.5f)

        drawLine(Color(0xFF64748B), Offset(centerX + 35f, centerY + 14f), Offset(centerX + 65f, centerY + 10f), strokeWidth = 2.5f)
        drawLine(Color(0xFF64748B), Offset(centerX + 35f, centerY + 22f), Offset(centerX + 65f, centerY + 24f), strokeWidth = 2.5f)
    }
}

fun DrawScope.drawCollar(centerX: Float, centerY: Float, collarId: String) {
    val collarColor = when (collarId) {
        "collar_bell" -> Color(0xFFEF4444)
        "collar_spikes" -> Color(0xFF475569)
        "collar_neon" -> NeonMint
        "collar_ribbon" -> NeonPink
        else -> NeonPurple
    }
    // Collar band
    drawRoundRect(
        color = collarColor,
        topLeft = Offset(centerX - 46f, centerY),
        size = Size(92f, 14f),
        cornerRadius = CornerRadius(7f, 7f)
    )

    // Bell or medallion
    if (collarId == "collar_bell") {
        drawCircle(
            color = GoldCoin,
            radius = 9f,
            center = Offset(centerX, centerY + 14f)
        )
    } else if (collarId == "collar_ribbon") {
        drawOval(
            color = NeonPink,
            topLeft = Offset(centerX - 14f, centerY + 8f),
            size = Size(28f, 14f)
        )
    } else if (collarId == "collar_spikes") {
        for (i in -2..2) {
            drawCircle(GoldCoin, radius = 3.5f, center = Offset(centerX + i * 18f, centerY + 7f))
        }
    }
}

fun DrawScope.drawGlasses(centerX: Float, centerY: Float, glassesId: String) {
    when (glassesId) {
        "glasses_shades" -> {
            // Cool black/neon sunglasses
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(centerX - 56f, centerY),
                size = Size(46f, 28f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(centerX + 10f, centerY),
                size = Size(46f, 28f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Bridge
            drawRect(Color(0xFF0F172A), Offset(centerX - 10f, centerY + 4f), Size(20f, 6f))
            // Neon sheen line
            drawLine(NeonMint, Offset(centerX - 50f, centerY + 8f), Offset(centerX - 20f, centerY + 22f), strokeWidth = 3f)
            drawLine(NeonMint, Offset(centerX + 16f, centerY + 8f), Offset(centerX + 46f, centerY + 22f), strokeWidth = 3f)
        }
        "glasses_nerd" -> {
            // Round wire frames
            drawCircle(Color(0xFF334155), radius = 18f, center = Offset(centerX - 30f, centerY + 12f), style = Stroke(width = 3.5f))
            drawCircle(Color(0xFF334155), radius = 18f, center = Offset(centerX + 30f, centerY + 12f), style = Stroke(width = 3.5f))
            drawLine(Color(0xFF334155), Offset(centerX - 12f, centerY + 12f), Offset(centerX + 12f, centerY + 12f), strokeWidth = 3f)
        }
        "glasses_star" -> {
            drawCircle(GoldCoin, radius = 16f, center = Offset(centerX - 30f, centerY + 12f))
            drawCircle(GoldCoin, radius = 16f, center = Offset(centerX + 30f, centerY + 12f))
            drawLine(GoldCoin, Offset(centerX - 14f, centerY + 12f), Offset(centerX + 14f, centerY + 12f), strokeWidth = 4f)
        }
        "glasses_monocle" -> {
            drawCircle(GoldCoin, radius = 17f, center = Offset(centerX + 28f, centerY + 12f), style = Stroke(width = 3.5f))
            drawLine(GoldCoin, Offset(centerX + 45f, centerY + 18f), Offset(centerX + 55f, centerY + 50f), strokeWidth = 2.5f)
        }
    }
}

fun DrawScope.drawHat(centerX: Float, centerY: Float, hatId: String) {
    when (hatId) {
        "hat_party" -> {
            // Party cone
            val cone = Path().apply {
                moveTo(centerX - 30f, centerY + 50f)
                lineTo(centerX, centerY - 15f)
                lineTo(centerX + 30f, centerY + 50f)
                close()
            }
            drawPath(cone, NeonPink)
            drawCircle(GoldCoin, radius = 8f, center = Offset(centerX, centerY - 15f))
        }
        "hat_crown" -> {
            // Golden crown with jewels
            val crown = Path().apply {
                moveTo(centerX - 42f, centerY + 45f)
                lineTo(centerX - 42f, centerY + 10f)
                lineTo(centerX - 20f, centerY + 25f)
                lineTo(centerX, centerY - 5f)
                lineTo(centerX + 20f, centerY + 25f)
                lineTo(centerX + 42f, centerY + 10f)
                lineTo(centerX + 42f, centerY + 45f)
                close()
            }
            drawPath(crown, GoldCoin)
            drawCircle(Color(0xFFEF4444), radius = 5f, center = Offset(centerX - 42f, centerY + 10f))
            drawCircle(NeonMint, radius = 6f, center = Offset(centerX, centerY - 5f))
            drawCircle(Color(0xFFEF4444), radius = 5f, center = Offset(centerX + 42f, centerY + 10f))
        }
        "hat_wizard" -> {
            // Wizard Hat
            val wizardHat = Path().apply {
                moveTo(centerX - 50f, centerY + 45f)
                lineTo(centerX, centerY - 35f)
                lineTo(centerX + 50f, centerY + 45f)
                close()
            }
            drawPath(wizardHat, Color(0xFF4C1D95))
            drawOval(Color(0xFF31105E), Offset(centerX - 60f, centerY + 40f), Size(120f, 16f))
            drawCircle(GoldCoin, radius = 5f, center = Offset(centerX - 10f, centerY + 15f))
        }
        "hat_detective" -> {
            drawOval(Color(0xFF78350F), Offset(centerX - 55f, centerY + 30f), Size(110f, 26f))
            drawRoundRect(Color(0xFF92400E), Offset(centerX - 38f, centerY + 10f), Size(76f, 32f), CornerRadius(16f, 16f))
        }
        "hat_chef" -> {
            drawCircle(Color.White, radius = 22f, center = Offset(centerX - 20f, centerY + 15f))
            drawCircle(Color.White, radius = 26f, center = Offset(centerX, centerY + 8f))
            drawCircle(Color.White, radius = 22f, center = Offset(centerX + 20f, centerY + 15f))
            drawRect(Color(0xFFF1F5F9), Offset(centerX - 35f, centerY + 30f), Size(70f, 18f))
        }
    }
}

fun DrawScope.drawAngelWings(centerX: Float, centerY: Float) {
    // Left Wing
    val leftWing = Path().apply {
        moveTo(centerX - 50f, centerY + 20f)
        cubicTo(centerX - 120f, centerY - 60f, centerX - 140f, centerY + 40f, centerX - 60f, centerY + 70f)
        close()
    }
    drawPath(leftWing, Color(0xCCFFFFFF))
    drawPath(leftWing, Color(0x66A78BFA), style = Stroke(width = 3f))

    // Right Wing
    val rightWing = Path().apply {
        moveTo(centerX + 50f, centerY + 20f)
        cubicTo(centerX + 120f, centerY - 60f, centerX + 140f, centerY + 40f, centerX + 60f, centerY + 70f)
        close()
    }
    drawPath(rightWing, Color(0xCCFFFFFF))
    drawPath(rightWing, Color(0x66A78BFA), style = Stroke(width = 3f))
}

fun DrawScope.drawHalo(centerX: Float, centerY: Float) {
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(GoldCoin, Color(0xFFFFBE0B)),
            center = Offset(centerX, centerY)
        ),
        topLeft = Offset(centerX - 42f, centerY),
        size = Size(84f, 22f),
        style = Stroke(width = 7f)
    )
}

fun DrawScope.drawSleepingZzz(x: Float, y: Float, bob: Float) {
    // Draw decorative Zzz
    drawCircle(Color(0x80A78BFA), radius = 6f, center = Offset(x - 12f, y + 10f + bob * 0.4f))
    drawCircle(Color(0xB3A78BFA), radius = 9f, center = Offset(x, y - 6f + bob * 0.6f))
    drawCircle(Color(0xFFDDD6FE), radius = 13f, center = Offset(x + 18f, y - 26f + bob))
}

fun DrawScope.drawHeart(x: Float, y: Float, size: Float, color: Color) {
    val heart = Path().apply {
        moveTo(x, y)
        cubicTo(x - size, y - size, x - size * 1.5f, y + size * 0.5f, x, y + size * 1.5f)
        cubicTo(x + size * 1.5f, y + size * 0.5f, x + size, y - size, x, y)
        close()
    }
    drawPath(heart, color)
}

fun DrawScope.drawBubble(x: Float, y: Float, radius: Float, color: Color) {
    drawCircle(color.copy(alpha = color.alpha * 0.6f), radius = radius, center = Offset(x, y))
    drawCircle(Color.White.copy(alpha = color.alpha * 0.8f), radius = radius * 0.28f, center = Offset(x - radius * 0.35f, y - radius * 0.35f))
    drawCircle(color, radius = radius, center = Offset(x, y), style = Stroke(width = 2.5f))
}

fun DrawScope.drawStarParticle(x: Float, y: Float, radius: Float, color: Color) {
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
