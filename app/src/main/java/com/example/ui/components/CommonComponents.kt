package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.SoundManager
import androidx.compose.ui.window.Dialog
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

@Composable
fun StatBar(
    name: String,
    value: Float, // 0 - 100
    iconEmoji: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (value / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "statBarProgress"
    )

    val isLow = value < 30f
    val infiniteTransition = rememberInfiniteTransition(label = "pulseLow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = iconEmoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLow) HealthRed else TextSecondary,
                    fontWeight = if (isLow) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "${value.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = if (isLow) HealthRed.copy(alpha = pulseAlpha) else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        // Liquid Glass Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0x40140A2E))
                .border(BorderStroke(1.dp, Color(0x35FFFFFF)), RoundedCornerShape(5.dp))
        ) {
            if (animatedProgress > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    barColor.copy(alpha = 0.85f),
                                    barColor,
                                    barColor.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .drawWithContent {
                            drawContent()
                            // Specular Liquid Gloss
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
}

@Composable
fun TopStatsHeader(
    coins: Int,
    level: Int,
    xp: Int,
    xpRequired: Int,
    streakDays: Int,
    onStreakClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Level & XP Liquid Glass Capsule
        LiquidGlassCapsule(
            shape = RoundedCornerShape(22.dp),
            accentBorderColor = NeonPurpleLight.copy(alpha = 0.45f),
            backgroundColor = DarkSurfaceVariant,
            modifier = Modifier.height(40.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NeonPurple, NeonPurpleLight)))
                        .border(BorderStroke(1.dp, Color(0x70FFFFFF)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lv$level",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "$xp / $xpRequired XP",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0x6013092E))
                            .border(BorderStroke(0.5.dp, Color(0x35FFFFFF)), RoundedCornerShape(3.dp))
                    ) {
                        val progress = if (xpRequired > 0) (xp.toFloat() / xpRequired).coerceIn(0f, 1f) else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(NeonMint.copy(alpha = 0.8f), NeonMint)
                                    )
                                )
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Streak Flame Liquid Glass Capsule
            LiquidGlassCapsule(
                shape = RoundedCornerShape(22.dp),
                accentBorderColor = EnergyOrange.copy(alpha = 0.5f),
                backgroundColor = DarkSurfaceVariant,
                onClick = onStreakClicked,
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔥", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streakDays d",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EnergyOrange
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Coins Liquid Glass Capsule
            LiquidGlassCapsule(
                shape = RoundedCornerShape(22.dp),
                accentBorderColor = GoldCoin.copy(alpha = 0.5f),
                backgroundColor = DarkSurfaceVariant,
                onClick = { SoundManager.playCoin() },
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coins",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldCoin
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Sound Effects Mute/Unmute Liquid Glass Orb
            val isSoundOn by SoundManager.isSoundEnabled.collectAsStateWithLifecycle()
            LiquidGlassCapsule(
                shape = CircleShape,
                accentBorderColor = if (isSoundOn) NeonMint.copy(alpha = 0.5f) else DarkBorder,
                backgroundColor = DarkSurfaceVariant,
                onClick = { SoundManager.toggleSound() },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("sound_toggle_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = if (isSoundOn) "🔊" else "🔇", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun LevelUpDialog(
    newLevel: Int,
    rewardCoins: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            shape = RoundedCornerShape(30.dp),
            accentBorderColor = NeonPurpleLight,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("level_up_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎉 ⭐ 🎊", fontSize = 34.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "LEVEL UP!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = GoldCoin
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your companion reached Level $newLevel!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                LiquidGlassCapsule(
                    shape = RoundedCornerShape(18.dp),
                    accentBorderColor = NeonMint.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Level Rewards", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+$rewardCoins 🪙 Coins & New Shop Items!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonMint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0x60FFFFFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("level_up_confirm_button")
                ) {
                    Text(
                        text = "Awesome!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
