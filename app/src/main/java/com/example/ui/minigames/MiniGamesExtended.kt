package com.example.ui.minigames

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.sin
import kotlin.random.Random

// =============================================================================
// 6. SKY HOP GAME (Flappy Pet Style Sky Adventure)
// =============================================================================
data class SkyRing(
    var x: Float,
    val gapY: Float,
    val gapHeight: Float = 160f,
    var isPassed: Boolean = false
)

@Composable
fun SkyHopGame(
    onGameFinished: (coins: Int, xp: Int) -> Unit,
    onClose: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    var petY by remember { mutableFloatStateOf(300f) }
    var petVelocity by remember { mutableFloatStateOf(0f) }
    val rings = remember { mutableStateListOf<SkyRing>() }

    // Start / Restart game
    fun startNewGame() {
        petY = 300f
        petVelocity = 0f
        score = 0
        rings.clear()
        rings.add(SkyRing(x = 600f, gapY = 280f))
        rings.add(SkyRing(x = 950f, gapY = 360f))
        rings.add(SkyRing(x = 1300f, gapY = 220f))
        isGameOver = false
        isPlaying = true
    }

    // Physics Loop
    LaunchedEffect(isPlaying, isGameOver) {
        if (isPlaying && !isGameOver) {
            var lastNanos = 0L
            while (isPlaying && !isGameOver) {
                withFrameNanos { nowNanos ->
                    if (lastNanos != 0L) {
                        val dt = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.04f)

                        // Gravity & Velocity
                        petVelocity += 820f * dt
                        petY += petVelocity * dt

                        // Boundary check
                        if (petY < 30f || petY > 680f) {
                            SoundManager.playFail()
                            isGameOver = true
                        }

                        // Move rings
                        val ringSpeed = 190f * dt
                        for (i in 0 until rings.size) {
                            val ring = rings[i]
                            ring.x -= ringSpeed

                            // Check score passing
                            if (!ring.isPassed && ring.x < 140f) {
                                ring.isPassed = true
                                score++
                                SoundManager.playCoin()
                            }

                            // Collision check (Pet radius ~24)
                            val petX = 140f
                            if (abs(ring.x - petX) < 32f) {
                                if (petY < ring.gapY - ring.gapHeight / 2f + 16f ||
                                    petY > ring.gapY + ring.gapHeight / 2f - 16f
                                ) {
                                    SoundManager.playFail()
                                    isGameOver = true
                                }
                            }
                        }

                        // Recycle off-screen rings
                        if (rings.isNotEmpty() && rings.first().x < -80f) {
                            val rightmostX = rings.maxOf { it.x }
                            val newGapY = (180..480).random().toFloat()
                            rings.removeAt(0)
                            rings.add(SkyRing(x = rightmostX + 350f, gapY = newGapY))
                        }
                    }
                    lastNanos = nowNanos
                }
            }
        }
    }

    // Hard economy reward: 1 coin per 2 rings passed, between 4 and 16 coins
    val earnedCoins = (score / 2).coerceIn(4, 16)
    val earnedXp = (10 + score * 2).coerceIn(10, 30)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("sky_hop_close_btn")) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Text(
                text = "Sky Hop ☁️",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Score: $score ⭐",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GoldCoin
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isGameOver) {
            GameResultView(
                title = "Sky Hop Finished! 🌤️",
                subtitle = "You cleared $score cloud rings in the sky!",
                coins = earnedCoins,
                xp = earnedXp,
                onClaim = { onGameFinished(earnedCoins, earnedXp) }
            )
        } else if (!isPlaying) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "🦊 ☁️ ✨", fontSize = 52.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Flap & Soar the Skies!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap to hop upward. Pass through glowing star rings without bumping cloud pillars!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { startNewGame() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("sky_hop_start_btn")
            ) {
                Text(text = "TAKE FLIGHT! 🚀", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            // Interactive Sky Hop Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F0B26), Color(0xFF1B0E3E), Color(0xFF0D061E))
                        )
                    )
                    .border(2.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                petVelocity = -360f
                                SoundManager.playJump()
                            }
                        )
                    }
                    .testTag("sky_hop_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw Cloud Rings
                    rings.forEach { ring ->
                        val topHeight = ring.gapY - ring.gapHeight / 2f
                        val bottomY = ring.gapY + ring.gapHeight / 2f
                        val bottomHeight = h - bottomY

                        // Top Cloud Pillar
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(Color(0xFF38155C), Color(0xFF5B21B6))),
                            topLeft = Offset(ring.x - 24f, 0f),
                            size = Size(48f, topHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                        )
                        // Bottom Cloud Pillar
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(Color(0xFF5B21B6), Color(0xFF38155C))),
                            topLeft = Offset(ring.x - 24f, bottomY),
                            size = Size(48f, bottomHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                        )
                        // Glowing Portal Ring in the Gap
                        drawOval(
                            brush = Brush.radialGradient(listOf(NeonMint, Color(0x0000F5D4))),
                            topLeft = Offset(ring.x - 28f, ring.gapY - ring.gapHeight / 2f),
                            size = Size(56f, ring.gapHeight),
                            style = Stroke(width = 3.5f)
                        )
                        // Star in center of gap
                        drawCircle(GoldCoin, radius = 7f, center = Offset(ring.x, ring.gapY))
                    }

                    // Draw Pet Hero Avatar
                    val petX = 140f
                    // Wing glow
                    drawCircle(NeonPurpleLight.copy(alpha = 0.45f), radius = 32f, center = Offset(petX, petY))
                    // Pet body
                    drawCircle(Color(0xFFFF6D3B), radius = 22f, center = Offset(petX, petY))
                    // Pet cream muzzle
                    drawCircle(Color(0xFFFFFBF5), radius = 10f, center = Offset(petX + 8f, petY + 4f))
                    // Shiny eye
                    drawCircle(Color(0xFF130924), radius = 4f, center = Offset(petX + 6f, petY - 4f))
                    drawCircle(Color.White, radius = 1.5f, center = Offset(petX + 5f, petY - 5f))
                    // Foxy ear
                    val ear = Path().apply {
                        moveTo(petX - 8f, petY - 14f)
                        lineTo(petX - 2f, petY - 32f)
                        lineTo(petX + 6f, petY - 14f)
                        close()
                    }
                    drawPath(ear, Color(0xFFFF6D3B))
                    drawCircle(Color(0xFF2C1810), radius = 3.5f, center = Offset(petX - 2f, petY - 30f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tap screen to flap & hop!",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}

// =============================================================================
// 7. SNACK WHACK GAME (Whack-a-Mole Style Treat Catching)
// =============================================================================
data class WhackMoleHole(
    val id: Int,
    val itemEmoji: String, // "🥩", "🍎", "🐟", "🍓", "🌶️" (Chili = bad!)
    val isChili: Boolean = false,
    var isHit: Boolean = false
)

@Composable
fun SnackWhackGame(
    onGameFinished: (coins: Int, xp: Int) -> Unit,
    onClose: () -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var secondsLeft by remember { mutableIntStateOf(25) }
    var isGameOver by remember { mutableStateOf(false) }

    // 9 holes state
    val holes = remember { mutableStateListOf<WhackMoleHole?>().apply { repeat(9) { add(null) } } }

    val treats = listOf(
        Pair("🥩", false),
        Pair("🍎", false),
        Pair("🐟", false),
        Pair("🍓", false),
        Pair("🦴", false),
        Pair("🧁", false),
        Pair("🌶️", true) // Spicy bomb!
    )

    // Game Timer Loop
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (secondsLeft > 0 && lives > 0) {
                delay(1000)
                secondsLeft--
            }
            isGameOver = true
            if (lives > 0) SoundManager.playSuccess() else SoundManager.playFail()
        }
    }

    // Treat Spawning Loop
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (secondsLeft > 0 && lives > 0) {
                // Randomly spawn 1 or 2 treats
                val emptyIndices = holes.indices.filter { holes[it] == null }
                if (emptyIndices.isNotEmpty()) {
                    val pickIndex = emptyIndices.random()
                    val treat = treats.random()
                    holes[pickIndex] = WhackMoleHole(
                        id = pickIndex,
                        itemEmoji = treat.first,
                        isChili = treat.second
                    )
                }

                delay((500..850).random().toLong())

                // Auto hide treats
                for (i in holes.indices) {
                    if (holes[i] != null && Random.nextFloat() > 0.45f) {
                        holes[i] = null
                    }
                }
            }
        }
    }

    // Hard economy: 1 coin per 30 points (max 16 coins)
    val earnedCoins = (score / 30).coerceIn(4, 16)
    val earnedXp = (12 + score / 15).coerceIn(12, 35)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("snack_whack_close_btn")) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Text(
                text = "Snack Whack 🥩",
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
            Text(text = "Time: ${secondsLeft}s", color = NeonMint, fontWeight = FontWeight.Bold)
            Text(text = "Score: $score pts", color = GoldCoin, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isGameOver) {
            GameResultView(
                title = if (lives <= 0) "Too Spicy! 🌶️" else "Feast Complete! 🍽️",
                subtitle = "You collected $score points of delicious snacks!",
                coins = earnedCoins,
                xp = earnedXp,
                onClaim = { onGameFinished(earnedCoins, earnedXp) }
            )
        } else {
            // 3x3 Holes Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("snack_whack_grid")
            ) {
                itemsIndexed(holes) { index, hole ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable(enabled = hole != null && !hole.isHit) {
                                if (hole != null) {
                                    if (hole.isChili) {
                                        SoundManager.playFail()
                                        lives--
                                        holes[index] = null
                                    } else {
                                        SoundManager.playEat()
                                        score += 15
                                        holes[index] = null
                                    }
                                }
                            }
                            .testTag("whack_hole_$index"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (hole?.isChili == true) Color(0xFF450A0A) else DarkSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (hole?.isChili == true) HealthRed else if (hole != null) NeonMint else DarkBorder
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hole != null) {
                                Text(
                                    text = hole.itemEmoji,
                                    fontSize = 42.sp,
                                    modifier = Modifier.scale(if (hole.isChili) 1.15f else 1.0f)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurfaceElevated)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Tap yummy snacks! Avoid spicy chilis 🌶️!",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}

// =============================================================================
// 8. COLOR RHYTHM GAME (Simon Sequence / Neon Memory)
// =============================================================================
@Composable
fun ColorRhythmGame(
    onGameFinished: (coins: Int, xp: Int) -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var sequenceLength by remember { mutableIntStateOf(1) }
    var currentRound by remember { mutableIntStateOf(1) }
    var isShowingSequence by remember { mutableStateOf(false) }
    var activeFlashIndex by remember { mutableStateOf<Int?>(null) }
    var isGameOver by remember { mutableStateOf(false) }

    val masterSequence = remember { mutableStateListOf<Int>() }
    val playerInputs = remember { mutableStateListOf<Int>() }

    fun playNextRound() {
        coroutineScope.launch {
            playerInputs.clear()
            masterSequence.add((0..3).random())
            isShowingSequence = true

            delay(600)
            for (step in masterSequence) {
                activeFlashIndex = step
                when (step) {
                    0 -> SoundManager.playTap()
                    1 -> SoundManager.playCoin()
                    2 -> SoundManager.playPet()
                    3 -> SoundManager.playClean()
                }
                delay(450)
                activeFlashIndex = null
                delay(200)
            }
            isShowingSequence = false
        }
    }

    LaunchedEffect(Unit) {
        masterSequence.clear()
        playNextRound()
    }

    fun handlePadPress(padIndex: Int) {
        if (isShowingSequence || isGameOver) return

        playerInputs.add(padIndex)
        val expectedIndex = masterSequence[playerInputs.size - 1]

        if (padIndex == expectedIndex) {
            when (padIndex) {
                0 -> SoundManager.playTap()
                1 -> SoundManager.playCoin()
                2 -> SoundManager.playPet()
                3 -> SoundManager.playClean()
            }
            // Check if finished sequence for this round
            if (playerInputs.size == masterSequence.size) {
                SoundManager.playMatch()
                currentRound++
                coroutineScope.launch {
                    delay(500)
                    playNextRound()
                }
            }
        } else {
            // Mistake
            SoundManager.playFail()
            isGameOver = true
        }
    }

    // Hard economy: 2 coins per completed sequence round (between 5 and 18 coins)
    val earnedCoins = ((currentRound - 1) * 2 + 3).coerceIn(4, 18)
    val earnedXp = ((currentRound - 1) * 4 + 10).coerceIn(10, 35)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("rhythm_close_btn")) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Text(
                text = "Color Rhythm 🎵",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Round: $currentRound",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NeonMint
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isGameOver) {
            GameResultView(
                title = "Beat Dropped! 🎵",
                subtitle = "You reached Round $currentRound in the rhythm sequence!",
                coins = earnedCoins,
                xp = earnedXp,
                onClaim = { onGameFinished(earnedCoins, earnedXp) }
            )
        } else {
            Text(
                text = if (isShowingSequence) "👀 Listen & Watch Pattern..." else "👉 Repeat the Pattern!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isShowingSequence) GoldCoin else NeonMint
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 4 Neon Pads Layout (Top, Right, Bottom, Left)
            Box(
                modifier = Modifier
                    .size(290.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .border(2.dp, DarkBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Pad 0: Top (Neon Violet)
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .clip(CircleShape)
                        .background(if (activeFlashIndex == 0) Color.White else NeonPurple)
                        .clickable(enabled = !isShowingSequence) { handlePadPress(0) }
                        .testTag("rhythm_pad_0")
                )

                // Pad 1: Right (Neon Mint)
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .clip(CircleShape)
                        .background(if (activeFlashIndex == 1) Color.White else NeonMint)
                        .clickable(enabled = !isShowingSequence) { handlePadPress(1) }
                        .testTag("rhythm_pad_1")
                )

                // Pad 2: Bottom (Gold Coin)
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(CircleShape)
                        .background(if (activeFlashIndex == 2) Color.White else GoldCoin)
                        .clickable(enabled = !isShowingSequence) { handlePadPress(2) }
                        .testTag("rhythm_pad_2")
                )

                // Pad 3: Left (Neon Pink)
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .clip(CircleShape)
                        .background(if (activeFlashIndex == 3) Color.White else NeonPink)
                        .clickable(enabled = !isShowingSequence) { handlePadPress(3) }
                        .testTag("rhythm_pad_3")
                )

                // Center Hub Badge
                Surface(
                    shape = CircleShape,
                    color = DarkBackground,
                    border = androidx.compose.foundation.BorderStroke(2.dp, DarkBorder),
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🎵", fontSize = 28.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Progress: ${playerInputs.size} / ${masterSequence.size} notes",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
