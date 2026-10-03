package com.example.data.repository

import com.example.data.local.FoxyPetDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.DailyMissionEntity
import com.example.data.model.GameProgressEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.data.model.MissionType
import com.example.data.model.PetEntity
import com.example.data.model.PetType
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class PetRepository(private val db: FoxyPetDatabase) {

    val allPets: Flow<List<PetEntity>> = db.petDao().getAllPets()
    val gameProgress: Flow<GameProgressEntity?> = db.gameProgressDao().getGameProgress()
    val inventory: Flow<List<InventoryItemEntity>> = db.inventoryDao().getAllItems()
    val dailyMissions: Flow<List<DailyMissionEntity>> = db.missionDao().getAllMissions()
    val achievements: Flow<List<AchievementEntity>> = db.achievementDao().getAllAchievements()

    fun getPet(id: String): Flow<PetEntity?> = db.petDao().getPetById(id)

    suspend fun getActivePet(): PetEntity? {
        val progress = db.gameProgressDao().getGameProgressDirect() ?: return null
        return db.petDao().getPetByIdDirect(progress.activePetId)
    }

    suspend fun checkAndApplyTimeDecay() {
        val pets = listOf("fox", "cat", "dog")
        val now = System.currentTimeMillis()
        for (id in pets) {
            val pet = db.petDao().getPetByIdDirect(id) ?: continue
            val elapsedMinutes = ((now - pet.lastUpdatedTime) / (1000 * 60)).coerceAtLeast(0)
            if (elapsedMinutes > 0) {
                // Decay rates per 5 minutes
                val steps = (elapsedMinutes / 5).coerceAtMost(100)
                if (steps > 0) {
                    val hungerDecay = steps * 0.8f
                    val happinessDecay = steps * 0.6f
                    val cleanlinessDecay = steps * 0.5f
                    val energyChange = if (pet.isSleeping) steps * 3.5f else -(steps * 0.7f)

                    val newHunger = max(5f, pet.hunger - hungerDecay)
                    val newHappiness = max(10f, pet.happiness - happinessDecay)
                    val newCleanliness = max(10f, pet.cleanliness - cleanlinessDecay)
                    val newEnergy = min(100f, max(5f, pet.energy + energyChange))
                    
                    // Health drops slightly if starving or filthy
                    val healthPenalty = if (newHunger < 20f || newCleanliness < 20f) steps * 0.5f else 0f
                    val healthRegen = if (newHunger > 70f && newHappiness > 70f && newCleanliness > 70f) steps * 0.5f else 0f
                    val newHealth = min(100f, max(10f, pet.health - healthPenalty + healthRegen))

                    db.petDao().insertOrUpdatePet(
                        pet.copy(
                            hunger = newHunger,
                            happiness = newHappiness,
                            cleanliness = newCleanliness,
                            energy = newEnergy,
                            health = newHealth,
                            lastUpdatedTime = now
                        )
                    )
                }
            }
        }
    }

    suspend fun checkDailyLoginAndReset() {
        val progress = db.gameProgressDao().getGameProgressDirect() ?: return
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (progress.lastLoginDate != todayStr) {
            val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - 86400000L))
            val newStreak = if (progress.lastLoginDate == yesterdayStr) {
                progress.streakDays + 1
            } else if (progress.lastLoginDate.isEmpty()) {
                1
            } else {
                1 // streak reset
            }

            // Reward daily coins based on streak (Hard balanced economy)
            val streakBonus = min(40, 5 + newStreak * 4)
            val updatedCoins = progress.coins + streakBonus

            db.gameProgressDao().saveProgress(
                progress.copy(
                    lastLoginDate = todayStr,
                    streakDays = newStreak,
                    coins = updatedCoins
                )
            )

            // Reset daily missions if date changed
            if (progress.dailyMissionsDate != todayStr) {
                resetDailyMissions(todayStr)
            }
            updateAchievementProgress("ach_streak", newStreak)
        }
    }

    private suspend fun resetDailyMissions(todayStr: String) {
        val missions = listOf(
            DailyMissionEntity("m_feed", "Tasty Mealtime", "Feed your pet 3 times today", MissionType.FEED, target = 3, currentProgress = 0, rewardCoins = 14, rewardXp = 20),
            DailyMissionEntity("m_clean", "Bubbly Spa Time", "Give your pet a warm bath", MissionType.CLEAN, target = 1, currentProgress = 0, rewardCoins = 10, rewardXp = 15),
            DailyMissionEntity("m_play", "Arcade Fun", "Play 2 mini-games", MissionType.PLAY_MINIGAME, target = 2, currentProgress = 0, rewardCoins = 16, rewardXp = 25),
            DailyMissionEntity("m_pet", "Cuddle Session", "Pet your companion 10 times", MissionType.PET_INTERACT, target = 10, currentProgress = 0, rewardCoins = 8, rewardXp = 15),
            DailyMissionEntity("m_happy", "Joyful Companion", "Keep pet happiness above 80%", MissionType.KEEP_HAPPY, target = 1, currentProgress = 0, rewardCoins = 12, rewardXp = 20)
        )
        db.missionDao().insertMissions(missions)
        val progress = db.gameProgressDao().getGameProgressDirect()
        if (progress != null) {
            db.gameProgressDao().saveProgress(progress.copy(dailyMissionsDate = todayStr))
        }
    }

    suspend fun feedPet(foodItemId: String): Boolean {
        val item = db.inventoryDao().getItemById(foodItemId) ?: return false
        if (item.quantity <= 0) return false
        val activePet = getActivePet() ?: return false

        // Deduct 1 item quantity
        db.inventoryDao().updateItem(item.copy(quantity = item.quantity - 1))

        // Update pet stats
        val newHunger = min(100f, activePet.hunger + item.hungerBoost)
        val newHappiness = min(100f, activePet.happiness + item.happinessBoost)
        val newHealth = min(100f, activePet.health + item.healthBoost)
        val newEnergy = min(100f, activePet.energy + item.energyBoost)

        db.petDao().insertOrUpdatePet(
            activePet.copy(
                hunger = newHunger,
                happiness = newHappiness,
                health = newHealth,
                energy = newEnergy,
                lastUpdatedTime = System.currentTimeMillis()
            )
        )

        addXpAndCoins(xpGained = 15, coinsGained = 0)
        incrementMissionProgress(MissionType.FEED)
        incrementAchievementProgress("ach_first_snack", 1)
        return true
    }

    suspend fun cleanPet() {
        val activePet = getActivePet() ?: return
        db.petDao().insertOrUpdatePet(
            activePet.copy(
                cleanliness = 100f,
                happiness = min(100f, activePet.happiness + 15f),
                lastUpdatedTime = System.currentTimeMillis()
            )
        )
        addXpAndCoins(xpGained = 15, coinsGained = 2)
        incrementMissionProgress(MissionType.CLEAN)
        incrementAchievementProgress("ach_clean_freak", 1)
    }

    suspend fun petInteraction(): Float {
        val activePet = getActivePet() ?: return 0f
        val newHappiness = min(100f, activePet.happiness + 4f)
        db.petDao().insertOrUpdatePet(
            activePet.copy(
                happiness = newHappiness,
                lastUpdatedTime = System.currentTimeMillis()
            )
        )
        incrementMissionProgress(MissionType.PET_INTERACT)
        if (newHappiness >= 80f) {
            incrementMissionProgress(MissionType.KEEP_HAPPY)
        }
        return newHappiness
    }

    suspend fun toggleSleep(sleeping: Boolean? = null) {
        val activePet = getActivePet() ?: return
        val targetSleep = sleeping ?: !activePet.isSleeping
        db.petDao().insertOrUpdatePet(
            activePet.copy(
                isSleeping = targetSleep,
                energy = if (targetSleep) min(100f, activePet.energy + 20f) else activePet.energy,
                lastUpdatedTime = System.currentTimeMillis()
            )
        )
    }

    suspend fun giveGift() {
        val activePet = getActivePet() ?: return
        db.petDao().insertOrUpdatePet(
            activePet.copy(
                happiness = 100f,
                hunger = min(100f, activePet.hunger + 15f),
                lastUpdatedTime = System.currentTimeMillis()
            )
        )
        addXpAndCoins(xpGained = 30, coinsGained = 15)
        incrementMissionProgress(MissionType.KEEP_HAPPY)
    }

    suspend fun switchActivePet(petId: String) {
        val progress = db.gameProgressDao().getGameProgressDirect() ?: return
        db.gameProgressDao().saveProgress(progress.copy(activePetId = petId))
    }

    suspend fun renamePet(petId: String, newName: String) {
        val pet = db.petDao().getPetByIdDirect(petId) ?: return
        db.petDao().insertOrUpdatePet(pet.copy(name = newName.trim()))
    }

    suspend fun equipItem(petId: String, category: ItemCategory, itemId: String?) {
        val pet = db.petDao().getPetByIdDirect(petId) ?: return
        val updated = when (category) {
            ItemCategory.HAT -> pet.copy(selectedHat = itemId)
            ItemCategory.GLASSES -> pet.copy(selectedGlasses = itemId)
            ItemCategory.CLOTHES -> pet.copy(selectedClothes = itemId)
            ItemCategory.COLLAR -> pet.copy(selectedCollar = itemId)
            ItemCategory.ACCESSORY -> pet.copy(selectedAccessory = itemId)
            ItemCategory.SKIN -> pet.copy(skinId = itemId ?: "default")
            else -> pet
        }
        db.petDao().insertOrUpdatePet(updated)
        if (itemId != null) {
            incrementAchievementProgress("ach_fashionista", 1)
        }
    }

    suspend fun switchWorld(worldId: String) {
        val progress = db.gameProgressDao().getGameProgressDirect() ?: return
        db.gameProgressDao().saveProgress(progress.copy(currentWorld = worldId))
        incrementAchievementProgress("ach_world", 1)
    }

    suspend fun purchaseItem(itemId: String): Boolean {
        val item = db.inventoryDao().getItemById(itemId) ?: return false
        val progress = db.gameProgressDao().getGameProgressDirect() ?: return false
        if (progress.coins < item.price) return false

        // Deduct coins
        db.gameProgressDao().saveProgress(progress.copy(coins = progress.coins - item.price))

        if (item.category == ItemCategory.FOOD) {
            db.inventoryDao().updateItem(item.copy(quantity = item.quantity + 3, isUnlocked = true))
        } else {
            db.inventoryDao().updateItem(item.copy(isUnlocked = true))
        }
        return true
    }

    suspend fun addXpAndCoins(xpGained: Int, coinsGained: Int): Pair<Int, Boolean> {
        val progress = db.gameProgressDao().getGameProgressDirect() ?: return Pair(1, false)
        var currentCoins = progress.coins + coinsGained
        var currentXp = progress.xp + xpGained
        var currentLevel = progress.level
        var leveledUp = false

        while (currentXp >= currentLevel * 100 + 50) {
            currentXp -= (currentLevel * 100 + 50)
            currentLevel += 1
            currentCoins += currentLevel * 50 // Level-up bonus coins!
            leveledUp = true
        }

        db.gameProgressDao().saveProgress(
            progress.copy(
                coins = currentCoins,
                xp = currentXp,
                level = currentLevel
            )
        )

        updateAchievementProgress("ach_coins", currentCoins)
        updateAchievementProgress("ach_level_up", currentLevel)
        return Pair(currentLevel, leveledUp)
    }

    suspend fun recordMiniGamePlayed(score: Int, earnedCoins: Int, earnedXp: Int) {
        val activePet = getActivePet()
        if (activePet != null) {
            db.petDao().insertOrUpdatePet(
                activePet.copy(
                    happiness = min(100f, activePet.happiness + 20f),
                    energy = max(10f, activePet.energy - 10f),
                    lastUpdatedTime = System.currentTimeMillis()
                )
            )
        }
        addXpAndCoins(earnedXp, earnedCoins)
        incrementMissionProgress(MissionType.PLAY_MINIGAME)
        incrementAchievementProgress("ach_gamer_pro", 1)
    }

    suspend fun incrementMissionProgress(type: MissionType, amount: Int = 1) {
        val missionId = when (type) {
            MissionType.FEED -> "m_feed"
            MissionType.CLEAN -> "m_clean"
            MissionType.PLAY_MINIGAME -> "m_play"
            MissionType.PET_INTERACT -> "m_pet"
            MissionType.KEEP_HAPPY -> "m_happy"
            else -> null
        } ?: return

        // We read the current mission progress or execute a quick update
        when (missionId) {
            "m_feed" -> {
                // Update progress
                db.missionDao().updateProgress("m_feed", min(3, getMissionProgressEstimate("m_feed") + amount))
            }
            "m_clean" -> {
                db.missionDao().updateProgress("m_clean", 1)
            }
            "m_play" -> {
                db.missionDao().updateProgress("m_play", min(2, getMissionProgressEstimate("m_play") + amount))
            }
            "m_pet" -> {
                db.missionDao().updateProgress("m_pet", min(10, getMissionProgressEstimate("m_pet") + amount))
            }
            "m_happy" -> {
                db.missionDao().updateProgress("m_happy", 1)
            }
        }
    }

    private var cachedMissionCounts = mutableMapOf<String, Int>()

    private fun getMissionProgressEstimate(missionId: String): Int {
        val current = cachedMissionCounts[missionId] ?: 0
        cachedMissionCounts[missionId] = current + 1
        return current + 1
    }

    suspend fun claimMissionReward(mission: DailyMissionEntity) {
        if (!mission.isCompleted || mission.isClaimed) return
        db.missionDao().updateMission(mission.copy(isClaimed = true))
        addXpAndCoins(mission.rewardXp, mission.rewardCoins)
    }

    suspend fun claimAchievementReward(achievement: AchievementEntity) {
        if (!achievement.isUnlocked || achievement.isClaimed) return
        db.achievementDao().updateAchievement(achievement.copy(isClaimed = true))
        addXpAndCoins(achievement.rewardXp, achievement.rewardCoins)
    }

    suspend fun incrementAchievementProgress(achievementId: String, amount: Int = 1) {
        val current = (cachedMissionCounts[achievementId] ?: 0) + amount
        cachedMissionCounts[achievementId] = current
        db.achievementDao().updateProgress(achievementId, current)
    }

    suspend fun updateAchievementProgress(achievementId: String, value: Int) {
        db.achievementDao().updateProgress(achievementId, value)
    }

    suspend fun updateSettings(sound: Boolean, haptics: Boolean, notifications: Boolean) {
        val progress = db.gameProgressDao().getGameProgressDirect() ?: return
        db.gameProgressDao().saveProgress(
            progress.copy(
                soundEnabled = sound,
                hapticsEnabled = haptics,
                notificationsEnabled = notifications
            )
        )
    }

    suspend fun completeOnboarding(selectedPetId: String, customName: String) {
        val progress = db.gameProgressDao().getGameProgressDirect() ?: return
        val pet = db.petDao().getPetByIdDirect(selectedPetId)
        if (pet != null) {
            db.petDao().insertOrUpdatePet(
                pet.copy(name = customName.ifBlank { pet.type.defaultName })
            )
        }
        db.gameProgressDao().saveProgress(
            progress.copy(
                activePetId = selectedPetId,
                isOnboardingCompleted = true
            )
        )
    }

    suspend fun resetGameData() {
        FoxyPetDatabase.populateInitialData(db)
    }
}
