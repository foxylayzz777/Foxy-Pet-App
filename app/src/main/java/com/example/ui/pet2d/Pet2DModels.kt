package com.example.ui.pet2d

import androidx.compose.ui.graphics.Color
import com.example.data.model.PetEntity
import com.example.data.model.PetMood
import com.example.data.model.PetType

enum class PetExpression(val title: String, val emoji: String) {
    NORMAL("Normal", "😌"),
    HAPPY("Happy", "😊"),
    VERY_HAPPY("Very Happy", "😄"),
    EXCITED("Excited", "🤩"),
    SLEEPY("Sleepy", "😴"),
    HUNGRY("Hungry", "🥺"),
    SAD("Sad", "😢"),
    SURPRISED("Surprised", "😮"),
    LOVED("Loved", "🥰")
}

data class PetPalette(
    val furBase: Color,
    val furShadow: Color,
    val furHighlight: Color,
    val furRimGlow: Color,
    val muzzleBase: Color,
    val muzzleShadow: Color,
    val innerEar: Color,
    val earTuft: Color,
    val earTip: Color? = null,
    val eyeIrisTop: Color,
    val eyeIrisBottom: Color,
    val eyeIrisGlow: Color,
    val eyeOutline: Color = Color(0xFF130924),
    val eyeShine: Color = Color.White,
    val noseColor: Color,
    val mouthColor: Color,
    val tongueColor: Color,
    val blushColor: Color,
    val pawPadColor: Color,
    val tailBase: Color,
    val tailHighlight: Color,
    val tailTip: Color
)

object Pet2DPalettes {

    fun getPalette(type: PetType, skinId: String): PetPalette {
        return when (skinId) {
            "skin_neon" -> PetPalette(
                furBase = Color(0xFF1E1B4B),
                furShadow = Color(0xFF0F172A),
                furHighlight = Color(0xFF4338CA),
                furRimGlow = Color(0xFF00F5D4),
                muzzleBase = Color(0xFF0F172A),
                muzzleShadow = Color(0xFF020617),
                innerEar = Color(0xFF00F5D4),
                earTuft = Color(0xFF67E8F9),
                earTip = Color(0xFF00F5D4),
                eyeIrisTop = Color(0xFF00F5D4),
                eyeIrisBottom = Color(0xFF059669),
                eyeIrisGlow = Color(0xFF5EEAD4),
                eyeOutline = Color(0xFF020617),
                noseColor = Color(0xFF00F5D4),
                mouthColor = Color(0xFF00F5D4),
                tongueColor = Color(0xFFFF2A85),
                blushColor = Color(0x77FF2A85),
                pawPadColor = Color(0xFF00F5D4),
                tailBase = Color(0xFF1E1B4B),
                tailHighlight = Color(0xFF312E81),
                tailTip = Color(0xFF00F5D4)
            )
            "skin_galaxy" -> PetPalette(
                furBase = Color(0xFF3B0764),
                furShadow = Color(0xFF2E1065),
                furHighlight = Color(0xFF6B21A8),
                furRimGlow = Color(0xFFE879F9),
                muzzleBase = Color(0xFFE9D5FF),
                muzzleShadow = Color(0xFFC084FC),
                innerEar = Color(0xFFE879F9),
                earTuft = Color(0xFFF5D0FE),
                earTip = Color(0xFF581C87),
                eyeIrisTop = Color(0xFFE879F9),
                eyeIrisBottom = Color(0xFF9333EA),
                eyeIrisGlow = Color(0xFFF0ABFC),
                eyeOutline = Color(0xFF1E0538),
                noseColor = Color(0xFF2E1065),
                mouthColor = Color(0xFF2E1065),
                tongueColor = Color(0xFFF472B6),
                blushColor = Color(0x77F472B6),
                pawPadColor = Color(0xFFC084FC),
                tailBase = Color(0xFF3B0764),
                tailHighlight = Color(0xFF581C87),
                tailTip = Color(0xFFE879F9)
            )
            "skin_sakura" -> PetPalette(
                furBase = Color(0xFFFFE4E6),
                furShadow = Color(0xFFFECDD3),
                furHighlight = Color(0xFFFFF1F2),
                furRimGlow = Color(0xFFFDA4AF),
                muzzleBase = Color(0xFFFFFFFF),
                muzzleShadow = Color(0xFFFFF1F2),
                innerEar = Color(0xFFFB7185),
                earTuft = Color(0xFFFFFFFF),
                earTip = Color(0xFFF43F5E),
                eyeIrisTop = Color(0xFFE11D48),
                eyeIrisBottom = Color(0xFFFB7185),
                eyeIrisGlow = Color(0xFFFDA4AF),
                eyeOutline = Color(0xFF4C0519),
                noseColor = Color(0xFF881337),
                mouthColor = Color(0xFF881337),
                tongueColor = Color(0xFFFB7185),
                blushColor = Color(0x77F43F5E),
                pawPadColor = Color(0xFFFB7185),
                tailBase = Color(0xFFFFE4E6),
                tailHighlight = Color(0xFFFFF1F2),
                tailTip = Color(0xFFFFFFFF)
            )
            "skin_golden" -> PetPalette(
                furBase = Color(0xFFFDE047),
                furShadow = Color(0xFFEAB308),
                furHighlight = Color(0xFFFEF08A),
                furRimGlow = Color(0xFFFACC15),
                muzzleBase = Color(0xFFFFFBEB),
                muzzleShadow = Color(0xFFFEF3C7),
                innerEar = Color(0xFFCA8A04),
                earTuft = Color(0xFFFFFBEB),
                earTip = Color(0xFF713F12),
                eyeIrisTop = Color(0xFFB45309),
                eyeIrisBottom = Color(0xFFF59E0B),
                eyeIrisGlow = Color(0xFFFDE047),
                eyeOutline = Color(0xFF451A03),
                noseColor = Color(0xFF713F12),
                mouthColor = Color(0xFF713F12),
                tongueColor = Color(0xFFFB7185),
                blushColor = Color(0x66F59E0B),
                pawPadColor = Color(0xFFEAB308),
                tailBase = Color(0xFFFDE047),
                tailHighlight = Color(0xFFFEF08A),
                tailTip = Color(0xFFFFFBEB)
            )
            else -> when (type) {
                PetType.FOX -> PetPalette(
                    furBase = Color(0xFFFF6D3B),
                    furShadow = Color(0xFFD84A1B),
                    furHighlight = Color(0xFFFF926B),
                    furRimGlow = Color(0xFFFFB295),
                    muzzleBase = Color(0xFFFFFBF5),
                    muzzleShadow = Color(0xFFFFE8D6),
                    innerEar = Color(0xFFFF9E80),
                    earTuft = Color(0xFFFFFBF5),
                    earTip = Color(0xFF2C1810),
                    eyeIrisTop = Color(0xFF8D3C03),
                    eyeIrisBottom = Color(0xFFF59E0B),
                    eyeIrisGlow = Color(0xFFFDE047),
                    eyeOutline = Color(0xFF1F100B),
                    noseColor = Color(0xFF231410),
                    mouthColor = Color(0xFF231410),
                    tongueColor = Color(0xFFFF6B8B),
                    blushColor = Color(0x66FF3366),
                    pawPadColor = Color(0xFFFF8A80),
                    tailBase = Color(0xFFFF6D3B),
                    tailHighlight = Color(0xFFFF926B),
                    tailTip = Color(0xFFFFFBF5)
                )
                PetType.CAT -> PetPalette(
                    furBase = Color(0xFFE2E8F0),
                    furShadow = Color(0xFFCBD5E1),
                    furHighlight = Color(0xFFFFFFFF),
                    furRimGlow = Color(0xFFF8FAFC),
                    muzzleBase = Color(0xFFFFFFFF),
                    muzzleShadow = Color(0xFFF1F5F9),
                    innerEar = Color(0xFFF472B6),
                    earTuft = Color(0xFFFFFFFF),
                    earTip = null,
                    eyeIrisTop = Color(0xFF047857),
                    eyeIrisBottom = Color(0xFF10B981),
                    eyeIrisGlow = Color(0xFF34D399),
                    eyeOutline = Color(0xFF09231B),
                    noseColor = Color(0xFFF472B6),
                    mouthColor = Color(0xFF475569),
                    tongueColor = Color(0xFFFB7185),
                    blushColor = Color(0x66F472B6),
                    pawPadColor = Color(0xFFF472B6),
                    tailBase = Color(0xFFE2E8F0),
                    tailHighlight = Color(0xFFFFFFFF),
                    tailTip = Color(0xFF94A3B8)
                )
                PetType.DOG -> PetPalette(
                    furBase = Color(0xFFD4A373),
                    furShadow = Color(0xFFA87342),
                    furHighlight = Color(0xFFE9C6A5),
                    furRimGlow = Color(0xFFF6E6D7),
                    muzzleBase = Color(0xFFFFF8EC),
                    muzzleShadow = Color(0xFFFAEDCD),
                    innerEar = Color(0xFF8C5827),
                    earTuft = Color(0xFFFFF8EC),
                    earTip = null,
                    eyeIrisTop = Color(0xFF422006),
                    eyeIrisBottom = Color(0xFF854D0E),
                    eyeIrisGlow = Color(0xFFCA8A04),
                    eyeOutline = Color(0xFF1E1005),
                    noseColor = Color(0xFF1E1005),
                    mouthColor = Color(0xFF1E1005),
                    tongueColor = Color(0xFFFF6B8B),
                    blushColor = Color(0x55F59E0B),
                    pawPadColor = Color(0xFF8C5827),
                    tailBase = Color(0xFFD4A373),
                    tailHighlight = Color(0xFFE9C6A5),
                    tailTip = Color(0xFFFFF8EC)
                )
            }
        }
    }

    fun resolveExpression(
        pet: PetEntity,
        isEating: Boolean,
        isBathActive: Boolean,
        isLoving: Boolean,
        isGiftActive: Boolean,
        isLevelUpActive: Boolean,
        isSurprised: Boolean
    ): PetExpression {
        return when {
            isLevelUpActive || isGiftActive -> PetExpression.EXCITED
            isLoving -> PetExpression.LOVED
            isSurprised -> PetExpression.SURPRISED
            isEating -> PetExpression.VERY_HAPPY
            pet.isSleeping -> PetExpression.SLEEPY
            pet.health < 40f || pet.happiness < 30f -> PetExpression.SAD
            pet.hunger < 35f -> PetExpression.HUNGRY
            pet.energy < 30f -> PetExpression.SLEEPY
            isBathActive -> PetExpression.HAPPY
            pet.happiness > 85f && pet.hunger > 70f -> PetExpression.VERY_HAPPY
            pet.happiness > 60f -> PetExpression.HAPPY
            else -> PetExpression.NORMAL
        }
    }
}
