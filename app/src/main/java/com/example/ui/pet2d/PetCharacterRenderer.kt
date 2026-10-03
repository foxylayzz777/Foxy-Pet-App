package com.example.ui.pet2d

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.PetEntity
import com.example.data.model.PetType
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object PetCharacterRenderer {

    fun drawPet(
        drawScope: DrawScope,
        pet: PetEntity,
        centerX: Float,
        centerY: Float,
        animTime: Float,
        lookTargetX: Float,
        lookTargetY: Float,
        isBlinking: Boolean,
        isEating: Boolean,
        isBathActive: Boolean,
        isLoving: Boolean,
        isGiftActive: Boolean = false,
        isLevelUpActive: Boolean = false,
        isSurprised: Boolean = false,
        scale: Float = 1.35f
    ) {
        with(drawScope) {
            val palette = Pet2DPalettes.getPalette(pet.type, pet.skinId)
            val expression = Pet2DPalettes.resolveExpression(
                pet = pet,
                isEating = isEating,
                isBathActive = isBathActive,
                isLoving = isLoving,
                isGiftActive = isGiftActive,
                isLevelUpActive = isLevelUpActive,
                isSurprised = isSurprised
            )

            // -----------------------------------------------------------------
            // ANIMATION SYSTEM: Breathing, Bouncing, Tail wagging, Ear twitches
            // -----------------------------------------------------------------
            val isExcitedOrCelebrating = expression == PetExpression.EXCITED || isGiftActive || isLevelUpActive
            val breathFreq = if (pet.isSleeping) 1.6f else if (isExcitedOrCelebrating) 4.5f else 2.8f
            val breathCycle = sin(animTime * breathFreq)
            val breathOffset = breathCycle * 3.5f * scale

            // Celebration / Interaction Hop
            val hopOffset = if (isExcitedOrCelebrating) {
                -abs(sin(animTime * 7.5f)) * 18f * scale
            } else if (isLoving) {
                -abs(sin(animTime * 5.0f)) * 8f * scale
            } else 0f

            // Squash & Stretch
            val squashScaleY = if (isExcitedOrCelebrating) {
                1f + sin(animTime * 7.5f) * 0.08f
            } else 1f

            val basePetX = centerX + lookTargetX * 8f
            val basePetY = centerY + breathOffset + hopOffset

            val headBob = breathOffset * 0.65f
            val headTilt = (sin(animTime * 1.6f) * 2.5f + lookTargetX * 10f)

            // Tail motion physics
            val tailSpeed = when (pet.type) {
                PetType.DOG -> if (isExcitedOrCelebrating || isLoving) 12f else 7.5f
                PetType.FOX -> if (isExcitedOrCelebrating) 6.5f else 3.8f
                PetType.CAT -> if (isLoving) 5f else 2.6f
            }
            val tailAmp = when (expression) {
                PetExpression.EXCITED, PetExpression.LOVED -> 28f
                PetExpression.VERY_HAPPY, PetExpression.HAPPY -> 20f
                PetExpression.SLEEPY -> 7f
                PetExpression.SAD -> 5f
                else -> 15f
            }
            val tailAngle = sin(animTime * tailSpeed) * tailAmp

            // Ear micro-twitching
            val earTwitch = if (pet.isSleeping) 0f else sin(animTime * 4.5f) * 4.5f

            // -----------------------------------------------------------------
            // LAYER 1: BACK ACCESSORIES (Wings, Halo)
            // -----------------------------------------------------------------
            if (pet.selectedAccessory == "acc_wings") {
                drawAngelWings(basePetX, basePetY - 14f * scale, scale, animTime)
            }
            if (pet.selectedAccessory == "acc_halo") {
                drawFloatingHalo(basePetX, basePetY - 138f * scale + headBob, scale, animTime)
            }

            // -----------------------------------------------------------------
            // LAYER 2: SPECIES TAIL (FOX fluffy plume, CAT curved, DOG wagging)
            // -----------------------------------------------------------------
            drawSpeciesTail(
                type = pet.type,
                palette = palette,
                x = basePetX,
                y = basePetY,
                angle = tailAngle,
                scale = scale,
                isSleeping = pet.isSleeping,
                time = animTime
            )

            // -----------------------------------------------------------------
            // LAYER 3: HIND FEET / SITTING BASE PAWS
            // -----------------------------------------------------------------
            drawHindFeet(palette, basePetX, basePetY + 44f * scale, scale)

            // -----------------------------------------------------------------
            // LAYER 4: BODY / TORSO & CHEST FLUFF BIB & OUTFITS
            // -----------------------------------------------------------------
            drawPetBody(
                palette = palette,
                x = basePetX,
                y = basePetY,
                scale = scale * squashScaleY,
                clothesId = pet.selectedClothes,
                breathCycle = breathCycle
            )

            // -----------------------------------------------------------------
            // LAYER 5: COLLAR / NECKLACE ACCESSORY
            // -----------------------------------------------------------------
            if (pet.selectedCollar != null) {
                drawPetCollar(basePetX, basePetY - 18f * scale + headBob * 0.5f, scale, pet.selectedCollar)
            }

            // -----------------------------------------------------------------
            // LAYER 6: HEAD, FLUFFY CHEEKS & EARS
            // -----------------------------------------------------------------
            val headX = basePetX + lookTargetX * 12f
            val headY = basePetY - 70f * scale + headBob

            // Draw Ears behind head
            drawPetEars(
                type = pet.type,
                palette = palette,
                x = headX,
                y = headY,
                scale = scale,
                expression = expression,
                earTwitch = earTwitch,
                headTilt = headTilt
            )

            // Draw Head Silhouette with soft rounded chibi cheeks
            drawPetHead(
                palette = palette,
                x = headX,
                y = headY,
                scale = scale
            )

            // -----------------------------------------------------------------
            // LAYER 7: COMPLETE EXPRESSIVE FACE SYSTEM
            // -----------------------------------------------------------------
            drawPetFace(
                pet = pet,
                palette = palette,
                headX = headX,
                headY = headY,
                scale = scale,
                expression = expression,
                isBlinking = isBlinking,
                isEating = isEating,
                lookTargetX = lookTargetX,
                lookTargetY = lookTargetY,
                animTime = animTime
            )

            // -----------------------------------------------------------------
            // LAYER 8: HEAD ACCESSORIES (Glasses, Hats)
            // -----------------------------------------------------------------
            if (pet.selectedGlasses != null) {
                drawPetGlasses(headX, headY - 4f * scale, scale, pet.selectedGlasses)
            }
            if (pet.selectedHat != null) {
                drawPetHat(headX, headY - 56f * scale, scale, pet.selectedHat, headTilt)
            }

            // -----------------------------------------------------------------
            // LAYER 9: FRONT PAWS (Animated holding snack, waving, or resting)
            // -----------------------------------------------------------------
            drawFrontPaws(
                palette = palette,
                x = basePetX,
                y = basePetY + 44f * scale,
                scale = scale,
                isEating = isEating,
                isBathActive = isBathActive,
                isExcited = isExcitedOrCelebrating,
                time = animTime
            )
        }
    }

    // =========================================================================
    // TAIL RENDERERS (Species Specific)
    // =========================================================================
    private fun DrawScope.drawSpeciesTail(
        type: PetType,
        palette: PetPalette,
        x: Float,
        y: Float,
        angle: Float,
        scale: Float,
        isSleeping: Boolean,
        time: Float
    ) {
        when (type) {
            PetType.FOX -> {
                // MAGNIFICENT PLUSH FOX TAIL with gorgeous sweeping curves and cloud scallops tip
                val tailBaseX = x + 34f * scale
                val tailBaseY = y + 26f * scale
                val sway = angle * 1.8f

                val tailPath = Path().apply {
                    moveTo(tailBaseX, tailBaseY)
                    // Sweeping outer luscious fox curve
                    cubicTo(
                        tailBaseX + 70f * scale, tailBaseY - 25f * scale + sway * 0.4f,
                        tailBaseX + 120f * scale, tailBaseY - 105f * scale + sway * 0.8f,
                        tailBaseX + 68f * scale, tailBaseY - 152f * scale + sway
                    )
                    // Fluffy rounded tip arc
                    cubicTo(
                        tailBaseX + 46f * scale, tailBaseY - 168f * scale + sway,
                        tailBaseX + 22f * scale, tailBaseY - 146f * scale + sway * 0.9f,
                        tailBaseX + 32f * scale, tailBaseY - 114f * scale + sway * 0.7f
                    )
                    // Inner curve back to body
                    cubicTo(
                        tailBaseX + 44f * scale, tailBaseY - 78f * scale + sway * 0.4f,
                        tailBaseX + 18f * scale, tailBaseY - 18f * scale,
                        tailBaseX, tailBaseY
                    )
                    close()
                }

                // Shaded Fox Fur Gradient
                drawPath(
                    path = tailPath,
                    brush = Brush.linearGradient(
                        colors = listOf(palette.tailHighlight, palette.tailBase, palette.furShadow),
                        start = Offset(tailBaseX, tailBaseY - 150f * scale + sway),
                        end = Offset(tailBaseX + 90f * scale, tailBaseY)
                    )
                )

                // Soft Outer Fur Glow / Ambient Rim
                drawPath(
                    path = tailPath,
                    color = palette.furRimGlow.copy(alpha = 0.25f),
                    style = Stroke(width = 2.5f * scale)
                )

                // Fluffy Cloud Tip (Cream/White Scalloped Fox Tip)
                val tipX = tailBaseX + 68f * scale
                val tipY = tailBaseY - 152f * scale + sway
                val tipPath = Path().apply {
                    moveTo(tipX, tipY)
                    cubicTo(
                        tipX - 18f * scale, tipY - 14f * scale,
                        tipX - 38f * scale, tipY + 6f * scale,
                        tipX - 32f * scale, tipY + 34f * scale
                    )
                    // Fluffy scallops
                    cubicTo(
                        tipX - 16f * scale, tipY + 44f * scale,
                        tipX + 8f * scale, tipY + 32f * scale,
                        tipX + 26f * scale, tipY + 16f * scale
                    )
                    close()
                }
                drawPath(tipPath, palette.tailTip)

                // Soft scallop fur tuft marks on tip
                drawCircle(
                    color = palette.tailHighlight.copy(alpha = 0.4f),
                    radius = 5f * scale,
                    center = Offset(tipX - 12f * scale, tipY + 22f * scale)
                )
            }

            PetType.CAT -> {
                // Sleek, velvety curved cat tail with smooth gentle wave
                val startX = x + 38f * scale
                val startY = y + 38f * scale
                val tailPath = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(
                        startX + 40f * scale, startY - 18f * scale + angle * 0.4f,
                        startX + 75f * scale, startY - 75f * scale + angle * 0.9f,
                        startX + 48f * scale, startY - 125f * scale + angle * 1.5f
                    )
                }
                // Cat tail body stroke
                drawPath(
                    path = tailPath,
                    color = palette.tailBase,
                    style = Stroke(width = 20f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Highlight sheen along cat tail
                drawPath(
                    path = tailPath,
                    color = palette.tailHighlight.copy(alpha = 0.5f),
                    style = Stroke(width = 8f * scale, cap = StrokeCap.Round)
                )
                // White rounded cat tail tip
                drawCircle(
                    color = palette.tailTip,
                    radius = 10f * scale,
                    center = Offset(startX + 48f * scale, startY - 125f * scale + angle * 1.5f)
                )
            }

            PetType.DOG -> {
                // Joyful, perky puppy tail with energetic wag
                val tailX = x + 34f * scale
                val tailY = y + 22f * scale
                val wagX = angle * 0.9f
                val wagY = abs(angle) * 0.25f

                // Curved dog plume
                val dogTail = Path().apply {
                    moveTo(tailX, tailY)
                    cubicTo(
                        tailX + 22f * scale + wagX, tailY - 20f * scale - wagY,
                        tailX + 44f * scale + wagX, tailY - 58f * scale - wagY,
                        tailX + 18f * scale + wagX, tailY - 74f * scale
                    )
                    cubicTo(
                        tailX + 6f * scale + wagX, tailY - 50f * scale,
                        tailX + 12f * scale, tailY - 15f * scale,
                        tailX, tailY
                    )
                    close()
                }
                drawPath(
                    dogTail,
                    brush = Brush.verticalGradient(
                        listOf(palette.tailHighlight, palette.tailBase)
                    )
                )
                // White fluffy tip
                drawCircle(
                    color = palette.tailTip,
                    radius = 12f * scale,
                    center = Offset(tailX + 18f * scale + wagX, tailY - 74f * scale)
                )
            }
        }
    }

    // =========================================================================
    // HIND FEET
    // =========================================================================
    private fun DrawScope.drawHindFeet(
        palette: PetPalette,
        x: Float,
        y: Float,
        scale: Float
    ) {
        val footW = 34f * scale
        val footH = 22f * scale

        // Left Hind Foot
        drawOval(
            brush = Brush.verticalGradient(listOf(palette.muzzleBase, palette.muzzleShadow)),
            topLeft = Offset(x - 52f * scale, y),
            size = Size(footW, footH)
        )
        // Cute little pink toe pad
        drawCircle(palette.pawPadColor.copy(alpha = 0.5f), radius = 4f * scale, center = Offset(x - 36f * scale, y + 10f * scale))

        // Right Hind Foot
        drawOval(
            brush = Brush.verticalGradient(listOf(palette.muzzleBase, palette.muzzleShadow)),
            topLeft = Offset(x + 18f * scale, y),
            size = Size(footW, footH)
        )
        drawCircle(palette.pawPadColor.copy(alpha = 0.5f), radius = 4f * scale, center = Offset(x + 36f * scale, y + 10f * scale))
    }

    // =========================================================================
    // BODY & CHEST FLUFF BIB & OUTFITS
    // =========================================================================
    private fun DrawScope.drawPetBody(
        palette: PetPalette,
        x: Float,
        y: Float,
        scale: Float,
        clothesId: String?,
        breathCycle: Float
    ) {
        val bodyW = 98f * scale + (breathCycle * 1.5f)
        val bodyH = 86f * scale

        val bodyBrush = when (clothesId) {
            "clothes_sweater" -> Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309)))
            "clothes_hoodie" -> Brush.verticalGradient(listOf(NeonPurple, Color(0xFF6D28D9), Color(0xFF4C1D95)))
            "clothes_cape" -> Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFF991B1B)))
            "clothes_tux" -> Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)))
            else -> Brush.verticalGradient(listOf(palette.furHighlight, palette.furBase, palette.furShadow))
        }

        // Main Soft Chubby Torso
        drawRoundRect(
            brush = bodyBrush,
            topLeft = Offset(x - bodyW / 2f, y - 8f * scale),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(44f * scale, 42f * scale)
        )

        // Torso Rim Glow / Soft Highlight
        drawRoundRect(
            color = palette.furRimGlow.copy(alpha = 0.2f),
            topLeft = Offset(x - bodyW / 2f, y - 8f * scale),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(44f * scale, 42f * scale),
            style = Stroke(width = 2.2f * scale)
        )

        // Cream Chest Fluff Bib (Natural plush markings)
        if (clothesId == null || clothesId == "clothes_cape") {
            val bibPath = Path().apply {
                val bibTop = y + 2f * scale
                moveTo(x - 26f * scale, bibTop)
                cubicTo(
                    x - 34f * scale, bibTop + 24f * scale,
                    x - 22f * scale, bibTop + 58f * scale,
                    x, bibTop + 62f * scale
                )
                cubicTo(
                    x + 22f * scale, bibTop + 58f * scale,
                    x + 34f * scale, bibTop + 24f * scale,
                    x + 26f * scale, bibTop
                )
                close()
            }
            drawPath(
                path = bibPath,
                brush = Brush.verticalGradient(listOf(palette.muzzleBase, palette.muzzleShadow))
            )
        } else if (clothesId == "clothes_tux") {
            // Tuxedo white shirt & stylish red bowtie
            drawOval(
                color = Color.White,
                topLeft = Offset(x - 20f * scale, y + 2f * scale),
                size = Size(40f * scale, 46f * scale)
            )
            // Bowtie
            val bowtiePath = Path().apply {
                val bx = x
                val by = y + 8f * scale
                moveTo(bx, by)
                lineTo(bx - 14f * scale, by - 8f * scale)
                lineTo(bx - 14f * scale, by + 8f * scale)
                lineTo(bx, by)
                lineTo(bx + 14f * scale, by - 8f * scale)
                lineTo(bx + 14f * scale, by + 8f * scale)
                close()
            }
            drawPath(bowtiePath, Color(0xFFDC2626))
            drawCircle(Color(0xFFDC2626), radius = 4f * scale, center = Offset(x, y + 8f * scale))
        }
    }

    // =========================================================================
    // HEAD & CHEEKS
    // =========================================================================
    private fun DrawScope.drawPetHead(
        palette: PetPalette,
        x: Float,
        y: Float,
        scale: Float
    ) {
        val headW = 114f * scale
        val headH = 92f * scale

        // Main Head - Soft mochi shape (plush rounded cheeks, wider at bottom)
        val headPath = Path().apply {
            moveTo(x - 42f * scale, y - 44f * scale)
            // Top crown
            cubicTo(
                x - 20f * scale, y - 48f * scale,
                x + 20f * scale, y - 48f * scale,
                x + 42f * scale, y - 44f * scale
            )
            // Right cheek curve (chubby and fluffy)
            cubicTo(
                x + 58f * scale, y - 14f * scale,
                x + 64f * scale, y + 24f * scale,
                x + 36f * scale, y + 44f * scale
            )
            // Chin
            cubicTo(
                x + 18f * scale, y + 48f * scale,
                x - 18f * scale, y + 48f * scale,
                x - 36f * scale, y + 44f * scale
            )
            // Left cheek curve
            cubicTo(
                x - 64f * scale, y + 24f * scale,
                x - 58f * scale, y - 14f * scale,
                x - 42f * scale, y - 44f * scale
            )
            close()
        }

        // Shaded Head Gradient
        drawPath(
            path = headPath,
            brush = Brush.verticalGradient(
                colors = listOf(palette.furHighlight, palette.furBase, palette.furShadow),
                startY = y - 48f * scale,
                endY = y + 48f * scale
            )
        )

        // Soft outer rim light on head
        drawPath(
            path = headPath,
            color = palette.furRimGlow.copy(alpha = 0.28f),
            style = Stroke(width = 2.4f * scale)
        )

        // Cheek fluff tufts (3-point plush feather tufts)
        // Left cheek tufts
        val leftCheek = Path().apply {
            moveTo(x - 46f * scale, y + 6f * scale)
            lineTo(x - 64f * scale, y + 16f * scale)
            lineTo(x - 48f * scale, y + 22f * scale)
            lineTo(x - 60f * scale, y + 30f * scale)
            lineTo(x - 42f * scale, y + 34f * scale)
            close()
        }
        drawPath(leftCheek, palette.furBase)

        // Right cheek tufts
        val rightCheek = Path().apply {
            moveTo(x + 46f * scale, y + 6f * scale)
            lineTo(x + 64f * scale, y + 16f * scale)
            lineTo(x + 48f * scale, y + 22f * scale)
            lineTo(x + 60f * scale, y + 30f * scale)
            lineTo(x + 42f * scale, y + 34f * scale)
            close()
        }
        drawPath(rightCheek, palette.furBase)
    }

    // =========================================================================
    // EARS (Species & Expression Sensitive)
    // =========================================================================
    private fun DrawScope.drawPetEars(
        type: PetType,
        palette: PetPalette,
        x: Float,
        y: Float,
        scale: Float,
        expression: PetExpression,
        earTwitch: Float,
        headTilt: Float
    ) {
        // Expression ear modifiers: Sad = drooping; Excited/Surprised = perked tall; Sleepy = relaxed
        val earDroop = when (expression) {
            PetExpression.SAD, PetExpression.HUNGRY -> 8f * scale
            PetExpression.SLEEPY -> 5f * scale
            PetExpression.EXCITED, PetExpression.SURPRISED -> -6f * scale
            else -> 0f
        }

        when (type) {
            PetType.FOX -> {
                // TALL PERKY FOX EARS with dark velvet tips and peach interior & fluffy ear tufts
                val leftTwitch = earTwitch + earDroop
                val rightTwitch = -earTwitch + earDroop

                // Left Ear Outer
                val leftEar = Path().apply {
                    moveTo(x - 52f * scale, y - 2f * scale)
                    cubicTo(
                        x - 70f * scale, y - 42f * scale,
                        x - 48f * scale, y - 96f * scale + leftTwitch,
                        x - 36f * scale, y - 92f * scale + leftTwitch
                    )
                    cubicTo(
                        x - 22f * scale, y - 68f * scale,
                        x - 12f * scale, y - 32f * scale,
                        x - 14f * scale, y - 12f * scale
                    )
                    close()
                }
                drawPath(leftEar, brush = Brush.verticalGradient(listOf(palette.furHighlight, palette.furBase)))

                // Dark Fox Ear Tip
                if (palette.earTip != null) {
                    val tip = Path().apply {
                        moveTo(x - 48f * scale, y - 82f * scale + leftTwitch)
                        lineTo(x - 36f * scale, y - 92f * scale + leftTwitch)
                        lineTo(x - 26f * scale, y - 76f * scale + leftTwitch)
                        close()
                    }
                    drawPath(tip, palette.earTip)
                }

                // Left Inner Ear Cup
                val leftInner = Path().apply {
                    moveTo(x - 46f * scale, y - 6f * scale)
                    lineTo(x - 38f * scale, y - 78f * scale + leftTwitch)
                    lineTo(x - 20f * scale, y - 18f * scale)
                    close()
                }
                drawPath(leftInner, palette.innerEar)

                // White fluffy ear tuft at base
                drawCircle(palette.earTuft, radius = 9f * scale, center = Offset(x - 42f * scale, y - 8f * scale))

                // Right Ear Outer
                val rightEar = Path().apply {
                    moveTo(x + 14f * scale, y - 12f * scale)
                    cubicTo(
                        x + 22f * scale, y - 32f * scale,
                        x + 22f * scale, y - 68f * scale,
                        x + 36f * scale, y - 92f * scale + rightTwitch
                    )
                    cubicTo(
                        x + 48f * scale, y - 96f * scale + rightTwitch,
                        x + 70f * scale, y - 42f * scale,
                        x + 52f * scale, y - 2f * scale
                    )
                    close()
                }
                drawPath(rightEar, brush = Brush.verticalGradient(listOf(palette.furHighlight, palette.furBase)))

                // Right Dark Fox Ear Tip
                if (palette.earTip != null) {
                    val rTip = Path().apply {
                        moveTo(x + 26f * scale, y - 76f * scale + rightTwitch)
                        lineTo(x + 36f * scale, y - 92f * scale + rightTwitch)
                        lineTo(x + 48f * scale, y - 82f * scale + rightTwitch)
                        close()
                    }
                    drawPath(rTip, palette.earTip)
                }

                // Right Inner Ear Cup
                val rightInner = Path().apply {
                    moveTo(x + 20f * scale, y - 18f * scale)
                    lineTo(x + 38f * scale, y - 78f * scale + rightTwitch)
                    lineTo(x + 46f * scale, y - 6f * scale)
                    close()
                }
                drawPath(rightInner, palette.innerEar)
                drawCircle(palette.earTuft, radius = 9f * scale, center = Offset(x + 42f * scale, y - 8f * scale))
            }

            PetType.CAT -> {
                // PERKY TRIANGULAR CAT EARS with soft pink fold & white fur tuft
                val leftEar = Path().apply {
                    moveTo(x - 48f * scale, y)
                    lineTo(x - 34f * scale, y - 72f * scale + earTwitch + earDroop)
                    lineTo(x - 12f * scale, y - 14f * scale)
                    close()
                }
                drawPath(leftEar, palette.furBase)
                val leftInner = Path().apply {
                    moveTo(x - 42f * scale, y - 4f * scale)
                    lineTo(x - 34f * scale, y - 60f * scale + earTwitch + earDroop)
                    lineTo(x - 18f * scale, y - 16f * scale)
                    close()
                }
                drawPath(leftInner, palette.innerEar)
                drawCircle(palette.earTuft, radius = 7f * scale, center = Offset(x - 38f * scale, y - 8f * scale))

                val rightEar = Path().apply {
                    moveTo(x + 12f * scale, y - 14f * scale)
                    lineTo(x + 34f * scale, y - 72f * scale - earTwitch + earDroop)
                    lineTo(x + 48f * scale, y)
                    close()
                }
                drawPath(rightEar, palette.furBase)
                val rightInner = Path().apply {
                    moveTo(x + 18f * scale, y - 16f * scale)
                    lineTo(x + 34f * scale, y - 60f * scale - earTwitch + earDroop)
                    lineTo(x + 42f * scale, y - 4f * scale)
                    close()
                }
                drawPath(rightInner, palette.innerEar)
                drawCircle(palette.earTuft, radius = 7f * scale, center = Offset(x + 38f * scale, y - 8f * scale))
            }

            PetType.DOG -> {
                // FLOPPY SOFT PUPPY EARS framing the face with gentle bounce
                val earBounce = earTwitch * 0.5f + earDroop
                drawOval(
                    brush = Brush.verticalGradient(listOf(palette.innerEar, palette.furShadow)),
                    topLeft = Offset(x - 64f * scale, y - 24f * scale + earBounce),
                    size = Size(28f * scale, 68f * scale)
                )
                drawOval(
                    brush = Brush.verticalGradient(listOf(palette.innerEar, palette.furShadow)),
                    topLeft = Offset(x + 36f * scale, y - 24f * scale - earBounce),
                    size = Size(28f * scale, 68f * scale)
                )
            }
        }
    }

    // =========================================================================
    // FACE & 9 EXPRESSIONS SYSTEM
    // =========================================================================
    private fun DrawScope.drawPetFace(
        pet: PetEntity,
        palette: PetPalette,
        headX: Float,
        headY: Float,
        scale: Float,
        expression: PetExpression,
        isBlinking: Boolean,
        isEating: Boolean,
        lookTargetX: Float,
        lookTargetY: Float,
        animTime: Float
    ) {
        // 1. Soft Muzzle Patch (Cream/White)
        val muzzleW = 76f * scale
        val muzzleH = 46f * scale
        val muzzleY = headY + 8f * scale

        drawOval(
            brush = Brush.verticalGradient(listOf(palette.muzzleBase, palette.muzzleShadow)),
            topLeft = Offset(headX - muzzleW / 2f, muzzleY),
            size = Size(muzzleW, muzzleH)
        )

        // 2. Soft Airbrushed Radiant Blush
        val blushIntensity = when (expression) {
            PetExpression.LOVED -> 0.75f
            PetExpression.VERY_HAPPY, PetExpression.EXCITED -> 0.60f
            PetExpression.HAPPY -> 0.45f
            else -> 0.35f
        }
        drawOval(
            color = palette.blushColor.copy(alpha = blushIntensity),
            topLeft = Offset(headX - 44f * scale, muzzleY + 4f * scale),
            size = Size(22f * scale, 13f * scale)
        )
        drawOval(
            color = palette.blushColor.copy(alpha = blushIntensity),
            topLeft = Offset(headX + 22f * scale, muzzleY + 4f * scale),
            size = Size(22f * scale, 13f * scale)
        )

        // Tiny cute blush sparkles for LOVED or EXCITED
        if (expression == PetExpression.LOVED || expression == PetExpression.EXCITED) {
            drawCircle(Color.White.copy(alpha = 0.8f), radius = 2f * scale, center = Offset(headX - 32f * scale, muzzleY + 10f * scale))
            drawCircle(Color.White.copy(alpha = 0.8f), radius = 2f * scale, center = Offset(headX + 32f * scale, muzzleY + 10f * scale))
        }

        // 3. Cute Button Nose
        val noseW = 12f * scale
        val noseH = 8.5f * scale
        drawOval(
            color = palette.noseColor,
            topLeft = Offset(headX - noseW / 2f, muzzleY + 12f * scale),
            size = Size(noseW, noseH)
        )
        // Specular glossy reflection dot on nose
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 2f * scale,
            center = Offset(headX - 2f * scale, muzzleY + 14f * scale)
        )

        // 4. Species Whiskers (Fox & Cat)
        if (pet.type == PetType.CAT || pet.type == PetType.FOX) {
            val wColor = palette.mouthColor.copy(alpha = 0.35f)
            // Left whiskers
            drawLine(wColor, Offset(headX - 24f * scale, muzzleY + 16f * scale), Offset(headX - 54f * scale, muzzleY + 13f * scale), strokeWidth = 1.8f * scale)
            drawLine(wColor, Offset(headX - 24f * scale, muzzleY + 23f * scale), Offset(headX - 52f * scale, muzzleY + 25f * scale), strokeWidth = 1.8f * scale)
            // Right whiskers
            drawLine(wColor, Offset(headX + 24f * scale, muzzleY + 16f * scale), Offset(headX + 54f * scale, muzzleY + 13f * scale), strokeWidth = 1.8f * scale)
            drawLine(wColor, Offset(headX + 24f * scale, muzzleY + 23f * scale), Offset(headX + 52f * scale, muzzleY + 25f * scale), strokeWidth = 1.8f * scale)
        }

        // 5. Mouth Rendering based on the 9 Expressions & Actions
        val mouthY = muzzleY + 22f * scale
        drawExpressionMouth(
            expression = expression,
            palette = palette,
            headX = headX,
            mouthY = mouthY,
            scale = scale,
            isEating = isEating,
            petType = pet.type,
            animTime = animTime
        )

        // 6. Eyes & Eyelids Rendering for 9 Expressions
        drawExpressionEyes(
            expression = expression,
            palette = palette,
            headX = headX,
            headY = headY,
            scale = scale,
            isBlinking = isBlinking || pet.isSleeping,
            lookTargetX = lookTargetX,
            lookTargetY = lookTargetY,
            animTime = animTime
        )
    }

    // =========================================================================
    // MOUTH EXPRESSIONS
    // =========================================================================
    private fun DrawScope.drawExpressionMouth(
        expression: PetExpression,
        palette: PetPalette,
        headX: Float,
        mouthY: Float,
        scale: Float,
        isEating: Boolean,
        petType: PetType,
        animTime: Float
    ) {
        val strokeWidth = 2.4f * scale

        if (isEating) {
            // Cheerful rhythmic chewing mouth (opens and closes)
            val chewHeight = (sin(animTime * 14f) * 4f + 8f).coerceAtLeast(4f) * scale
            drawOval(
                color = palette.tongueColor,
                topLeft = Offset(headX - 6f * scale, mouthY),
                size = Size(12f * scale, chewHeight)
            )
            return
        }

        when (expression) {
            PetExpression.SURPRISED -> {
                // Round cute 'o' mouth
                drawOval(
                    color = palette.noseColor,
                    topLeft = Offset(headX - 5f * scale, mouthY),
                    size = Size(10f * scale, 12f * scale)
                )
                drawOval(
                    color = palette.tongueColor,
                    topLeft = Offset(headX - 3.5f * scale, mouthY + 4f * scale),
                    size = Size(7f * scale, 6f * scale)
                )
            }

            PetExpression.SAD -> {
                // Downturned sad curved frown ( ︵ )
                val frownPath = Path().apply {
                    moveTo(headX - 8f * scale, mouthY + 6f * scale)
                    quadraticTo(headX, mouthY + 1f * scale, headX + 8f * scale, mouthY + 6f * scale)
                }
                drawPath(frownPath, palette.mouthColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }

            PetExpression.HUNGRY -> {
                // Pleading little wistful open mouth with drool bead
                val poutPath = Path().apply {
                    moveTo(headX - 6f * scale, mouthY + 4f * scale)
                    quadraticTo(headX, mouthY + 8f * scale, headX + 6f * scale, mouthY + 4f * scale)
                }
                drawPath(poutPath, palette.mouthColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                // Little drool sparkle drop
                drawCircle(Color(0xFF67E8F9), radius = 2f * scale, center = Offset(headX + 7f * scale, mouthY + 9f * scale))
            }

            PetExpression.VERY_HAPPY, PetExpression.EXCITED -> {
                // Wide open joyful laughing mouth with visible tongue
                val openMouth = Path().apply {
                    moveTo(headX - 10f * scale, mouthY)
                    quadraticTo(headX, mouthY + 15f * scale, headX + 10f * scale, mouthY)
                    close()
                }
                drawPath(openMouth, Color(0xFF881337))
                // Pink playful tongue
                drawOval(
                    color = palette.tongueColor,
                    topLeft = Offset(headX - 6f * scale, mouthY + 5f * scale),
                    size = Size(12f * scale, 10f * scale)
                )
            }

            PetExpression.SLEEPY -> {
                // Relaxed tiny sleepy mouth
                val sleepMouth = Path().apply {
                    moveTo(headX - 5f * scale, mouthY + 2f * scale)
                    quadraticTo(headX, mouthY + 5f * scale, headX + 5f * scale, mouthY + 2f * scale)
                }
                drawPath(sleepMouth, palette.mouthColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }

            else -> {
                // Default & Happy & Loved: Cute 'ω' (w) smile
                val wMouth = Path().apply {
                    moveTo(headX - 9f * scale, mouthY)
                    quadraticTo(headX - 4.5f * scale, mouthY + 5f * scale, headX, mouthY + 1f * scale)
                    quadraticTo(headX + 4.5f * scale, mouthY + 5f * scale, headX + 9f * scale, mouthY)
                }
                drawPath(wMouth, palette.mouthColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

                // Dog panting tongue when Happy / Loved
                if (petType == PetType.DOG && (expression == PetExpression.HAPPY || expression == PetExpression.LOVED)) {
                    drawOval(
                        color = palette.tongueColor,
                        topLeft = Offset(headX - 5f * scale, mouthY + 2f * scale),
                        size = Size(10f * scale, 14f * scale)
                    )
                }
            }
        }
    }

    // =========================================================================
    // EYES & EYELIDS (9 Expressions)
    // =========================================================================
    private fun DrawScope.drawExpressionEyes(
        expression: PetExpression,
        palette: PetPalette,
        headX: Float,
        headY: Float,
        scale: Float,
        isBlinking: Boolean,
        lookTargetX: Float,
        lookTargetY: Float,
        animTime: Float
    ) {
        val eyeSpacing = 24f * scale
        val eyeY = headY - 4f * scale
        val eyeW = 22f * scale
        val eyeH = 28f * scale

        // 1. Natural Blinking or Sleeping or Sleepy closed
        if (isBlinking || expression == PetExpression.SLEEPY) {
            // Closed peaceful smiling arcs ( ˘ ◡ ˘ )
            listOf(headX - eyeSpacing, headX + eyeSpacing).forEach { ex ->
                val closedEye = Path().apply {
                    moveTo(ex - 11f * scale, eyeY)
                    quadraticTo(ex, eyeY + 9f * scale, ex + 11f * scale, eyeY)
                }
                drawPath(closedEye, palette.eyeOutline, style = Stroke(width = 3.6f * scale, cap = StrokeCap.Round))
                // Tiny soft eyelash tick at corner
                drawLine(palette.eyeOutline, Offset(ex + 10f * scale, eyeY), Offset(ex + 14f * scale, eyeY - 4f * scale), strokeWidth = 2.4f * scale, cap = StrokeCap.Round)
            }
            return
        }

        // 2. Beaming Happy Crescents ( ^ ‿ ^ ) for VERY_HAPPY
        if (expression == PetExpression.VERY_HAPPY) {
            listOf(headX - eyeSpacing, headX + eyeSpacing).forEach { ex ->
                val happyArc = Path().apply {
                    moveTo(ex - 11f * scale, eyeY + 5f * scale)
                    quadraticTo(ex, eyeY - 8f * scale, ex + 11f * scale, eyeY + 5f * scale)
                }
                drawPath(happyArc, palette.eyeOutline, style = Stroke(width = 4.2f * scale, cap = StrokeCap.Round))
            }
            return
        }

        // 3. LOVED Expression: Heart Sparkles or Beaming Eyes with Hearts
        if (expression == PetExpression.LOVED) {
            listOf(headX - eyeSpacing, headX + eyeSpacing).forEach { ex ->
                // Draw heart in place of iris
                drawHeartShape(ex, eyeY, 11f * scale, NeonPink)
                // Specular glint
                drawCircle(Color.White, radius = 2.5f * scale, center = Offset(ex - 3f * scale, eyeY - 4f * scale))
            }
            return
        }

        // 4. Open Expressive Eyes (NORMAL, HAPPY, EXCITED, SURPRISED, HUNGRY, SAD)
        val pupilOffsetX = lookTargetX * 4f * scale
        val pupilOffsetY = -lookTargetY * 3f * scale

        listOf(headX - eyeSpacing, headX + eyeSpacing).forEach { ex ->
            // Eye Silhouette / Dark Eyeliner Base
            drawOval(
                color = palette.eyeOutline,
                topLeft = Offset(ex - eyeW / 2f, eyeY - eyeH / 2f),
                size = Size(eyeW, eyeH)
            )

            // Inner Eye Sclera / Base
            drawOval(
                color = Color(0xFF160D28),
                topLeft = Offset(ex - (eyeW * 0.92f) / 2f, eyeY - (eyeH * 0.92f) / 2f),
                size = Size(eyeW * 0.92f, eyeH * 0.92f)
            )

            // Multi-Stop Radiant Gradient Iris (Amber / Emerald / Hazel / Neon)
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.eyeIrisTop, palette.eyeIrisBottom, palette.eyeIrisGlow),
                    startY = eyeY - eyeH / 3f,
                    endY = eyeY + eyeH / 2f
                ),
                topLeft = Offset(ex - (eyeW * 0.84f) / 2f + pupilOffsetX * 0.4f, eyeY - (eyeH * 0.35f) + pupilOffsetY * 0.4f),
                size = Size(eyeW * 0.84f, eyeH * 0.80f)
            )

            // Internal luminous iris crescent at bottom (Marble gloss effect)
            drawOval(
                color = palette.eyeIrisGlow.copy(alpha = 0.7f),
                topLeft = Offset(ex - (eyeW * 0.60f) / 2f + pupilOffsetX * 0.4f, eyeY + 2f * scale + pupilOffsetY * 0.4f),
                size = Size(eyeW * 0.60f, 6f * scale)
            )

            // Deep Black Pupil (Dilated for EXCITED / SURPRISED; Smaller for NORMAL)
            val pupilScale = when (expression) {
                PetExpression.EXCITED, PetExpression.SURPRISED -> 0.68f
                PetExpression.HUNGRY -> 0.62f
                else -> 0.48f
            }
            drawOval(
                color = Color(0xFF0A0412),
                topLeft = Offset(ex - (eyeW * pupilScale) / 2f + pupilOffsetX, eyeY - (eyeH * 0.15f) + pupilOffsetY),
                size = Size(eyeW * pupilScale, eyeH * pupilScale * 1.15f)
            )

            // Specular Highlights: Large Crisp Shine + Secondary Specular Dot
            val shineOffset = pupilOffsetX * 0.2f
            drawCircle(
                color = Color.White,
                radius = 5.0f * scale,
                center = Offset(ex - 3.2f * scale + shineOffset, eyeY - 5.5f * scale)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 2.4f * scale,
                center = Offset(ex + 3.8f * scale + shineOffset, eyeY + 4.2f * scale)
            )

            // Star Sparkle in Eye for EXCITED
            if (expression == PetExpression.EXCITED) {
                drawCircle(
                    color = GoldCoin.copy(alpha = 0.85f),
                    radius = 2.0f * scale,
                    center = Offset(ex + shineOffset, eyeY + 1f * scale)
                )
            }

            // Teary glistening specular for SAD or HUNGRY
            if (expression == PetExpression.SAD || expression == PetExpression.HUNGRY) {
                drawCircle(
                    color = Color(0xFFBAE6FD).copy(alpha = 0.85f),
                    radius = 3.0f * scale,
                    center = Offset(ex - 1f * scale, eyeY + 9f * scale)
                )
            }
        }
    }

    private fun DrawScope.drawHeartShape(x: Float, y: Float, size: Float, color: Color) {
        val path = Path().apply {
            moveTo(x, y + size * 0.6f)
            cubicTo(x - size, y - size * 0.2f, x - size, y - size * 0.8f, x, y - size * 0.3f)
            cubicTo(x + size, y - size * 0.8f, x + size, y - size * 0.2f, x, y + size * 0.6f)
            close()
        }
        drawPath(path, color)
    }

    // =========================================================================
    // FRONT PAWS (Interactive Eating, Bath Grooming, Waving, Resting)
    // =========================================================================
    private fun DrawScope.drawFrontPaws(
        palette: PetPalette,
        x: Float,
        y: Float,
        scale: Float,
        isEating: Boolean,
        isBathActive: Boolean,
        isExcited: Boolean,
        time: Float
    ) {
        val pawW = 28f * scale
        val pawH = 22f * scale

        if (isEating) {
            // Both paws hold the snack up near mouth
            val snackY = y - 24f * scale
            // Left Paw holding snack
            drawOval(
                brush = Brush.verticalGradient(listOf(palette.muzzleBase, palette.muzzleShadow)),
                topLeft = Offset(x - 28f * scale, snackY),
                size = Size(pawW, pawH)
            )
            // Right Paw holding snack
            drawOval(
                brush = Brush.verticalGradient(listOf(palette.muzzleBase, palette.muzzleShadow)),
                topLeft = Offset(x, snackY),
                size = Size(pawW, pawH)
            )
            // Cute golden snack item being eaten
            drawCircle(GoldCoin, radius = 9f * scale, center = Offset(x, snackY + 8f * scale))
            return
        }

        if (isBathActive) {
            // Cute grooming paw swipe across cheek
            val swipeY = sin(time * 8f) * 12f * scale
            drawOval(
                brush = Brush.verticalGradient(listOf(palette.muzzleBase, palette.muzzleShadow)),
                topLeft = Offset(x - 38f * scale, y),
                size = Size(pawW, pawH)
            )
            // Raised right paw grooming
            drawOval(
                brush = Brush.verticalGradient(listOf(palette.muzzleBase, palette.muzzleShadow)),
                topLeft = Offset(x + 18f * scale, y - 36f * scale + swipeY),
                size = Size(pawW, pawH)
            )
            drawCircle(palette.pawPadColor, radius = 3.5f * scale, center = Offset(x + 32f * scale, y - 26f * scale + swipeY))
            return
        }

        // Default or Excited waving paws
        val waveBob = if (isExcited) sin(time * 12f) * 8f * scale else 0f

        // Left Paw
        drawOval(
            brush = Brush.verticalGradient(listOf(palette.muzzleBase, palette.muzzleShadow)),
            topLeft = Offset(x - 38f * scale, y - waveBob),
            size = Size(pawW, pawH)
        )
        drawCircle(palette.pawPadColor.copy(alpha = 0.55f), radius = 3.5f * scale, center = Offset(x - 24f * scale, y + 10f * scale - waveBob))

        // Right Paw
        drawOval(
            brush = Brush.verticalGradient(listOf(palette.muzzleBase, palette.muzzleShadow)),
            topLeft = Offset(x + 10f * scale, y + waveBob),
            size = Size(pawW, pawH)
        )
        drawCircle(palette.pawPadColor.copy(alpha = 0.55f), radius = 3.5f * scale, center = Offset(x + 24f * scale, y + 10f * scale + waveBob))
    }

    // =========================================================================
    // ACCESSORIES (HATS, GLASSES, COLLARS, WINGS, HALOS)
    // =========================================================================
    private fun DrawScope.drawPetCollar(x: Float, y: Float, scale: Float, collarId: String) {
        val cColor = when (collarId) {
            "collar_bell" -> Color(0xFFEF4444)
            "collar_spikes" -> Color(0xFF475569)
            "collar_neon" -> NeonMint
            "collar_ribbon" -> NeonPink
            else -> NeonPurple
        }

        drawRoundRect(
            color = cColor,
            topLeft = Offset(x - 38f * scale, y),
            size = Size(76f * scale, 12f * scale),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )

        if (collarId == "collar_bell") {
            // Golden Jingling Bell
            drawCircle(GoldCoin, radius = 8.5f * scale, center = Offset(x, y + 12f * scale))
            drawCircle(Color.White.copy(alpha = 0.8f), radius = 2.5f * scale, center = Offset(x - 2f * scale, y + 9f * scale))
        } else if (collarId == "collar_ribbon") {
            val bowPath = Path().apply {
                moveTo(x, y + 8f * scale)
                lineTo(x - 14f * scale, y + 2f * scale)
                lineTo(x - 14f * scale, y + 16f * scale)
                lineTo(x, y + 10f * scale)
                lineTo(x + 14f * scale, y + 16f * scale)
                lineTo(x + 14f * scale, y + 2f * scale)
                close()
            }
            drawPath(bowPath, NeonPink)
            drawCircle(Color.White, radius = 3.5f * scale, center = Offset(x, y + 9f * scale))
        } else if (collarId == "collar_spikes") {
            for (i in -2..2) {
                drawCircle(GoldCoin, radius = 3.5f * scale, center = Offset(x + i * 15f * scale, y + 6f * scale))
            }
        }
    }

    private fun DrawScope.drawPetGlasses(x: Float, y: Float, scale: Float, glassesId: String) {
        when (glassesId) {
            "glasses_shades" -> {
                drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(x - 45f * scale, y),
                    size = Size(38f * scale, 24f * scale),
                    cornerRadius = CornerRadius(5f * scale, 5f * scale)
                )
                drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(x + 7f * scale, y),
                    size = Size(38f * scale, 24f * scale),
                    cornerRadius = CornerRadius(5f * scale, 5f * scale)
                )
                drawRect(Color(0xFF0F172A), Offset(x - 8f * scale, y + 4f * scale), Size(16f * scale, 5f * scale))
                drawLine(NeonMint, Offset(x - 40f * scale, y + 6f * scale), Offset(x - 14f * scale, y + 18f * scale), strokeWidth = 2.5f * scale)
                drawLine(NeonMint, Offset(x + 12f * scale, y + 6f * scale), Offset(x + 38f * scale, y + 18f * scale), strokeWidth = 2.5f * scale)
            }
            "glasses_nerd" -> {
                drawCircle(Color(0xFF334155), radius = 15f * scale, center = Offset(x - 24f * scale, y + 10f * scale), style = Stroke(width = 3f * scale))
                drawCircle(Color(0xFF334155), radius = 15f * scale, center = Offset(x + 24f * scale, y + 10f * scale), style = Stroke(width = 3f * scale))
                drawLine(Color(0xFF334155), Offset(x - 9f * scale, y + 10f * scale), Offset(x + 9f * scale, y + 10f * scale), strokeWidth = 2.5f * scale)
            }
            "glasses_star" -> {
                drawCircle(GoldCoin, radius = 14f * scale, center = Offset(x - 24f * scale, y + 10f * scale))
                drawCircle(GoldCoin, radius = 14f * scale, center = Offset(x + 24f * scale, y + 10f * scale))
                drawLine(GoldCoin, Offset(x - 10f * scale, y + 10f * scale), Offset(x + 10f * scale, y + 10f * scale), strokeWidth = 3f * scale)
            }
        }
    }

    private fun DrawScope.drawPetHat(x: Float, y: Float, scale: Float, hatId: String, tilt: Float) {
        when (hatId) {
            "hat_crown" -> {
                val crownPath = Path().apply {
                    moveTo(x - 32f * scale, y + 35f * scale)
                    lineTo(x - 32f * scale, y + 8f * scale)
                    lineTo(x - 16f * scale, y + 20f * scale)
                    lineTo(x, y - 4f * scale)
                    lineTo(x + 16f * scale, y + 20f * scale)
                    lineTo(x + 32f * scale, y + 8f * scale)
                    lineTo(x + 32f * scale, y + 35f * scale)
                    close()
                }
                drawPath(crownPath, brush = Brush.verticalGradient(listOf(Color(0xFFFFE066), GoldCoin)))
                drawCircle(Color(0xFFEF4444), radius = 4f * scale, center = Offset(x - 32f * scale, y + 8f * scale))
                drawCircle(NeonMint, radius = 4.5f * scale, center = Offset(x, y - 4f * scale))
                drawCircle(Color(0xFFEF4444), radius = 4f * scale, center = Offset(x + 32f * scale, y + 8f * scale))
            }
            "hat_party" -> {
                val cone = Path().apply {
                    moveTo(x - 24f * scale, y + 42f * scale)
                    lineTo(x, y - 18f * scale)
                    lineTo(x + 24f * scale, y + 42f * scale)
                    close()
                }
                drawPath(cone, NeonPink)
                drawCircle(GoldCoin, radius = 7f * scale, center = Offset(x, y - 18f * scale))
            }
            "hat_wizard" -> {
                val wizard = Path().apply {
                    moveTo(x - 38f * scale, y + 38f * scale)
                    lineTo(x, y - 28f * scale)
                    lineTo(x + 38f * scale, y + 38f * scale)
                    close()
                }
                drawPath(wizard, Color(0xFF4C1D95))
                drawOval(Color(0xFF2E1065), Offset(x - 46f * scale, y + 34f * scale), Size(92f * scale, 14f * scale))
                drawCircle(GoldCoin, radius = 5f * scale, center = Offset(x - 6f * scale, y + 10f * scale))
            }
            "hat_detective" -> {
                drawOval(Color(0xFF78350F), Offset(x - 44f * scale, y + 25f * scale), Size(88f * scale, 22f * scale))
                drawRoundRect(Color(0xFF92400E), Offset(x - 30f * scale, y + 8f * scale), Size(60f * scale, 26f * scale), CornerRadius(12f * scale, 12f * scale))
            }
            "hat_chef" -> {
                drawCircle(Color.White, radius = 18f * scale, center = Offset(x - 16f * scale, y + 14f * scale))
                drawCircle(Color.White, radius = 22f * scale, center = Offset(x, y + 6f * scale))
                drawCircle(Color.White, radius = 18f * scale, center = Offset(x + 16f * scale, y + 14f * scale))
                drawRect(Color(0xFFF1F5F9), Offset(x - 28f * scale, y + 24f * scale), Size(56f * scale, 14f * scale))
            }
        }
    }

    private fun DrawScope.drawAngelWings(x: Float, y: Float, scale: Float, time: Float) {
        val wingFlap = sin(time * 3.5f) * 8f * scale
        val lw = Path().apply {
            moveTo(x - 38f * scale, y + 15f * scale)
            cubicTo(
                x - 90f * scale, y - 45f * scale + wingFlap,
                x - 110f * scale, y + 35f * scale,
                x - 45f * scale, y + 55f * scale
            )
            close()
        }
        drawPath(lw, Color(0xE6FFFFFF))
        drawPath(lw, Color(0x66A78BFA), style = Stroke(width = 2.5f * scale))

        val rw = Path().apply {
            moveTo(x + 38f * scale, y + 15f * scale)
            cubicTo(
                x + 90f * scale, y - 45f * scale + wingFlap,
                x + 110f * scale, y + 35f * scale,
                x + 45f * scale, y + 55f * scale
            )
            close()
        }
        drawPath(rw, Color(0xE6FFFFFF))
        drawPath(rw, Color(0x66A78BFA), style = Stroke(width = 2.5f * scale))
    }

    private fun DrawScope.drawFloatingHalo(x: Float, y: Float, scale: Float, time: Float) {
        val haloFloat = sin(time * 2.8f) * 6f * scale
        drawOval(
            brush = Brush.radialGradient(listOf(GoldCoin, Color(0xFFFFBE0B))),
            topLeft = Offset(x - 36f * scale, y + haloFloat),
            size = Size(72f * scale, 18f * scale),
            style = Stroke(width = 5.5f * scale)
        )
    }
}
