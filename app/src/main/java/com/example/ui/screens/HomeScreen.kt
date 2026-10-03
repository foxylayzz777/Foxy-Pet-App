package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import com.example.data.model.GameProgressEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.data.model.PetEntity
import com.example.ui.components.PetCanvas
import com.example.ui.components.StatBar
import com.example.ui.components.TopStatsHeader
import com.example.ui.components.WorldBackground
import com.example.ui.pet2d.Pet2DView
import com.example.ui.theme.CleanBlue
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EnergyOrange
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.HealthRed
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LiquidGlassCapsule
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    pet: PetEntity?,
    progress: GameProgressEntity?,
    inventory: List<InventoryItemEntity>,
    isBathActive: Boolean,
    isFeedingActive: Boolean,
    isGiftActive: Boolean = false,
    isLevelUpActive: Boolean = false,
    onFeed: (String) -> Unit,
    onClean: () -> Unit,
    onPet: () -> Unit,
    onSleepToggle: () -> Unit,
    onGift: () -> Unit,
    onSwitchWorld: (String) -> Unit,
    onNavigateToShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (pet == null) return
    val haptic = LocalHapticFeedback.current

    var showFoodSheet by remember { mutableStateOf(false) }
    var showWorldSelector by remember { mutableStateOf(false) }

    val mood = pet.calculateMood()
    val ownedFoods = inventory.filter { it.category == ItemCategory.FOOD && it.quantity > 0 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        if (progress != null) {
            TopStatsHeader(
                coins = progress.coins,
                level = progress.level,
                xp = progress.xp,
                xpRequired = progress.xpForNextLevel(),
                streakDays = progress.streakDays
            )
        }

        // Premium Animated 2.5D Liquid Glass Hero Stage
        LiquidGlassCard(
            shape = RoundedCornerShape(32.dp),
            accentBorderColor = NeonPurpleLight,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("pet_home_stage")
        ) {
            // Layered Animated 2.5D Pet Hero Scene (Custom Anatomy, Soft Shadows, Depth & Interactive Parallax)
            Pet2DView(
                pet = pet,
                isBathActive = isBathActive,
                isFeedingActive = isFeedingActive,
                isGiftActive = isGiftActive,
                isLevelUpActive = isLevelUpActive,
                onPetTapped = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPet()
                },
                modifier = Modifier.fillMaxSize()
            )

            // Sleeping Night Dimmer Overlay
            if (pet.isSleeping) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x990A041E))
                )
            }

            // Mood bubble top-left Liquid Glass Capsule
            LiquidGlassCapsule(
                shape = RoundedCornerShape(20.dp),
                accentBorderColor = NeonPurpleLight.copy(alpha = 0.45f),
                modifier = Modifier
                    .padding(14.dp)
                    .align(Alignment.TopStart)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = mood.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = mood.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // World Switch Button top-right Liquid Glass Capsule
            LiquidGlassCapsule(
                shape = RoundedCornerShape(20.dp),
                accentBorderColor = NeonMint.copy(alpha = 0.45f),
                onClick = {
                    SoundManager.playTap()
                    showWorldSelector = true
                },
                modifier = Modifier
                    .padding(14.dp)
                    .align(Alignment.TopEnd)
                    .testTag("switch_world_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🌎", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = getWorldDisplayName(progress?.currentWorld ?: "BEDROOM"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMint
                    )
                }
            }

            // Pet Name & Sleep Status badge bottom center
            LiquidGlassCapsule(
                shape = RoundedCornerShape(18.dp),
                accentBorderColor = Color(0x35FFFFFF),
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pet.name} ${if (pet.isSleeping) "💤" else ""}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• Companion 🐾",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMint
                    )
                }
            }
        }

        // Action Buttons Row (Feed, Clean, Play/Pet, Sleep, Gift)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionButtonItem(
                emoji = "🍖",
                label = "Feed",
                containerColor = Color(0xFFEF4444),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    SoundManager.playTap()
                    showFoodSheet = true
                },
                testTag = "feed_action_btn"
            )
            ActionButtonItem(
                emoji = "🛁",
                label = "Clean",
                containerColor = CleanBlue,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClean()
                },
                testTag = "clean_action_btn"
            )
            ActionButtonItem(
                emoji = if (pet.isSleeping) "☀️" else "😴",
                label = if (pet.isSleeping) "Wake" else "Sleep",
                containerColor = NeonPurple,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSleepToggle()
                },
                testTag = "sleep_action_btn"
            )
            ActionButtonItem(
                emoji = "❤️",
                label = "Pet",
                containerColor = NeonPink,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPet()
                },
                testTag = "pet_action_btn"
            )
            ActionButtonItem(
                emoji = "🎁",
                label = "Gift",
                containerColor = GoldCoin,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onGift()
                },
                testTag = "gift_action_btn"
            )
        }

        // Pet Stats Overview Card (Liquid Glass Vitals)
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(26.dp),
            accentBorderColor = NeonPurpleLight.copy(alpha = 0.35f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Companion Vitals",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "• Realtime Status",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMint
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                StatBar(
                    name = "Hunger",
                    value = pet.hunger,
                    iconEmoji = "🍖",
                    barColor = EnergyOrange
                )
                Spacer(modifier = Modifier.height(11.dp))
                StatBar(
                    name = "Happiness",
                    value = pet.happiness,
                    iconEmoji = "💖",
                    barColor = NeonPink
                )
                Spacer(modifier = Modifier.height(11.dp))
                StatBar(
                    name = "Energy",
                    value = pet.energy,
                    iconEmoji = "⚡",
                    barColor = GoldCoin
                )
                Spacer(modifier = Modifier.height(11.dp))
                StatBar(
                    name = "Cleanliness",
                    value = pet.cleanliness,
                    iconEmoji = "🛁",
                    barColor = CleanBlue
                )
                Spacer(modifier = Modifier.height(11.dp))
                StatBar(
                    name = "Health",
                    value = pet.health,
                    iconEmoji = "💚",
                    barColor = NeonMint
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
    }

    // Feeding Bottom Sheet
    if (showFoodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFoodSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag("food_sheet")
            ) {
                Text(
                    text = "Feed ${pet.name} 🍖",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a treat from your pantry to restore hunger & mood",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (ownedFoods.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🍽️", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Your fridge is empty!", color = TextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    showFoodSheet = false
                                    onNavigateToShop()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                            ) {
                                Text(text = "Visit Pet Market 🛒")
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ownedFoods) { food ->
                            FoodItemCard(
                                food = food,
                                onFeed = {
                                    onFeed(food.itemId)
                                    showFoodSheet = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // World Environment Selector Dialog / Sheet
    if (showWorldSelector) {
        val worlds = listOf(
            Triple("world_bedroom", "Cozy Bedroom", "🛏️"),
            Triple("world_garden", "Sunflower Garden", "🌻"),
            Triple("world_park", "Sunny City Park", "🌳"),
            Triple("world_beach", "Tropical Beach", "🏖️"),
            Triple("world_forest", "Whispering Forest", "🌲"),
            Triple("world_night", "Starry Night Sky", "🌌")
        )

        ModalBottomSheet(
            onDismissRequest = { showWorldSelector = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag("world_selector_sheet")
            ) {
                Text(
                    text = "Travel to New Worlds 🌎",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                worlds.forEach { (id, name, emoji) ->
                    val isCurrent = progress?.currentWorld == id || (progress?.currentWorld == "BEDROOM" && id == "world_bedroom")
                    val isUnlocked = id == "world_bedroom" || inventory.any { it.itemId == id && it.isUnlocked }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = isUnlocked) {
                                onSwitchWorld(id)
                                showWorldSelector = false
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCurrent) NeonPurple.copy(alpha = 0.25f) else DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) NeonPurple else DarkBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isUnlocked) TextPrimary else TextSecondary
                                    )
                                    Text(
                                        text = if (isCurrent) "Current World" else if (isUnlocked) "Unlocked" else "Locked (Unlock in Shop)",
                                        fontSize = 11.sp,
                                        color = if (isCurrent) NeonMint else TextSecondary
                                    )
                                }
                            }
                            if (!isUnlocked) {
                                Text(text = "🔒", fontSize = 16.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ActionButtonItem(
    emoji: String,
    label: String,
    containerColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                SoundManager.playTap()
                onClick()
            }
            .testTag(testTag)
    ) {
        // Liquid Glass Floating Sphere
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            containerColor.copy(alpha = 0.50f),
                            containerColor.copy(alpha = 0.20f),
                            Color(0x3512082C)
                        ),
                        center = Offset(24f, 20f),
                        radius = 48f
                    )
                )
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0x95FFFFFF),
                                containerColor.copy(alpha = 0.85f),
                                Color(0x20FFFFFF)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(45f, 60f)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 23.sp)
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun FoodItemCard(
    food: InventoryItemEntity,
    onFeed: () -> Unit
) {
    LiquidGlassCard(
        shape = RoundedCornerShape(22.dp),
        accentBorderColor = NeonPurpleLight.copy(alpha = 0.45f),
        onClick = onFeed,
        modifier = Modifier
            .width(135.dp)
            .testTag("food_card_${food.itemId}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0x301D1140))
                    .border(BorderStroke(1.dp, Color(0x40FFFFFF)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = food.iconEmoji, fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = food.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Left: x${food.quantity}",
                fontSize = 10.sp,
                color = NeonMint,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onFeed,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0x60FFFFFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                Text(text = "Feed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun getWorldDisplayName(worldId: String): String {
    return when (worldId) {
        "world_bedroom", "BEDROOM" -> "Bedroom"
        "world_garden", "GARDEN" -> "Garden"
        "world_park", "PARK" -> "Park"
        "world_beach", "BEACH" -> "Beach"
        "world_forest", "FOREST" -> "Forest"
        "world_night", "NIGHT" -> "Night"
        else -> "Bedroom"
    }
}
