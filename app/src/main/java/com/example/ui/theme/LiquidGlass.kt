package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass Theme Design System Tokens & Components
 * High-fidelity glassmorphic surfaces, fluid refraction, specular highlights, and ambient illumination.
 */
object LiquidGlassTokens {
    // Glass Surface Fills (Frosted translucent layers)
    val GlassSurfaceBase = Color(0x38130B2A)
    val GlassSurfaceCard = Color(0x421D1240)
    val GlassSurfaceElevated = Color(0x52291958)
    val GlassSurfaceHighlight = Color(0x28FFFFFF)
    
    // Glass Borders (Directional light catching edges)
    val SpecularRimTop = Color(0x80FFFFFF)
    val SpecularRimMid = Color(0x25FFFFFF)
    val SpecularRimSubtle = Color(0x10FFFFFF)
    
    // Ambient liquid glowing accents
    val LiquidCyan = Color(0xFF00F5D4)
    val LiquidElectricViolet = Color(0xFFA78BFA)
    val LiquidDeepPurple = Color(0xFF7C3AED)
    val LiquidPink = Color(0xFFFF2A85)
    val LiquidAmber = Color(0xFFFFB703)
    val LiquidBlue = Color(0xFF38BDF8)
    val LiquidEmerald = Color(0xFF10B981)

    // Liquid Glass border brush (Simulates specular refraction edge)
    fun glassBorderBrush(accentColor: Color = LiquidElectricViolet): Brush {
        return Brush.linearGradient(
            0.0f to SpecularRimTop,
            0.3f to SpecularRimMid,
            0.7f to accentColor.copy(alpha = 0.40f),
            1.0f to SpecularRimSubtle,
            start = Offset(0f, 0f),
            end = Offset(400f, 600f)
        )
    }

    // Glass panel fill brush with directional depth
    fun glassFillBrush(tintColor: Color = Color.Transparent, alpha: Float = 0.5f): Brush {
        return Brush.verticalGradient(
            0.0f to Color(0x50281850).copy(alpha = (0.35f * alpha) + 0.15f),
            0.4f to Color(0x381B1038).copy(alpha = (0.28f * alpha) + 0.12f),
            1.0f to (if (tintColor != Color.Transparent) tintColor.copy(alpha = 0.18f) else Color(0x22130928))
        )
    }
}

/**
 * Ambient Liquid Mesh Backdrop
 * Displays slowly shifting luminous liquid orbs beneath content,
 * giving authentic refraction through frosted glass containers.
 */
@Composable
fun AmbientLiquidBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquidAmbient")

    val orb1Offset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1Offset"
    )

    val orb2Offset by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -70f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2Offset"
    )

    val orb3Scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb3Scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Liquid Orb 1 (Top-Right: Deep Electric Violet)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x558B5CF6),
                            Color(0x306D28D9),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * 0.85f + orb1Offset, canvasHeight * 0.18f + orb1Offset * 0.5f),
                        radius = canvasWidth * 0.7f
                    )
                )

                // Liquid Orb 2 (Mid-Left: Luminous Cyan/Mint)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x4000F5D4),
                            Color(0x200EA5E9),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * 0.12f + orb2Offset * 0.6f, canvasHeight * 0.55f + orb2Offset),
                        radius = canvasWidth * 0.65f
                    )
                )

                // Liquid Orb 3 (Bottom-Center: Vivid Magenta)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x38FF2A85),
                            Color(0x189333EA),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * 0.55f, canvasHeight * 0.88f),
                        radius = (canvasWidth * 0.75f) * orb3Scale
                    )
                )

                // Liquid Ambient Sheen Overlay (Soft diagonal refraction)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x12FFFFFF),
                            Color(0x04000000),
                            Color(0x0C8B5CF6)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(canvasWidth, canvasHeight)
                    )
                )
            }
    ) {
        content()
    }
}

/**
 * Liquid Glass Card Container
 * Features a frosted glass surface, specular rim border, and curved refraction gloss.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    accentBorderColor: Color = LiquidGlassTokens.LiquidElectricViolet,
    tintColor: Color = Color.Transparent,
    borderWidth: Dp = 1.2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .then(clickableModifier)
            .background(
                brush = LiquidGlassTokens.glassFillBrush(tintColor = tintColor)
            )
            .border(
                BorderStroke(
                    borderWidth,
                    LiquidGlassTokens.glassBorderBrush(accentBorderColor)
                ),
                shape = shape
            )
            .drawWithContent {
                drawContent()
                // Specular Glass Gloss line on top edge
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x30FFFFFF),
                            Color(0x08FFFFFF),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 40.dp.toPx()
                    ),
                    blendMode = BlendMode.Screen
                )
            }
    ) {
        content()
    }
}

/**
 * Liquid Glass Capsule / Pill
 * Ideal for stats, badges, indicators, and small tags.
 */
@Composable
fun LiquidGlassCapsule(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    accentBorderColor: Color = Color.White.copy(alpha = 0.25f),
    backgroundColor: Color = LiquidGlassTokens.GlassSurfaceElevated,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .then(clickableModifier)
            .background(
                Brush.verticalGradient(
                    listOf(
                        backgroundColor.copy(alpha = 0.75f),
                        backgroundColor.copy(alpha = 0.50f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0x60FFFFFF),
                            accentBorderColor,
                            Color(0x15FFFFFF)
                        )
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Liquid Glass Luminous Progress Bar
 * Simulates a glowing fluid capsule with liquid bubbles & gloss reflections.
 */
@Composable
fun LiquidProgressBar(
    progress: Float, // 0f .. 1f
    liquidColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    showGlow: Boolean = true
) {
    val clampedProgress = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(Color(0x4013092E))
            .border(
                BorderStroke(1.dp, Color(0x30FFFFFF)),
                shape = RoundedCornerShape(height / 2)
            )
    ) {
        if (clampedProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clampedProgress)
                    .height(height)
                    .clip(RoundedCornerShape(height / 2))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                liquidColor.copy(alpha = 0.85f),
                                liquidColor,
                                liquidColor.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .drawWithContent {
                        drawContent()
                        // Liquid top gloss reflection
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0x60FFFFFF),
                                    Color(0x10FFFFFF),
                                    Color.Transparent
                                )
                            )
                        )
                    }
            )
        }
    }
}
