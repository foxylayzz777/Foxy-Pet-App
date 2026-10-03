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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameProgressEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.data.model.PetEntity
import com.example.data.model.PetType
import com.example.ui.components.PetCanvas
import com.example.ui.components.TopStatsHeader
import com.example.ui.pet2d.Pet2DView
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

@Composable
fun PetCareScreen(
    activePet: PetEntity?,
    allPets: List<PetEntity>,
    progress: GameProgressEntity?,
    inventory: List<InventoryItemEntity>,
    onSwitchPet: (String) -> Unit,
    onRenamePet: (String, String) -> Unit,
    onEquipItem: (petId: String, category: ItemCategory, itemId: String?) -> Unit,
    onNavigateToShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePet == null) return

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf(activePet.name) }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val closetCategories = listOf(
        Pair(ItemCategory.HAT, "Hats 🎩"),
        Pair(ItemCategory.GLASSES, "Glasses 🕶️"),
        Pair(ItemCategory.CLOTHES, "Outfits 👕"),
        Pair(ItemCategory.COLLAR, "Collars 🎀"),
        Pair(ItemCategory.ACCESSORY, "Accessories ✨"),
        Pair(ItemCategory.SKIN, "Skins 🎨")
    )

    val currentCategory = closetCategories[selectedCategoryIndex].first
    val categoryItems = inventory.filter { it.category == currentCategory }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Top stats
        if (progress != null) {
            TopStatsHeader(
                coins = progress.coins,
                level = progress.level,
                xp = progress.xp,
                xpRequired = progress.xpForNextLevel(),
                streakDays = progress.streakDays
            )
        }

        // Pet Switcher Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            allPets.forEach { petItem ->
                val isSelected = petItem.id == activePet.id
                LiquidGlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("switch_pet_${petItem.id}"),
                    shape = RoundedCornerShape(20.dp),
                    accentBorderColor = if (isSelected) NeonMint else DarkBorder,
                    onClick = {
                        SoundManager.playTap()
                        onSwitchPet(petItem.id)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = petItem.type.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = petItem.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            maxLines = 1
                        )
                        Text(
                            text = petItem.type.displayName,
                            fontSize = 10.sp,
                            color = if (isSelected) NeonMint else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Live Pet Showcase & Rename (Liquid Glass Showcase)
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(30.dp),
            accentBorderColor = NeonPurpleLight
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = activePet.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = {
                            SoundManager.playTap()
                            renameInput = activePet.name
                            showRenameDialog = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = NeonMint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "${activePet.type.trait} • ${activePet.type.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0x35140A30))
                        .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0x35FFFFFF)), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Pet2DView(
                        pet = activePet,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Closet Dressing Room Section
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            accentBorderColor = NeonPurpleLight.copy(alpha = 0.45f)
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pet Closet & Wardrobe",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    TextButton(onClick = onNavigateToShop) {
                        Text(text = "Shop More 🛒", color = NeonMint, fontSize = 12.sp)
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
                    closetCategories.forEachIndexed { index, pair ->
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

                Spacer(modifier = Modifier.height(12.dp))

                // Items list horizontally
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // "None / Unequip" option
                    item {
                        val isNoneActive = when (currentCategory) {
                            ItemCategory.HAT -> activePet.selectedHat == null
                            ItemCategory.GLASSES -> activePet.selectedGlasses == null
                            ItemCategory.CLOTHES -> activePet.selectedClothes == null
                            ItemCategory.COLLAR -> activePet.selectedCollar == null
                            ItemCategory.ACCESSORY -> activePet.selectedAccessory == null
                            ItemCategory.SKIN -> activePet.skinId == "default"
                            else -> true
                        }
                        ClosetItemCard(
                            emoji = "❌",
                            name = "None",
                            isEquipped = isNoneActive,
                            isUnlocked = true,
                            onEquip = {
                                onEquipItem(
                                    activePet.id,
                                    currentCategory,
                                    if (currentCategory == ItemCategory.SKIN) "default" else null
                                )
                            }
                        )
                    }

                    items(categoryItems) { item ->
                        val isEquipped = when (currentCategory) {
                            ItemCategory.HAT -> activePet.selectedHat == item.itemId
                            ItemCategory.GLASSES -> activePet.selectedGlasses == item.itemId
                            ItemCategory.CLOTHES -> activePet.selectedClothes == item.itemId
                            ItemCategory.COLLAR -> activePet.selectedCollar == item.itemId
                            ItemCategory.ACCESSORY -> activePet.selectedAccessory == item.itemId
                            ItemCategory.SKIN -> activePet.skinId == item.itemId
                            else -> false
                        }

                        ClosetItemCard(
                            emoji = item.iconEmoji,
                            name = item.name,
                            isEquipped = isEquipped,
                            isUnlocked = item.isUnlocked,
                            onEquip = {
                                if (item.isUnlocked) {
                                    onEquipItem(
                                        activePet.id,
                                        currentCategory,
                                        if (isEquipped) null else item.itemId
                                    )
                                } else {
                                    onNavigateToShop()
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Rename Pet Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = {
                Text(
                    text = "Rename ${activePet.type.displayName}",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a cute nickname for your companion:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rename_pet_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRenamePet(activePet.id, renameInput)
                        }
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    modifier = Modifier.testTag("rename_pet_confirm_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun ClosetItemCard(
    emoji: String,
    name: String,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    onEquip: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .width(112.dp)
            .testTag("closet_item_$name"),
        shape = RoundedCornerShape(20.dp),
        accentBorderColor = if (isEquipped) NeonMint else if (isUnlocked) NeonPurpleLight.copy(alpha = 0.35f) else Color(0x3364748B),
        onClick = {
            SoundManager.playTap()
            onEquip()
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isEquipped) NeonPurple.copy(alpha = 0.25f) else Color(0x35140A30))
                    .border(
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isEquipped) NeonMint else Color(0x30FFFFFF)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 26.sp)
                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔒", fontSize = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) TextPrimary else TextSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isEquipped) "EQUIPPED" else if (isUnlocked) "Equip" else "Shop",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isEquipped) NeonMint else if (isUnlocked) NeonPurpleLight else GoldCoin
            )
        }
    }
}
