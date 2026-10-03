package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameProgressEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.ui.components.TopStatsHeader
import com.example.ui.theme.CleanBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EnergyOrange
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
import kotlinx.coroutines.launch

@Composable
fun ShopScreen(
    progress: GameProgressEntity?,
    inventory: List<InventoryItemEntity>,
    onPurchaseItem: (String, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val categories = listOf(
        Pair(ItemCategory.FOOD, "Food 🍖"),
        Pair(ItemCategory.HAT, "Hats 🎩"),
        Pair(ItemCategory.GLASSES, "Glasses 🕶️"),
        Pair(ItemCategory.CLOTHES, "Outfits 👕"),
        Pair(ItemCategory.COLLAR, "Collars 🎀"),
        Pair(ItemCategory.ACCESSORY, "Accessories ✨"),
        Pair(ItemCategory.SKIN, "Skins 🎨"),
        Pair(ItemCategory.WORLD, "Worlds 🌎"),
        Pair(ItemCategory.FURNITURE, "Furniture 🛋️")
    )

    val currentCategory = categories[selectedCategoryIndex].first
    val items = inventory.filter { it.category == currentCategory }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (progress != null) {
                TopStatsHeader(
                    coins = progress.coins,
                    level = progress.level,
                    xp = progress.xp,
                    xpRequired = progress.xpForNextLevel(),
                    streakDays = progress.streakDays
                )
            }

            // Shop Banner (Liquid Glass)
            LiquidGlassCard(
                shape = RoundedCornerShape(26.dp),
                accentBorderColor = NeonPurpleLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(androidx.compose.ui.graphics.Brush.radialGradient(listOf(GoldCoin.copy(alpha = 0.40f), Color(0x35140A30))))
                            .border(androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x60FFFFFF)), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🛍️", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Pet Marketplace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "100% Free gameplay • Earn coins by caring & playing!",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonMint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                containerColor = Color.Transparent,
                contentColor = NeonPurple,
                edgePadding = 16.dp,
                divider = {}
            ) {
                categories.forEachIndexed { index, pair ->
                    Tab(
                        selected = selectedCategoryIndex == index,
                        onClick = {
                            SoundManager.playTap()
                            selectedCategoryIndex = index
                        },
                        text = {
                            Text(
                                text = pair.second,
                                fontSize = 12.sp,
                                fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategoryIndex == index) NeonPurpleLight else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shop Items Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    val userCoins = progress?.coins ?: 0
                    val canAfford = userCoins >= item.price
                    val isOwned = item.isUnlocked && item.category != ItemCategory.FOOD

                    LiquidGlassCard(
                        shape = RoundedCornerShape(22.dp),
                        accentBorderColor = if (isOwned) NeonMint else if (canAfford) GoldCoin.copy(alpha = 0.5f) else Color(0x3564748B),
                        modifier = Modifier.testTag("shop_item_${item.itemId}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0x3512082C))
                                    .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0x35FFFFFF)), RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.iconEmoji, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                maxLines = 2,
                                minLines = 2
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Stat perks or count
                            if (item.category == ItemCategory.FOOD) {
                                Text(
                                    text = "In Bag: x${item.quantity}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonMint
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Buy / Owned Button
                            if (isOwned) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(NeonMint.copy(alpha = 0.2f))
                                        .border(androidx.compose.foundation.BorderStroke(1.dp, NeonMint.copy(alpha = 0.6f)), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "OWNED ✓",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonMint
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        SoundManager.playTap()
                                        if (canAfford) {
                                            onPurchaseItem(item.itemId) { success ->
                                                if (success) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Purchased ${item.name}! 🎉")
                                                    }
                                                }
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Not enough coins! Play mini-games to earn more.")
                                            }
                                        }
                                    },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (canAfford) NeonPurple else DarkSurfaceElevated
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (canAfford) Color(0x60FFFFFF) else Color.Transparent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("buy_btn_${item.itemId}")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🪙 ${item.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (canAfford) GoldCoin else TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
        )
    }
}
