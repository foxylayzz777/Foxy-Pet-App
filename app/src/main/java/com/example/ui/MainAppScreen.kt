package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.LevelUpDialog
import com.example.ui.navigation.AppTab
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PetCareScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.theme.AmbientLiquidBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FoxyPetViewModel
import com.example.util.SoundManager

@Composable
fun MainAppScreen(
    viewModel: FoxyPetViewModel = viewModel()
) {
    val activePet by viewModel.activePet.collectAsStateWithLifecycle()
    val allPets by viewModel.allPets.collectAsStateWithLifecycle()
    val progress by viewModel.gameProgress.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val missions by viewModel.dailyMissions.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    val isBathActive by viewModel.isBathActive.collectAsStateWithLifecycle()
    val isFeedingActive by viewModel.isFeedingActive.collectAsStateWithLifecycle()
    val isGiftActive by viewModel.isGiftActive.collectAsStateWithLifecycle()
    val levelUpEvent by viewModel.levelUpEvent.collectAsStateWithLifecycle()
    val randomEventMessage by viewModel.randomEventMessage.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.HOME) }

    // If on non-home tab, system back button takes user back to Home
    BackHandler(enabled = currentTab != AppTab.HOME) {
        currentTab = AppTab.HOME
    }

    // Show onboarding if not yet completed
    if (progress != null && !progress!!.isOnboardingCompleted) {
        OnboardingScreen(
            pets = allPets,
            onCompleteOnboarding = { selectedPetId, name ->
                viewModel.completeOnboarding(selectedPetId, name)
            }
        )
        return
    }

    AmbientLiquidBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                // Floating Liquid Glass Navigation Dock
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("bottom_nav_bar"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(34.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x751E1242),
                                        Color(0x65130A2E)
                                    )
                                )
                            )
                            .border(
                                BorderStroke(
                                    1.2.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0x80FFFFFF),
                                            Color(0x25FFFFFF),
                                            NeonPurpleLight.copy(alpha = 0.5f),
                                            Color(0x15FFFFFF)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(400f, 100f)
                                    )
                                ),
                                RoundedCornerShape(34.dp)
                            )
                            .drawWithContent {
                                drawContent()
                                // Specular glass gloss highlight line
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color(0x35FFFFFF),
                                            Color(0x05FFFFFF),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = 24.dp.toPx()
                                    )
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(26.dp))
                                        .then(
                                            if (isSelected) {
                                                Modifier
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                NeonPurple.copy(alpha = 0.40f),
                                                                Color(0x2500F5D4)
                                                            )
                                                        )
                                                    )
                                                    .border(
                                                        BorderStroke(1.dp, Color(0x60FFFFFF)),
                                                        RoundedCornerShape(26.dp)
                                                    )
                                            } else Modifier
                                        )
                                        .clickable {
                                            SoundManager.playTap()
                                            currentTab = tab
                                        }
                                        .testTag("nav_tab_${tab.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = tab.iconEmoji,
                                            fontSize = if (isSelected) 20.sp else 17.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = tab.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelected) NeonMint else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        pet = activePet,
                        progress = progress,
                        inventory = inventory,
                        isBathActive = isBathActive,
                        isFeedingActive = isFeedingActive,
                        isGiftActive = isGiftActive,
                        isLevelUpActive = levelUpEvent != null,
                        onFeed = { foodId -> viewModel.feedPet(foodId) },
                        onClean = { viewModel.cleanPet() },
                        onPet = { viewModel.petInteraction() },
                        onSleepToggle = { viewModel.toggleSleep() },
                        onGift = { viewModel.giveGift() },
                        onSwitchWorld = { worldId -> viewModel.switchWorld(worldId) },
                        onNavigateToShop = { currentTab = AppTab.SHOP }
                    )
                }
                AppTab.PLAY -> {
                    PlayScreen(
                        pet = activePet,
                        progress = progress,
                        onGameRewardEarned = { coins, xp ->
                            viewModel.recordMiniGame(coins, xp)
                        }
                    )
                }
                AppTab.PET -> {
                    PetCareScreen(
                        activePet = activePet,
                        allPets = allPets,
                        progress = progress,
                        inventory = inventory,
                        onSwitchPet = { petId -> viewModel.switchPet(petId) },
                        onRenamePet = { petId, name -> viewModel.renamePet(petId, name) },
                        onEquipItem = { petId, cat, itemId -> viewModel.equipItem(petId, cat, itemId) },
                        onNavigateToShop = { currentTab = AppTab.SHOP }
                    )
                }
                AppTab.SHOP -> {
                    ShopScreen(
                        progress = progress,
                        inventory = inventory,
                        onPurchaseItem = { itemId, onResult ->
                            viewModel.purchaseItem(itemId, onResult)
                        }
                    )
                }
                AppTab.PROFILE -> {
                    ProfileScreen(
                        pet = activePet,
                        progress = progress,
                        missions = missions,
                        achievements = achievements,
                        onClaimMission = { mission -> viewModel.claimMission(mission) },
                        onClaimAchievement = { ach -> viewModel.claimAchievement(ach) },
                        onUpdateSettings = { s, h, n -> viewModel.updateSettings(s, h, n) },
                        onResetGame = { viewModel.resetGame() }
                    )
                }
            }

            // Level Up Modal Celebration Dialog
            if (levelUpEvent != null) {
                LevelUpDialog(
                    newLevel = levelUpEvent!!.newLevel,
                    rewardCoins = levelUpEvent!!.bonusCoins,
                    onDismiss = { viewModel.dismissLevelUp() }
                )
            }

            // Random Occasional Event Dialog
            if (randomEventMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissRandomEvent() },
                    title = {
                        Text(
                            text = "Special Event! 🌟",
                            fontWeight = FontWeight.Black,
                            color = GoldCoin
                        )
                    },
                    text = {
                        Text(
                            text = randomEventMessage!!,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissRandomEvent() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Text("Yay!")
                        }
                    },
                    containerColor = DarkSurface
                )
            }
        }
    }
}
}
