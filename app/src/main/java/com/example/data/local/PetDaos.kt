package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AchievementEntity
import com.example.data.model.DailyMissionEntity
import com.example.data.model.GameProgressEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.data.model.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets")
    fun getAllPets(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id LIMIT 1")
    fun getPetById(id: String): Flow<PetEntity?>

    @Query("SELECT * FROM pets WHERE id = :id LIMIT 1")
    suspend fun getPetByIdDirect(id: String): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePet(pet: PetEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialPets(pets: List<PetEntity>)

    @Update
    suspend fun updatePet(pet: PetEntity)
}

@Dao
interface GameProgressDao {
    @Query("SELECT * FROM game_progress WHERE id = 1 LIMIT 1")
    fun getGameProgress(): Flow<GameProgressEntity?>

    @Query("SELECT * FROM game_progress WHERE id = 1 LIMIT 1")
    suspend fun getGameProgressDirect(): GameProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: GameProgressEntity)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory WHERE category = :category")
    fun getItemsByCategory(category: ItemCategory): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory WHERE isUnlocked = 1")
    fun getUnlockedItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory WHERE itemId = :id LIMIT 1")
    suspend fun getItemById(id: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)
}

@Dao
interface MissionDao {
    @Query("SELECT * FROM daily_missions")
    fun getAllMissions(): Flow<List<DailyMissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<DailyMissionEntity>)

    @Update
    suspend fun updateMission(mission: DailyMissionEntity)

    @Query("UPDATE daily_missions SET currentProgress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("UPDATE achievements SET currentProgress = :progress, isUnlocked = CASE WHEN :progress >= targetProgress THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)
}
