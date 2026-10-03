package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementEntity
import com.example.data.model.DailyMissionEntity
import com.example.data.model.GameProgressEntity
import com.example.data.model.PetEntity
import com.example.ui.components.PetCanvas
import com.example.ui.components.TopStatsHeader
import com.example.ui.pet2d.Pet2DView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EnergyOrange
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.HealthRed
import com.example.ui.theme.LiquidGlassCapsule
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SoundManager

@Composable
fun ProfileScreen(
    pet: PetEntity?,
    progress: GameProgressEntity?,
    missions: List<DailyMissionEntity>,
    achievements: List<AchievementEntity>,
    onClaimMission: (DailyMissionEntity) -> Unit,
    onClaimAchievement: (AchievementEntity) -> Unit,
    onUpdateSettings: (sound: Boolean, haptics: Boolean, notifications: Boolean) -> Unit,
    onResetGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (pet == null) return
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Passport 📸", "Quests 📅", "Badges 🏆", "Settings ⚙️")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (progress != null) {
            TopStatsHeader(
                coins = progress.coins,
                level = progress.level,
                xp = progress.xp,
                xpRequired = progress.xpForNextLevel(),
                streakDays = progress.streakDays
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = NeonPurple,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        SoundManager.playTap()
                        selectedTab = index
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) NeonPurpleLight else TextSecondary
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> PassportTab(pet = pet, progress = progress, achievements = achievements)
            1 -> QuestsTab(
                progress = progress,
                missions = missions,
                onClaimMission = {
                    SoundManager.playCoin()
                    onClaimMission(it)
                }
            )
            2 -> AchievementsTab(
                achievements = achievements,
                onClaimAchievement = {
                    SoundManager.playLevelUp()
                    onClaimAchievement(it)
                }
            )
            3 -> SettingsTab(
                progress = progress,
                onUpdateSettings = onUpdateSettings,
                onRequestReset = { showResetDialog = true }
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "Reset Game Data?", color = TextPrimary) },
            text = {
                Text(
                    text = "Are you sure you want to reset all pets, level, coins, and items back to starter defaults? This cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetGame()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthRed)
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

// ---------------------------------------------------------------------------
// 1. PET PASSPORT & SHARE TAB
// ---------------------------------------------------------------------------
@Composable
fun PassportTab(
    pet: PetEntity,
    progress: GameProgressEntity?,
    achievements: List<AchievementEntity>
) {
    val context = LocalContext.current
    val daysTogether = (System.currentTimeMillis() - pet.adoptedDate) / (1000 * 60 * 60 * 24) + 1
    val unlockedCount = achievements.count { it.isUnlocked }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High polish Pet ID Passport Card (Liquid Glass)
        LiquidGlassCard(
            shape = RoundedCornerShape(30.dp),
            accentBorderColor = NeonPurpleLight,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pet_passport_card")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header of the card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🦊", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FOXYPET PASSPORT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonMint,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "ID: #0928-${pet.id.uppercase()}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pet Avatar Render inside Passport
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x35140A30))
                        .border(1.5.dp, Color(0x40FFFFFF), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Pet2DView(
                        pet = pet,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "${pet.type.displayName} Companion • Level ${progress?.level ?: 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonPurpleLight,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0x3512082C))
                        .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0x30FFFFFF)), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "DAYS", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(text = "$daysTogether d", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "MOOD", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(text = pet.calculateMood().emoji, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "BADGES", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(text = "$unlockedCount / ${achievements.size}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = GoldCoin)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "🐾 Meet my virtual pet ${pet.name}! A Level ${progress?.level ?: 1} ${pet.type.displayName} on FoxyPet. We've been companions for $daysTogether days with $unlockedCount unlocked achievements! 🦊✨"
                    )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share My Pet"))
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("share_pet_button")
        ) {
            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Share My Pet Card 📸", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ---------------------------------------------------------------------------
// 2. DAILY QUESTS & STREAK TAB
// ---------------------------------------------------------------------------
@Composable
fun QuestsTab(
    progress: GameProgressEntity?,
    missions: List<DailyMissionEntity>,
    onClaimMission: (DailyMissionEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 7-Day Login Streak Calendar (Liquid Glass)
        LiquidGlassCard(
            shape = RoundedCornerShape(26.dp),
            accentBorderColor = EnergyOrange.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "7-Day Login Streak 🔥", fontWeight = FontWeight.Black, color = TextPrimary)
                    Text(text = "${progress?.streakDays ?: 1} Days", color = EnergyOrange, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentStreak = progress?.streakDays ?: 1
                    for (day in 1..7) {
                        val isChecked = day <= currentStreak
                        val isToday = day == currentStreak
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isToday) NeonPurple else if (isChecked) NeonMint.copy(alpha = 0.25f) else Color(0x35140A30)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isToday) NeonPurpleLight else if (isChecked) NeonMint else Color(0x35FFFFFF),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isChecked) "✓" else "D$day",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) NeonMint else TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${day * 25}🪙",
                                fontSize = 9.sp,
                                color = GoldCoin,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Daily Missions (Resets Midnight)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(missions) { mission ->
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    accentBorderColor = if (mission.isCompleted) NeonMint.copy(alpha = 0.5f) else DarkBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mission.title,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = mission.description,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${mission.currentProgress} / ${mission.target}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonMint
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "🪙 +${mission.rewardCoins}", fontSize = 11.sp, color = GoldCoin, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "⭐ +${mission.rewardXp} XP", fontSize = 11.sp, color = NeonPurpleLight, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (mission.isClaimed) {
                            Text(text = "Claimed ✓", fontSize = 12.sp, color = NeonMint, fontWeight = FontWeight.Bold)
                        } else {
                            Button(
                                onClick = { onClaimMission(mission) },
                                enabled = mission.isCompleted,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (mission.isCompleted) NeonPurple else DarkSurfaceElevated
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (mission.isCompleted) Color(0x60FFFFFF) else Color.Transparent),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = if (mission.isCompleted) "Claim" else "In Progress",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. ACHIEVEMENTS TAB
// ---------------------------------------------------------------------------
@Composable
fun AchievementsTab(
    achievements: List<AchievementEntity>,
    onClaimAchievement: (AchievementEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(achievements) { ach ->
            LiquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                accentBorderColor = if (ach.isUnlocked) NeonMint.copy(alpha = 0.6f) else DarkBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (ach.isUnlocked) GoldCoin.copy(alpha = 0.2f) else Color(0x35140A30))
                            .border(androidx.compose.foundation.BorderStroke(1.dp, if (ach.isUnlocked) GoldCoin else Color(0x30FFFFFF)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = ach.iconEmoji, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ach.title,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = ach.description,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Text(text = "🪙 +${ach.rewardCoins}", fontSize = 11.sp, color = GoldCoin, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "⭐ +${ach.rewardXp} XP", fontSize = 11.sp, color = NeonPurpleLight, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (ach.isClaimed) {
                        Text(text = "Completed ✓", fontSize = 11.sp, color = NeonMint, fontWeight = FontWeight.Bold)
                    } else if (ach.isUnlocked) {
                        Button(
                            onClick = { onClaimAchievement(ach) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x60FFFFFF)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(text = "Claim", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "${ach.currentProgress}/${ach.targetProgress}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// 4. SETTINGS TAB
// ---------------------------------------------------------------------------
@Composable
fun SettingsTab(
    progress: GameProgressEntity?,
    onUpdateSettings: (sound: Boolean, haptics: Boolean, notifications: Boolean) -> Unit,
    onRequestReset: () -> Unit
) {
    var sound by remember { mutableStateOf(progress?.soundEnabled ?: true) }
    var haptics by remember { mutableStateOf(progress?.hapticsEnabled ?: true) }
    var notifications by remember { mutableStateOf(progress?.notificationsEnabled ?: true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        LiquidGlassCard(
            shape = RoundedCornerShape(26.dp),
            accentBorderColor = NeonPurpleLight.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Sound Effects", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(text = "Pet reactions & arcade sounds", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = sound,
                        onCheckedChange = {
                            sound = it
                            SoundManager.setSoundEnabled(it)
                            onUpdateSettings(sound, haptics, notifications)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonMint, checkedTrackColor = NeonPurple)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Haptic Feedback Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Haptic Vibration", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(text = "Vibrate on petting and mini-games", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = haptics,
                        onCheckedChange = {
                            haptics = it
                            onUpdateSettings(sound, haptics, notifications)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonMint, checkedTrackColor = NeonPurple)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notifications Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Care Reminders", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(text = "Daily reward & hunger reminders", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = notifications,
                        onCheckedChange = {
                            notifications = it
                            onUpdateSettings(sound, haptics, notifications)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonMint, checkedTrackColor = NeonPurple)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Display & Performance Setting
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Animation & Performance", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(text = "Target 60 FPS • Smooth 2.5D layered animation", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    var selectedMode by remember { mutableStateOf("Smooth 60 FPS ✨") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Battery Saver 🔋", "Smooth 60 FPS ✨").forEach { label ->
                            val isSelected = selectedMode == label
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) NeonPurple else DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonPurpleLight else DarkBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clickable {
                                        SoundManager.playTap()
                                        selectedMode = label
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About & Game Info
        LiquidGlassCard(
            shape = RoundedCornerShape(26.dp),
            accentBorderColor = NeonPurpleLight.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "About FoxyPet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version 1.0.0 • Offline-first Virtual Pet Simulation. Built with Kotlin and Jetpack Compose Liquid Glass Theme.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        SoundManager.playTap()
                        onRequestReset()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HealthRed.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                ) {
                    Text(text = "Reset Game Data ⚠️", color = HealthRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
