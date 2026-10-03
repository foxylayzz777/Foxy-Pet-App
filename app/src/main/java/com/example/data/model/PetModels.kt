package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PetType(val displayName: String, val emoji: String, val defaultName: String, val trait: String) {
    FOX("Fox", "🦊", "Rusty", "Clever & Playful"),
    CAT("Cat", "🐱", "Luna", "Curious & Cuddly"),
    DOG("Dog", "🐶", "Milo", "Loyal & Energetic")
}

enum class PetMood(val title: String, val emoji: String, val description: String) {
    ECSTATIC("Ecstatic", "✨🥰", "Feeling fantastic and overflowing with joy!"),
    HAPPY("Happy", "😊", "Content and enjoying time with you."),
    NORMAL("Chilling", "😌", "Relaxed and doing just fine."),
    HUNGRY("Hungry", "🍖🥺", "Tummy is rumbling, needs tasty food!"),
    SLEEPY("Sleepy", "😴💤", "Dozing off, time for a cozy nap."),
    DIRTY("Messy", "🛁🧼", "Needs a warm bubbly bath!"),
    SICK("Unwell", "🤒❤️", "Needs love, medicine, and careful attention.")
}

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val id: String, // "fox", "cat", "dog"
    val name: String,
    val type: PetType,
    val skinId: String = "default", // default, neon, galaxy, sakura, golden
    val hunger: Float = 85f, // 0 - 100
    val happiness: Float = 90f,
    val energy: Float = 80f,
    val cleanliness: Float = 85f,
    val health: Float = 100f,
    val isSleeping: Boolean = false,
    val selectedHat: String? = null,
    val selectedGlasses: String? = null,
    val selectedClothes: String? = null,
    val selectedCollar: String? = null,
    val selectedAccessory: String? = null,
    val lastUpdatedTime: Long = System.currentTimeMillis(),
    val adoptedDate: Long = System.currentTimeMillis(),
    val isUnlocked: Boolean = true
) {
    fun calculateMood(): PetMood {
        return when {
            health < 40f -> PetMood.SICK
            energy < 25f -> PetMood.SLEEPY
            hunger < 30f -> PetMood.HUNGRY
            cleanliness < 30f -> PetMood.DIRTY
            hunger > 80f && happiness > 80f && energy > 70f && cleanliness > 70f -> PetMood.ECSTATIC
            happiness > 60f -> PetMood.HAPPY
            else -> PetMood.NORMAL
        }
    }
}

@Entity(tableName = "game_progress")
data class GameProgressEntity(
    @PrimaryKey val id: Int = 1,
    val activePetId: String = "fox",
    val coins: Int = 150,
    val xp: Int = 0,
    val level: Int = 1,
    val streakDays: Int = 1,
    val lastLoginDate: String = "",
    val dailyMissionsDate: String = "",
    val currentWorld: String = "BEDROOM", // BEDROOM, GARDEN, PARK, BEACH, FOREST, NIGHT
    val currentWallpaper: String = "pastel_studio",
    val currentBed: String = "cloud",
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isOnboardingCompleted: Boolean = false
) {
    fun xpForNextLevel(): Int = level * 100 + 50
}

enum class ItemCategory(val displayName: String, val icon: String) {
    FOOD("Food & Treats", "🍖"),
    HAT("Hats", "🎩"),
    GLASSES("Glasses", "🕶️"),
    CLOTHES("Outfits", "👕"),
    COLLAR("Collars", "🎀"),
    ACCESSORY("Accessories", "✨"),
    SKIN("Pet Skins", "🎨"),
    WORLD("Worlds", "🌎"),
    FURNITURE("Room Decor", "🛋️")
}

@Entity(tableName = "inventory")
data class InventoryItemEntity(
    @PrimaryKey val itemId: String,
    val category: ItemCategory,
    val name: String,
    val description: String,
    val price: Int,
    val quantity: Int = 1, // For consumable food
    val isUnlocked: Boolean = false,
    val hungerBoost: Float = 0f,
    val happinessBoost: Float = 0f,
    val healthBoost: Float = 0f,
    val energyBoost: Float = 0f,
    val cleanlinessBoost: Float = 0f,
    val iconEmoji: String = "🎁"
)

enum class MissionType {
    FEED, PLAY_MINIGAME, CLEAN, PET_INTERACT, KEEP_HAPPY, SLEEP_NAP, BUY_ITEM
}

@Entity(tableName = "daily_missions")
data class DailyMissionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: MissionType,
    val target: Int,
    val currentProgress: Int = 0,
    val rewardCoins: Int,
    val rewardXp: Int,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = currentProgress >= target
}

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val currentProgress: Int = 0,
    val targetProgress: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false
)
