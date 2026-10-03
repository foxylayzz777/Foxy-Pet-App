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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PetEntity
import com.example.data.model.PetType
import com.example.ui.components.PetCanvas
import com.example.ui.pet2d.Pet2DView
import com.example.ui.theme.AmbientLiquidBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.LiquidGlassCapsule
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SoundManager

@Composable
fun OnboardingScreen(
    pets: List<PetEntity>,
    onCompleteOnboarding: (selectedPetId: String, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPetId by remember { mutableStateOf("fox") }
    val currentPet = pets.find { it.id == selectedPetId } ?: PetEntity("fox", "Rusty", PetType.FOX)
    var petName by remember(selectedPetId) { mutableStateOf(currentPet.name) }

    AmbientLiquidBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to",
                fontSize = 16.sp,
                color = NeonMint,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "FoxyPet 🦊",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Choose your very first companion to start your journey! Feed, play, care, dress up, and level up together.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pet Selection Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val petOptions = listOf(
                    Triple("fox", "Fox 🦊", "Clever & Playful"),
                    Triple("cat", "Cat 🐱", "Curious & Cuddly"),
                    Triple("dog", "Dog 🐶", "Loyal & Joyful")
                )

                petOptions.forEach { (id, title, trait) ->
                    val isSelected = selectedPetId == id
                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboarding_select_$id"),
                        shape = RoundedCornerShape(20.dp),
                        accentBorderColor = if (isSelected) NeonMint else DarkBorder,
                        onClick = {
                            SoundManager.playTap()
                            selectedPetId = id
                            val match = pets.find { it.id == id }
                            petName = match?.name ?: "Buddy"
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = if (id == "fox") "🦊" else if (id == "cat") "🐱" else "🐶", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Live Pet Showcase Preview (Liquid Glass)
            LiquidGlassCard(
                shape = RoundedCornerShape(26.dp),
                accentBorderColor = NeonPurpleLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x35140A30))
                            .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0x35FFFFFF)), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Pet2DView(
                            pet = currentPet.copy(name = petName),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Name input
                    Text(
                        text = "Name your companion:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_pet_name_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Starter Gift Badge (Liquid Glass)
            LiquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                accentBorderColor = GoldCoin.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎁", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Starter Welcome Gift", fontWeight = FontWeight.Bold, color = GoldCoin)
                        Text(
                            text = "Includes 50 🪙 Coins + Golden Apple + Chew Bone!",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    SoundManager.playSuccess()
                    onCompleteOnboarding(selectedPetId, petName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x70FFFFFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_start_button")
            ) {
                Text(
                    text = "Adopt & Start Adventure! 🐾",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
