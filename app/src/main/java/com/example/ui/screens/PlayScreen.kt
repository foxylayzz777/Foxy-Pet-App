package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameProgressEntity
import com.example.data.model.PetEntity
import com.example.ui.components.TopStatsHeader
import com.example.ui.minigames.CoinCatcherGame
import com.example.ui.minigames.ColorRhythmGame
import com.example.ui.minigames.GuessEmojiGame
import com.example.ui.minigames.MemoryMatchGame
import com.example.ui.minigames.ReactionTapGame
import com.example.ui.minigames.SkyHopGame
import com.example.ui.minigames.SnackWhackGame
import com.example.ui.minigames.TapFrenzyGame
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldCoin
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

enum class ActiveMiniGame {
    NONE, MEMORY_MATCH, REACTION_TAP, COIN_CATCHER, TAP_FRENZY, GUESS_EMOJI, SKY_HOP, SNACK_WHACK, COLOR_RHYTHM
}

data class GameCardItem(
    val type: ActiveMiniGame,
    val title: String,
    val description: String,
    val emoji: String,
    val maxRewardCoins: Int,
    val gradientColors: List<Color>
)

@Composable
fun PlayScreen(
    pet: PetEntity?,
    progress: GameProgressEntity?,
    onGameRewardEarned: (coins: Int, xp: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeGame by remember { mutableStateOf(ActiveMiniGame.NONE) }

    // Handle back button when playing a mini-game
    BackHandler(enabled = activeGame != ActiveMiniGame.NONE) {
        activeGame = ActiveMiniGame.NONE
    }

    when (activeGame) {
        ActiveMiniGame.MEMORY_MATCH -> {
            MemoryMatchGame(
                onGameFinished = { coins, xp ->
                    onGameRewardEarned(coins, xp)
                    activeGame = ActiveMiniGame.NONE
                },
                onClose = { activeGame = ActiveMiniGame.NONE }
            )
        }
        ActiveMiniGame.REACTION_TAP -> {
            ReactionTapGame(
                onGameFinished = { coins, xp ->
                    onGameRewardEarned(coins, xp)
                    activeGame = ActiveMiniGame.NONE
                },
                onClose = { activeGame = ActiveMiniGame.NONE }
            )
        }
        ActiveMiniGame.COIN_CATCHER -> {
            CoinCatcherGame(
                onGameFinished = { coins, xp ->
                    onGameRewardEarned(coins, xp)
                    activeGame = ActiveMiniGame.NONE
                },
                onClose = { activeGame = ActiveMiniGame.NONE }
            )
        }
        ActiveMiniGame.TAP_FRENZY -> {
            TapFrenzyGame(
                onGameFinished = { coins, xp ->
                    onGameRewardEarned(coins, xp)
                    activeGame = ActiveMiniGame.NONE
                },
                onClose = { activeGame = ActiveMiniGame.NONE }
            )
        }
        ActiveMiniGame.GUESS_EMOJI -> {
            GuessEmojiGame(
                onGameFinished = { coins, xp ->
                    onGameRewardEarned(coins, xp)
                    activeGame = ActiveMiniGame.NONE
                },
                onClose = { activeGame = ActiveMiniGame.NONE }
            )
        }
        ActiveMiniGame.SKY_HOP -> {
            SkyHopGame(
                onGameFinished = { coins, xp ->
                    onGameRewardEarned(coins, xp)
                    activeGame = ActiveMiniGame.NONE
                },
                onClose = { activeGame = ActiveMiniGame.NONE }
            )
        }
        ActiveMiniGame.SNACK_WHACK -> {
            SnackWhackGame(
                onGameFinished = { coins, xp ->
                    onGameRewardEarned(coins, xp)
                    activeGame = ActiveMiniGame.NONE
                },
                onClose = { activeGame = ActiveMiniGame.NONE }
            )
        }
        ActiveMiniGame.COLOR_RHYTHM -> {
            ColorRhythmGame(
                onGameFinished = { coins, xp ->
                    onGameRewardEarned(coins, xp)
                    activeGame = ActiveMiniGame.NONE
                },
                onClose = { activeGame = ActiveMiniGame.NONE }
            )
        }
        ActiveMiniGame.NONE -> {
            val gamesList = listOf(
                GameCardItem(
                    type = ActiveMiniGame.SKY_HOP,
                    title = "Sky Hop",
                    description = "Tap to hop & flap through floating cloud star rings!",
                    emoji = "☁️",
                    maxRewardCoins = 16,
                    gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                ),
                GameCardItem(
                    type = ActiveMiniGame.SNACK_WHACK,
                    title = "Snack Whack",
                    description = "Whack tasty treats popping out of holes! Dodge spicy chilis!",
                    emoji = "🥩",
                    maxRewardCoins = 16,
                    gradientColors = listOf(Color(0xFFF97316), Color(0xFFEA580C))
                ),
                GameCardItem(
                    type = ActiveMiniGame.COLOR_RHYTHM,
                    title = "Color Rhythm",
                    description = "Follow and repeat the musical glowing neon color sequences!",
                    emoji = "🎵",
                    maxRewardCoins = 18,
                    gradientColors = listOf(NeonPink, Color(0xFFBE185D))
                ),
                GameCardItem(
                    type = ActiveMiniGame.MEMORY_MATCH,
                    title = "Memory Match",
                    description = "Flip and pair matching pet symbols. Boosts focus and reflexes!",
                    emoji = "🃏",
                    maxRewardCoins = 16,
                    gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF4C1D95))
                ),
                GameCardItem(
                    type = ActiveMiniGame.REACTION_TAP,
                    title = "Reaction Tap",
                    description = "Time your taps perfectly inside the target sweet-spot zone.",
                    emoji = "⚡",
                    maxRewardCoins = 15,
                    gradientColors = listOf(Color(0xFF059669), Color(0xFF064E3B))
                ),
                GameCardItem(
                    type = ActiveMiniGame.COIN_CATCHER,
                    title = "Coin Catcher",
                    description = "Slide your basket to catch falling coins while dodging bombs!",
                    emoji = "🪙",
                    maxRewardCoins = 18,
                    gradientColors = listOf(Color(0xFFD97706), Color(0xFF78350F))
                ),
                GameCardItem(
                    type = ActiveMiniGame.TAP_FRENZY,
                    title = "Tap Frenzy",
                    description = "10 seconds of blazing fast tapping. Test your ultimate speed!",
                    emoji = "🔥",
                    maxRewardCoins = 15,
                    gradientColors = listOf(Color(0xFFDC2626), Color(0xFF7F1D1D))
                ),
                GameCardItem(
                    type = ActiveMiniGame.GUESS_EMOJI,
                    title = "Guess the Emoji",
                    description = "Decode playful emoji clues and pet riddles for earned rewards.",
                    emoji = "🧩",
                    maxRewardCoins = 15,
                    gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1E3A8A))
                )
            )

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(DarkBackground)
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

                // Arcade Lobby Header Banner (Liquid Glass)
                LiquidGlassCard(
                    shape = RoundedCornerShape(26.dp),
                    accentBorderColor = NeonPurpleLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Brush.radialGradient(listOf(NeonPurple.copy(alpha = 0.45f), Color(0x35140A30))))
                                .border(androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x60FFFFFF)), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🕹️", fontSize = 30.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Pet Arcade Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Play mini-games to boost happiness & earn coins!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Games List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(gamesList) { game ->
                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("play_game_${game.type.name}"),
                            shape = RoundedCornerShape(22.dp),
                            accentBorderColor = game.gradientColors.first().copy(alpha = 0.5f),
                            onClick = {
                                SoundManager.playTap()
                                activeGame = game.type
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Brush.linearGradient(game.gradientColors))
                                        .border(androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x60FFFFFF)), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = game.emoji, fontSize = 28.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = game.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = game.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🪙 Up to +${game.maxRewardCoins} Coins", fontSize = 11.sp, color = GoldCoin, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "⭐ +XP", fontSize = 11.sp, color = NeonMint, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        SoundManager.playTap()
                                        activeGame = game.type
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x60FFFFFF)),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text(text = "Play", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
    }
}
