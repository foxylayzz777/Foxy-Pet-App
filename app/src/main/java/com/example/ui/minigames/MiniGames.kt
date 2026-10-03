package com.example.ui.minigames

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EnergyOrange
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.HealthRed
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

// ---------------------------------------------------------------------------
// 1. MEMORY MATCH GAME
// ---------------------------------------------------------------------------
data class MemoryCard(
    val id: Int,
    val emoji: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
fun MemoryMatchGame(
    onGameFinished: (coins: Int, xp: Int) -> Unit,
    onClose: () -> Unit
) {
    val icons = remember { listOf("🦊", "🐱", "🐶", "🍎", "🥩", "👑") }
    val cards = remember {
        mutableStateListOf<MemoryCard>().apply {
            val doubled = (icons + icons).shuffled()
            doubled.forEachIndexed { index, emoji ->
                add(MemoryCard(id = index, emoji = emoji))
            }
        }
    }

    var selectedFirst by remember { mutableStateOf<Int?>(null) }
    var selectedSecond by remember { mutableStateOf<Int?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var isChecking by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }

    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(1000)
            secondsElapsed++
        }
    }

    LaunchedEffect(selectedFirst, selectedSecond) {
        val first = selectedFirst
        val second = selectedSecond
        if (first != null && second != null) {
            isChecking = true
            moves++
            delay(600)
            if (cards[first].emoji == cards[second].emoji) {
                cards[first] = cards[first].copy(isMatched = true)
                cards[second] = cards[second].copy(isMatched = true)
                SoundManager.playMatch()
                // Check if all matched
                if (cards.all { it.isMatched }) {
                    SoundManager.playSuccess()
                    isGameOver = true
                }
            } else {
                cards[first] = cards[first].copy(isFlipped = false)
                cards[second] = cards[second].copy(isFlipped = false)
            }
            selectedFirst = null
            selectedSecond = null
            isChecking = false
        }
    }

    val earnedCoins = (8 + (24 - moves).coerceAtLeast(0)).coerceIn(6, 16)
    val earnedXp = 15

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("memory_close_btn")) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Text(
                text = "Memory Match 🃏",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${secondsElapsed}s | $moves moves",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonMint
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isGameOver) {
            GameResultView(
                title = "Puzzle Solved! 🎉",
                subtitle = "Completed in $moves moves ($secondsElapsed seconds)",
                coins = earnedCoins,
                xp = earnedXp,
                onClaim = { onGameFinished(earnedCoins, earnedXp) }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("memory_grid")
            ) {
                itemsIndexed(cards) { index, card ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable(enabled = !card.isFlipped && !card.isMatched && !isChecking) {
                                cards[index] = card.copy(isFlipped = true)
                                if (selectedFirst == null) {
                                    selectedFirst = index
                                } else {
                                    selectedSecond = index
                                }
                            }
                            .testTag("memory_card_$index"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (card.isFlipped || card.isMatched) DarkSurfaceElevated else DarkSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (card.isMatched) NeonMint else if (card.isFlipped) NeonPurpleLight else DarkBorder
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (card.isFlipped || card.isMatched) {
                                Text(text = card.emoji, fontSize = 34.sp)
                            } else {
                                Text(text = "🐾", fontSize = 26.sp, color = NeonPurple.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2. REACTION TAP GAME
// ---------------------------------------------------------------------------
@Composable
fun ReactionTapGame(
    onGameFinished: (coins: Int, xp: Int) -> Unit,
    onClose: () -> Unit
) {
    var round by remember { mutableIntStateOf(1) }
    val maxRounds = 5
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("Tap when in green!") }
    var isGameOver by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "indicatorOscillation")
    val position by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900 - (round * 60), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sliderPos"
    )

    fun onCatchTapped() {
        if (isGameOver) return
        val sweetSpotCenter = 0.5f
        val distance = abs(position - sweetSpotCenter)
        if (distance < 0.08f) {
            score += 20 + streak * 5
            streak++
            SoundManager.playSuccess()
            feedbackText = "🔥 PERFECT! +${20 + (streak - 1) * 5}"
        } else if (distance < 0.16f) {
            score += 10
            streak = 1
            SoundManager.playMatch()
            feedbackText = "✨ GOOD! +10"
        } else {
            streak = 0
            SoundManager.playFail()
            feedbackText = "❌ MISSED!"
        }

        if (round >= maxRounds) {
            isGameOver = true
        } else {
            round++
        }
    }

    val earnedCoins = (score / 12).coerceIn(4, 15)
    val earnedXp = (10 + score / 8).coerceIn(10, 25)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Text(
                text = "Reaction Tap ⚡",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Score: $score",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GoldCoin
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isGameOver) {
            GameResultView(
                title = "Round Complete! ⚡",
                subtitle = "Total Score: $score points",
                coins = earnedCoins,
                xp = earnedXp,
                onClaim = { onGameFinished(earnedCoins, earnedXp) }
            )
        } else {
            Text(
                text = "Round $round / $maxRounds",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = feedbackText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (feedbackText.contains("PERFECT")) GoldCoin else if (feedbackText.contains("GOOD")) NeonMint else NeonPink
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Reaction Bar Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(DarkSurfaceElevated)
                    .border(2.dp, DarkBorder, RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                // Sweet spot zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.24f)
                        .height(60.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonMint.copy(alpha = 0.35f))
                        .border(2.dp, NeonMint, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎯", fontSize = 16.sp)
                }

                // Moving Indicator Needle
                Box(
                    modifier = Modifier
                        .fillMaxWidth(position)
                        .height(60.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp, 50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.verticalGradient(listOf(NeonPink, Color(0xFFEF4444))))
                            .shadow(8.dp, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onCatchTapped() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("reaction_catch_btn")
            ) {
                Text(
                    text = "CATCH! 🐾",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// 3. COIN CATCHER GAME
// ---------------------------------------------------------------------------
data class FallingItem(
    val id: Long,
    var xPercent: Float,
    var yPercent: Float,
    val isBomb: Boolean,
    val emoji: String
)

@Composable
fun CoinCatcherGame(
    onGameFinished: (coins: Int, xp: Int) -> Unit,
    onClose: () -> Unit
) {
    var basketXPercent by remember { mutableFloatStateOf(0.5f) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var timeLeft by remember { mutableIntStateOf(25) }
    var isGameOver by remember { mutableStateOf(false) }

    val items = remember { mutableStateListOf<FallingItem>() }
    var itemIdCounter by remember { mutableStateOf(0L) }

    // Game loop
    LaunchedEffect(isGameOver) {
        while (!isGameOver && timeLeft > 0) {
            delay(1000)
            timeLeft--
            if (timeLeft <= 0) {
                isGameOver = true
            }
        }
    }

    // Spawn items
    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay((400..700).random().toLong())
            val isBomb = Random.nextFloat() < 0.22f
            items.add(
                FallingItem(
                    id = itemIdCounter++,
                    xPercent = Random.nextFloat().coerceIn(0.1f, 0.9f),
                    yPercent = 0f,
                    isBomb = isBomb,
                    emoji = if (isBomb) "💣" else if (Random.nextBoolean()) "🪙" else "💎"
                )
            )
        }
    }

    // Fall animation loop
    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(30)
            val toRemove = mutableListOf<FallingItem>()
            for (i in 0 until items.size) {
                val item = items[i]
                item.yPercent += 0.024f

                // Check catch by basket near bottom (yPercent ~ 0.85)
                if (item.yPercent in 0.80f..0.92f && abs(item.xPercent - basketXPercent) < 0.14f) {
                    if (item.isBomb) {
                        SoundManager.playFail()
                        lives--
                        if (lives <= 0) isGameOver = true
                    } else {
                        SoundManager.playCoin()
                        score += if (item.emoji == "💎") 3 else 1
                    }
                    toRemove.add(item)
                } else if (item.yPercent > 1.0f) {
                    toRemove.add(item)
                }
            }
            items.removeAll(toRemove)
        }
    }

    val earnedCoins = score.coerceIn(4, 18)
    val earnedXp = (10 + score * 2).coerceIn(10, 30)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Text(
                text = "Coin Catcher 🪙",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row {
                repeat(lives) { Text(text = "❤️", fontSize = 14.sp) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Time: ${timeLeft}s", color = NeonMint, fontWeight = FontWeight.Bold)
            Text(text = "Caught: $score", color = GoldCoin, fontWeight = FontWeight.Bold)
        }

        if (isGameOver) {
            GameResultView(
                title = if (lives <= 0) "Ouch! Watch for bombs! 💥" else "Great Catch! 🪙",
                subtitle = "You collected $score treasure items!",
                coins = earnedCoins,
                xp = earnedXp,
                onClaim = { onGameFinished(earnedCoins, earnedXp) }
            )
        } else {
            // Interactive falling playfield
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurface)
                    .border(2.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            basketXPercent = (change.position.x / size.width).coerceIn(0.1f, 0.9f)
                        }
                    }
                    .testTag("coin_catcher_arena")
            ) {
                // Falling items
                items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = item.emoji,
                            fontSize = 26.sp,
                            modifier = Modifier
                                .padding(
                                    start = (item.xPercent * 300).dp,
                                    top = (item.yPercent * 480).dp
                                )
                        )
                    }
                }

                // Pet Basket Catcher
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NeonPurple,
                        modifier = Modifier
                            .padding(bottom = 24.dp, start = (basketXPercent * 280).dp)
                            .size(76.dp, 36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🧺 🐾", fontSize = 16.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "👈 Drag left/right to catch coins, dodge bombs 👉",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4. TAP FRENZY CHALLENGE
// ---------------------------------------------------------------------------
@Composable
fun TapFrenzyGame(
    onGameFinished: (coins: Int, xp: Int) -> Unit,
    onClose: () -> Unit
) {
    var taps by remember { mutableIntStateOf(0) }
    var secondsLeft by remember { mutableIntStateOf(10) }
    var isStarted by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(isStarted, isFinished) {
        if (isStarted && !isFinished) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            isFinished = true
        }
    }

    val earnedCoins = (taps / 7).coerceIn(4, 15)
    val earnedXp = (10 + taps / 4).coerceIn(10, 30)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Text(
                text = "Tap Frenzy 🔥",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${secondsLeft}s",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (secondsLeft <= 3) HealthRed else NeonMint
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isFinished) {
            GameResultView(
                title = "Frenzy Complete! 🔥",
                subtitle = "You reached $taps taps in 10 seconds!",
                coins = earnedCoins,
                xp = earnedXp,
                onClaim = { onGameFinished(earnedCoins, earnedXp) }
            )
        } else if (!isStarted) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "🐾 ⚡ 💥", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ready to test your speed?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the giant button as fast as you can in 10 seconds!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { isStarted = true },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("tap_frenzy_start_btn")
            ) {
                Text(text = "START! 🚀", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Text(
                text = "$taps",
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = GoldCoin
            )
            Text(
                text = "TAPS!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.weight(1f))

            // Giant Tap Action Button
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonPink, Color(0xFFE11D48), DarkSurfaceElevated)
                        )
                    )
                    .border(4.dp, GoldCoin, CircleShape)
                    .clickable {
                        SoundManager.playTap()
                        taps++
                    }
                    .testTag("tap_frenzy_giant_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔥", fontSize = 42.sp)
                    Text(
                        text = "TAP!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// ---------------------------------------------------------------------------
// 5. GUESS THE EMOJI GAME
// ---------------------------------------------------------------------------
data class EmojiRiddle(
    val emojis: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

@Composable
fun GuessEmojiGame(
    onGameFinished: (coins: Int, xp: Int) -> Unit,
    onClose: () -> Unit
) {
    val riddles = remember {
        listOf(
            EmojiRiddle("🦊 + 🌲 + 🌙", "Where does the sly fox wander at night?", listOf("Deep Forest", "Shopping Mall", "Swimming Pool", "Space Station"), 0),
            EmojiRiddle("🐶 + 🦴 + 🎾", "What makes a puppy happiest?", listOf("Math Homework", "Treats & Catch", "Cold Rain", "Being Ignored"), 1),
            EmojiRiddle("🐱 + 🥛 + 🧶", "Classic cozy cat hobby:", listOf("Riding a Motorcycle", "Milk & Yarn Play", "Baking Pies", "Skydiving"), 1),
            EmojiRiddle("🛁 + 🫧 + 🧼", "Time for the pet to get:", listOf("Super Muddy", "Clean & Sparkly", "Very Hungry", "Extremely Tired"), 1),
            EmojiRiddle("👑 + 🦊 + ✨", "What kind of pet is Rusty with a crown?", listOf("Royal Prince Fox", "Wild Wolf", "Baby Hamster", "Sea Turtle"), 0)
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isGameOver by remember { mutableStateOf(false) }

    val currentRiddle = riddles[currentIndex]
    val earnedCoins = (score * 3).coerceIn(3, 15)
    val earnedXp = (10 + score * 4).coerceIn(10, 30)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Text(
                text = "Guess the Emoji 🧩",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Score: $score",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GoldCoin
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isGameOver) {
            GameResultView(
                title = "Trivia Master! 🧩",
                subtitle = "You got $score / ${riddles.size} correct answers!",
                coins = earnedCoins,
                xp = earnedXp,
                onClaim = { onGameFinished(earnedCoins, earnedXp) }
            )
        } else {
            Text(
                text = "Question ${currentIndex + 1} of ${riddles.size}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = currentRiddle.emojis, fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentRiddle.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            currentRiddle.options.forEachIndexed { optIndex, optionText ->
                val isSelected = selectedOption == optIndex
                val isCorrect = optIndex == currentRiddle.correctIndex
                val cardColor = if (selectedOption != null) {
                    if (isCorrect) NeonMint.copy(alpha = 0.25f) else if (isSelected) HealthRed.copy(alpha = 0.25f) else DarkSurfaceVariant
                } else {
                    DarkSurfaceVariant
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable(enabled = selectedOption == null) {
                            selectedOption = optIndex
                            if (isCorrect) {
                                SoundManager.playMatch()
                                score++
                            } else {
                                SoundManager.playFail()
                            }
                        }
                        .testTag("emoji_option_$optIndex"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selectedOption != null && isCorrect) NeonMint else DarkBorder
                    )
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (selectedOption != null) {
                Button(
                    onClick = {
                        if (currentIndex + 1 < riddles.size) {
                            currentIndex++
                            selectedOption = null
                        } else {
                            isGameOver = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (currentIndex + 1 < riddles.size) "Next Question ➡️" else "Finish! 🏆",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared Game Result Card
// ---------------------------------------------------------------------------
@Composable
fun GameResultView(
    title: String,
    subtitle: String,
    coins: Int,
    xp: Int,
    onClaim: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(2.dp, NeonPurpleLight),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("game_result_card")
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🏆", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = GoldCoin,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceElevated)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🪙 Coins", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = "+$coins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldCoin
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "⭐ XP", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = "+$xp",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMint
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("game_claim_reward_btn")
            ) {
                Text(
                    text = "Collect Rewards! ✨",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
