package com.example.ui.pet2d

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.PetType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import kotlin.math.sin

object Room2DDecor {

    fun drawCozyRoom(
        drawScope: DrawScope,
        w: Float,
        h: Float,
        isSleeping: Boolean,
        ambientTime: Float,
        petType: PetType
    ) {
        with(drawScope) {
            val floorY = h * 0.70f

            // 1. Room Wall Gradient
            val wallTop = if (isSleeping) Color(0xFF100724) else Color(0xFF241348)
            val wallBottom = if (isSleeping) Color(0xFF0C051A) else Color(0xFF170C33)

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(wallTop, wallBottom),
                    startY = 0f,
                    endY = floorY
                ),
                size = Size(w, floorY)
            )

            // Cozy arched window on the left wall with stars outside
            drawWindow(w, h, floorY, ambientTime, isSleeping)

            // Warm hanging fairy lights across the ceiling
            drawFairyLights(w, ambientTime, isSleeping)

            // 2. Wooden Plank Floor with Soft Perspective
            val floorTop = if (isSleeping) Color(0xFF140B24) else Color(0xFF1F1238)
            val floorBottom = if (isSleeping) Color(0xFF0B0516) else Color(0xFF120824)

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(floorTop, floorBottom),
                    startY = floorY,
                    endY = h
                ),
                topLeft = Offset(0f, floorY),
                size = Size(w, h - floorY)
            )

            // Baseboard / Skirting board
            drawLine(
                color = Color(0xFF4C2A80),
                start = Offset(0f, floorY),
                end = Offset(w, floorY),
                strokeWidth = 3.5f
            )

            // Floor perspective planks
            for (i in 1..4) {
                val px = w * (i * 0.2f)
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(px, floorY),
                    end = Offset(px + (i - 2.5f) * 45f, h),
                    strokeWidth = 1.5f
                )
            }

            // 3. Decorative Elements in Room
            // Decorative Pet Bed / Cushion on the left
            drawPetBed(w, floorY, isSleeping)

            // Cute Potted Fairy Plant / Succulent on small stand on the right
            drawPottedPlant(w, floorY)

            // Small species-specific Toy
            drawPetToy(w, floorY, petType)

            // 4. Large Plush Woven Rug under the Pet
            val rugCenterY = h * 0.73f
            val rugWidth = w * 0.82f
            val rugHeight = 115f

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3F1D72), Color(0xFF28114E)),
                    center = Offset(w * 0.5f, rugCenterY),
                    radius = rugWidth * 0.5f
                ),
                topLeft = Offset(w * 0.5f - rugWidth / 2f, rugCenterY - rugHeight / 2f),
                size = Size(rugWidth, rugHeight)
            )
            // Glowing neon braided border trim
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = listOf(NeonPurple, NeonMint, NeonPink, NeonPurple)
                ),
                topLeft = Offset(w * 0.5f - (rugWidth * 0.94f) / 2f, rugCenterY - (rugHeight * 0.90f) / 2f),
                size = Size(rugWidth * 0.94f, rugHeight * 0.90f),
                style = Stroke(width = 3.5f)
            )

            // 5. Soft Ground Contact Shadow (directly under the pet)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x99000000), Color(0x40000000), Color(0x00000000)),
                    center = Offset(w * 0.5f, rugCenterY + 12f),
                    radius = 110f
                ),
                topLeft = Offset(w * 0.5f - 110f, rugCenterY - 14f),
                size = Size(220f, 52f)
            )

            // 6. Floating Ambient Sparkle Dust Motes
            if (!isSleeping) {
                for (i in 0..5) {
                    val mx = w * (0.15f + i * 0.14f) + sin(ambientTime + i * 1.5f) * 16f
                    val my = h * (0.25f + (i % 3) * 0.15f) + sin(ambientTime * 1.2f + i) * 12f
                    val alpha = (0.35f + sin(ambientTime * 2f + i) * 0.25f).coerceIn(0.1f, 0.7f)
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = 2.5f,
                        center = Offset(mx, my)
                    )
                }
            }
        }
    }

    private fun DrawScope.drawWindow(w: Float, h: Float, floorY: Float, time: Float, isSleeping: Boolean) {
        val winX = w * 0.08f
        val winY = h * 0.10f
        val winW = w * 0.28f
        val winH = h * 0.32f

        // Outside night sky
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF0F0824), Color(0xFF1A0B3B))
            ),
            topLeft = Offset(winX, winY),
            size = Size(winW, winH),
            cornerRadius = CornerRadius(winW / 2f, winW / 2f)
        )

        // Moon
        drawCircle(
            color = Color(0xFFFEF08A),
            radius = 18f,
            center = Offset(winX + winW * 0.65f, winY + winH * 0.30f)
        )
        // Stars in window
        drawCircle(Color.White, radius = 2f, center = Offset(winX + winW * 0.25f, winY + winH * 0.25f))
        drawCircle(Color.White, radius = 2.5f, center = Offset(winX + winW * 0.40f, winY + winH * 0.55f))
        drawCircle(Color.White, radius = 1.8f, center = Offset(winX + winW * 0.75f, winY + winH * 0.65f))

        // Window Frame
        drawRoundRect(
            color = Color(0xFF4C2A80),
            topLeft = Offset(winX, winY),
            size = Size(winW, winH),
            cornerRadius = CornerRadius(winW / 2f, winW / 2f),
            style = Stroke(width = 3.5f)
        )
        // Window Crossbars
        drawLine(Color(0xFF4C2A80), Offset(winX + winW / 2f, winY), Offset(winX + winW / 2f, winY + winH), strokeWidth = 2.5f)
        drawLine(Color(0xFF4C2A80), Offset(winX, winY + winH * 0.55f), Offset(winX + winW, winY + winH * 0.55f), strokeWidth = 2.5f)
    }

    private fun DrawScope.drawFairyLights(w: Float, time: Float, isSleeping: Boolean) {
        val lightY = 18f
        val wire = Path().apply {
            moveTo(0f, lightY)
            quadraticTo(w * 0.25f, lightY + 22f, w * 0.5f, lightY + 12f)
            quadraticTo(w * 0.75f, lightY + 24f, w, lightY)
        }
        drawPath(wire, Color(0x44FFFFFF), style = Stroke(width = 1.5f))

        val colors = listOf(GoldCoin, NeonMint, NeonPink, Color(0xFFC084FC), GoldCoin, NeonMint)
        for (i in 0 until colors.size) {
            val lx = w * (0.12f + i * 0.15f)
            val ly = lightY + 14f + sin(i * 1.3f) * 6f
            val glowAlpha = if (isSleeping) 0.35f else (0.6f + sin(time * 3f + i) * 0.35f).coerceIn(0.2f, 1f)
            // Glow
            drawCircle(colors[i].copy(alpha = glowAlpha * 0.4f), radius = 10f, center = Offset(lx, ly))
            // Bulb
            drawCircle(colors[i].copy(alpha = glowAlpha), radius = 4.5f, center = Offset(lx, ly))
        }
    }

    private fun DrawScope.drawPetBed(w: Float, floorY: Float, isSleeping: Boolean) {
        val bedX = w * 0.05f
        val bedY = floorY - 26f
        val bedW = w * 0.24f
        val bedH = 46f

        // Bed Cushion Outer
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF7C3AED), Color(0xFF5B21B6))),
            topLeft = Offset(bedX, bedY),
            size = Size(bedW, bedH),
            cornerRadius = CornerRadius(22f, 22f)
        )
        // Inner plush rim
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE))),
            topLeft = Offset(bedX + 6f, bedY + 5f),
            size = Size(bedW - 12f, bedH - 14f),
            cornerRadius = CornerRadius(16f, 16f)
        )
    }

    private fun DrawScope.drawPottedPlant(w: Float, floorY: Float) {
        val potX = w * 0.84f
        val potY = floorY - 22f

        // Pot Stand
        drawRoundRect(
            color = Color(0xFF6D28D9),
            topLeft = Offset(potX, potY),
            size = Size(40f, 28f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Little green leaves
        val leaf1 = Path().apply {
            moveTo(potX + 20f, potY)
            quadraticTo(potX + 5f, potY - 26f, potX + 8f, potY - 32f)
            quadraticTo(potX + 22f, potY - 18f, potX + 20f, potY)
        }
        drawPath(leaf1, Color(0xFF10B981))

        val leaf2 = Path().apply {
            moveTo(potX + 20f, potY)
            quadraticTo(potX + 35f, potY - 24f, potX + 32f, potY - 30f)
            quadraticTo(potX + 18f, potY - 16f, potX + 20f, potY)
        }
        drawPath(leaf2, Color(0xFF34D399))
    }

    private fun DrawScope.drawPetToy(w: Float, floorY: Float, type: PetType) {
        val toyX = w * 0.78f
        val toyY = floorY + 16f

        when (type) {
            PetType.CAT -> {
                // Pink Yarn Ball
                drawCircle(NeonPink, radius = 12f, center = Offset(toyX, toyY))
                drawCircle(Color.White.copy(alpha = 0.4f), radius = 4f, center = Offset(toyX - 3f, toyY - 3f))
                // Stray yarn thread
                val thread = Path().apply {
                    moveTo(toyX, toyY)
                    quadraticTo(toyX - 12f, toyY + 8f, toyX - 22f, toyY + 5f)
                }
                drawPath(thread, NeonPink, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            }
            PetType.DOG -> {
                // Cheerful Bone
                drawRoundRect(
                    color = Color(0xFFFFFBEB),
                    topLeft = Offset(toyX - 14f, toyY - 4f),
                    size = Size(28f, 8f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawCircle(Color(0xFFFFFBEB), radius = 5.5f, center = Offset(toyX - 14f, toyY - 3f))
                drawCircle(Color(0xFFFFFBEB), radius = 5.5f, center = Offset(toyX - 14f, toyY + 3f))
                drawCircle(Color(0xFFFFFBEB), radius = 5.5f, center = Offset(toyX + 14f, toyY - 3f))
                drawCircle(Color(0xFFFFFBEB), radius = 5.5f, center = Offset(toyX + 14f, toyY + 3f))
            }
            PetType.FOX -> {
                // Shiny Bouncy Acorn / Gem
                drawOval(GoldCoin, Offset(toyX - 8f, toyY - 10f), Size(16f, 20f))
                drawRoundRect(Color(0xFF78350F), Offset(toyX - 9f, toyY - 13f), Size(18f, 7f), CornerRadius(3f, 3f))
            }
        }
    }
}
