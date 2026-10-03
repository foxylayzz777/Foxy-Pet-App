package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FoxyPetDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.DailyMissionEntity
import com.example.data.model.GameProgressEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.data.model.PetEntity
import com.example.data.repository.PetRepository
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LevelUpEvent(val newLevel: Int, val bonusCoins: Int)

class FoxyPetViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FoxyPetDatabase.getDatabase(application, viewModelScope)
    private val repository = PetRepository(db)

    val gameProgress: StateFlow<GameProgressEntity?> = repository.gameProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allPets: StateFlow<List<PetEntity>> = repository.allPets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePet: StateFlow<PetEntity?> = combine(allPets, gameProgress) { pets, progress ->
        val activeId = progress?.activePetId ?: "fox"
        pets.find { it.id == activeId } ?: pets.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val inventory: StateFlow<List<InventoryItemEntity>> = repository.inventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyMissions: StateFlow<List<DailyMissionEntity>> = repository.dailyMissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isBathActive = MutableStateFlow(false)
    val isBathActive: StateFlow<Boolean> = _isBathActive.asStateFlow()

    private val _isFeedingActive = MutableStateFlow(false)
    val isFeedingActive: StateFlow<Boolean> = _isFeedingActive.asStateFlow()

    private val _isGiftActive = MutableStateFlow(false)
    val isGiftActive: StateFlow<Boolean> = _isGiftActive.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<LevelUpEvent?>(null)
    val levelUpEvent: StateFlow<LevelUpEvent?> = _levelUpEvent.asStateFlow()

    private val _randomEventMessage = MutableStateFlow<String?>(null)
    val randomEventMessage: StateFlow<String?> = _randomEventMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkDailyLoginAndReset()
            repository.checkAndApplyTimeDecay()
            triggerOccasionalRandomEvents()
        }
    }

    private fun triggerOccasionalRandomEvents() {
        viewModelScope.launch {
            delay(15000)
            val events = listOf(
                "🎁 Your companion found a shiny coin behind the couch! (+15 🪙)",
                "🦋 A colorful butterfly flew into the room! Pet happiness +10%",
                "⭐ Starlight blessed your companion! XP +20 gained!"
            )
            val event = events.random()
            _randomEventMessage.value = event
            repository.addXpAndCoins(15, 15)
        }
    }

    fun dismissRandomEvent() {
        _randomEventMessage.value = null
    }

    fun dismissLevelUp() {
        _levelUpEvent.value = null
    }

    fun feedPet(foodItemId: String) {
        viewModelScope.launch {
            _isFeedingActive.value = true
            SoundManager.playEat()
            val success = repository.feedPet(foodItemId)
            delay(1200)
            _isFeedingActive.value = false
        }
    }

    fun cleanPet() {
        viewModelScope.launch {
            _isBathActive.value = true
            SoundManager.playClean()
            delay(1600)
            repository.cleanPet()
            _isBathActive.value = false
        }
    }

    fun petInteraction() {
        viewModelScope.launch {
            SoundManager.playPet()
            repository.petInteraction()
        }
    }

    fun toggleSleep() {
        viewModelScope.launch {
            SoundManager.playTap()
            repository.toggleSleep()
        }
    }

    fun giveGift() {
        viewModelScope.launch {
            _isGiftActive.value = true
            SoundManager.playSuccess()
            repository.giveGift()
            delay(2000)
            _isGiftActive.value = false
        }
    }

    fun switchPet(petId: String) {
        viewModelScope.launch {
            SoundManager.playTap()
            repository.switchActivePet(petId)
        }
    }

    fun renamePet(petId: String, newName: String) {
        viewModelScope.launch {
            SoundManager.playTap()
            repository.renamePet(petId, newName)
        }
    }

    fun equipItem(petId: String, category: ItemCategory, itemId: String?) {
        viewModelScope.launch {
            SoundManager.playTap()
            repository.equipItem(petId, category, itemId)
        }
    }

    fun switchWorld(worldId: String) {
        viewModelScope.launch {
            SoundManager.playTap()
            repository.switchWorld(worldId)
        }
    }

    fun purchaseItem(itemId: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.purchaseItem(itemId)
            if (success) {
                SoundManager.playCoin()
            } else {
                SoundManager.playFail()
            }
            onResult(success)
        }
    }

    fun recordMiniGame(coins: Int, xp: Int) {
        viewModelScope.launch {
            if (coins > 0) {
                SoundManager.playCoin()
            }
            val (newLevel, leveledUp) = repository.addXpAndCoins(xp, coins)
            repository.recordMiniGamePlayed(coins, coins, xp)
            if (leveledUp) {
                SoundManager.playLevelUp()
                _levelUpEvent.value = LevelUpEvent(newLevel, newLevel * 50)
            }
        }
    }

    fun claimMission(mission: DailyMissionEntity) {
        viewModelScope.launch {
            repository.claimMissionReward(mission)
        }
    }

    fun claimAchievement(achievement: AchievementEntity) {
        viewModelScope.launch {
            repository.claimAchievementReward(achievement)
        }
    }

    fun updateSettings(sound: Boolean, haptics: Boolean, notifications: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(sound, haptics, notifications)
        }
    }

    fun completeOnboarding(petId: String, name: String) {
        viewModelScope.launch {
            repository.completeOnboarding(petId, name)
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            repository.resetGameData()
        }
    }
}
