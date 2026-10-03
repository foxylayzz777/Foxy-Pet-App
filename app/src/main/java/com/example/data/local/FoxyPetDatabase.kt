package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.DailyMissionEntity
import com.example.data.model.GameProgressEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.data.model.MissionType
import com.example.data.model.PetEntity
import com.example.data.model.PetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PetEntity::class,
        GameProgressEntity::class,
        InventoryItemEntity::class,
        DailyMissionEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FoxyPetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun gameProgressDao(): GameProgressDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun missionDao(): MissionDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: FoxyPetDatabase? = null

        fun getDatabase(context: Context, coroutineScope: CoroutineScope): FoxyPetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoxyPetDatabase::class.java,
                    "foxypet_database"
                )
                    .addCallback(DatabaseCallback(coroutineScope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: FoxyPetDatabase) {
            // Initial Pets
            val pets = listOf(
                PetEntity(
                    id = "fox",
                    name = "Rusty",
                    type = PetType.FOX,
                    skinId = "default",
                    hunger = 85f,
                    happiness = 90f,
                    energy = 80f,
                    cleanliness = 85f,
                    health = 100f,
                    isUnlocked = true
                ),
                PetEntity(
                    id = "cat",
                    name = "Luna",
                    type = PetType.CAT,
                    skinId = "default",
                    hunger = 80f,
                    happiness = 85f,
                    energy = 90f,
                    cleanliness = 90f,
                    health = 100f,
                    isUnlocked = true
                ),
                PetEntity(
                    id = "dog",
                    name = "Milo",
                    type = PetType.DOG,
                    skinId = "default",
                    hunger = 85f,
                    happiness = 95f,
                    energy = 85f,
                    cleanliness = 75f,
                    health = 100f,
                    isUnlocked = true
                )
            )
            db.petDao().insertInitialPets(pets)

            // Initial Game Progress (Hard Economy: 50 starter coins)
            val progress = GameProgressEntity(
                id = 1,
                activePetId = "fox",
                coins = 50,
                xp = 0,
                level = 1,
                streakDays = 1,
                lastLoginDate = "",
                dailyMissionsDate = "",
                currentWorld = "BEDROOM",
                isOnboardingCompleted = false
            )
            db.gameProgressDao().saveProgress(progress)

            // Initial Inventory & Shop Items (Foods must be bought; 1 apple & 1 bone starter kit)
            val initialInventory = listOf(
                // Foods (Consumables with initial counts)
                InventoryItemEntity("food_apple", ItemCategory.FOOD, "Golden Apple", "Sweet & restores health and hunger", 20, quantity = 1, isUnlocked = true, hungerBoost = 30f, healthBoost = 20f, iconEmoji = "🍎"),
                InventoryItemEntity("food_steak", ItemCategory.FOOD, "Juicy Steak", "Hearty meal for energetic pets", 35, quantity = 0, isUnlocked = false, hungerBoost = 45f, happinessBoost = 20f, iconEmoji = "🥩"),
                InventoryItemEntity("food_fish", ItemCategory.FOOD, "Fresh Salmon", "Delicacy rich in vitamins", 28, quantity = 0, isUnlocked = false, hungerBoost = 35f, healthBoost = 15f, iconEmoji = "🐟"),
                InventoryItemEntity("food_bone", ItemCategory.FOOD, "Chew Bone", "Crunchy treat pets love chewing", 18, quantity = 1, isUnlocked = true, hungerBoost = 25f, happinessBoost = 25f, iconEmoji = "🦴"),
                InventoryItemEntity("food_berries", ItemCategory.FOOD, "Wild Berries", "Fresh berries picked from the woods", 15, quantity = 0, isUnlocked = false, hungerBoost = 20f, happinessBoost = 20f, iconEmoji = "🍓"),
                InventoryItemEntity("food_cake", ItemCategory.FOOD, "Rainbow Cupcake", "Sweet celebration pastry", 45, quantity = 0, isUnlocked = false, hungerBoost = 25f, happinessBoost = 45f, iconEmoji = "🧁"),
                InventoryItemEntity("food_milk", ItemCategory.FOOD, "Warm Honey Milk", "Soothes and restores tired energy", 22, quantity = 0, isUnlocked = false, hungerBoost = 20f, energyBoost = 35f, iconEmoji = "🥛"),

                // Hats
                InventoryItemEntity("hat_party", ItemCategory.HAT, "Party Cone", "Celebratory cone for happy vibes", 60, isUnlocked = true, iconEmoji = "🎉"),
                InventoryItemEntity("hat_crown", ItemCategory.HAT, "Golden Tiara", "Fit for virtual pet royalty", 150, isUnlocked = false, iconEmoji = "👑"),
                InventoryItemEntity("hat_wizard", ItemCategory.HAT, "Star Wizard Hat", "Imbued with cute magic", 120, isUnlocked = false, iconEmoji = "🧙"),
                InventoryItemEntity("hat_detective", ItemCategory.HAT, "Detective Cap", "On the case for lost snacks", 90, isUnlocked = false, iconEmoji = "🕵️"),
                InventoryItemEntity("hat_chef", ItemCategory.HAT, "Chef Toque", "Ready to cook 5-star pet dinners", 75, isUnlocked = false, iconEmoji = "👨‍🍳"),

                // Glasses
                InventoryItemEntity("glasses_shades", ItemCategory.GLASSES, "Cool Neon Shades", "Dark neon sunglasses with swagger", 65, isUnlocked = true, iconEmoji = "😎"),
                InventoryItemEntity("glasses_nerd", ItemCategory.GLASSES, "Smart Round Specs", "Makes your pet look like a scholar", 50, isUnlocked = false, iconEmoji = "👓"),
                InventoryItemEntity("glasses_star", ItemCategory.GLASSES, "Retro Star Glasses", "Party superstar eyewear", 80, isUnlocked = false, iconEmoji = "⭐"),
                InventoryItemEntity("glasses_monocle", ItemCategory.GLASSES, "Gentleman Monocle", "Pure sophistication", 95, isUnlocked = false, iconEmoji = "🧐"),

                // Outfits
                InventoryItemEntity("clothes_sweater", ItemCategory.CLOTHES, "Cozy Knit Sweater", "Warm wool sweater for chilly days", 90, isUnlocked = false, iconEmoji = "🧶"),
                InventoryItemEntity("clothes_hoodie", ItemCategory.CLOTHES, "Cyber Neon Hoodie", "Trendy street style with glowing trim", 120, isUnlocked = false, iconEmoji = "🧥"),
                InventoryItemEntity("clothes_cape", ItemCategory.CLOTHES, "Hero Cape", "Soar through the backyard", 140, isUnlocked = false, iconEmoji = "🦸"),
                InventoryItemEntity("clothes_tux", ItemCategory.CLOTHES, "Fancy Bowtie Tux", "Gala-ready luxury formal wear", 160, isUnlocked = false, iconEmoji = "🤵"),

                // Collars
                InventoryItemEntity("collar_bell", ItemCategory.COLLAR, "Ruby Bell Choker", "Jingles gently as they walk", 45, isUnlocked = true, iconEmoji = "🔔"),
                InventoryItemEntity("collar_spikes", ItemCategory.COLLAR, "Gold Stud Collar", "Edgy yet totally adorable", 70, isUnlocked = false, iconEmoji = "⚡"),
                InventoryItemEntity("collar_neon", ItemCategory.COLLAR, "Cyber Glow Ring", "Pulsing neon collar", 85, isUnlocked = false, iconEmoji = "💎"),
                InventoryItemEntity("collar_ribbon", ItemCategory.COLLAR, "Rainbow Satin Bow", "Cute silky bow collar", 55, isUnlocked = false, iconEmoji = "🎀"),

                // Accessories
                InventoryItemEntity("acc_wings", ItemCategory.ACCESSORY, "Angel Wings", "Soft feathery wings fluttering behind", 200, isUnlocked = false, iconEmoji = "🪽"),
                InventoryItemEntity("acc_halo", ItemCategory.ACCESSORY, "Floating Halo", "A glowing ring of pure goodness", 180, isUnlocked = false, iconEmoji = "😇"),
                InventoryItemEntity("acc_backpack", ItemCategory.ACCESSORY, "Mini Satchel", "Holds treats and pet secrets", 95, isUnlocked = false, iconEmoji = "🎒"),
                InventoryItemEntity("acc_wand", ItemCategory.ACCESSORY, "Magic Star Wand", "Sparks twinkle whenever they jump", 130, isUnlocked = false, iconEmoji = "🪄"),

                // Skins
                InventoryItemEntity("skin_default", ItemCategory.SKIN, "Classic Fur", "Original natural fur pattern", 0, isUnlocked = true, iconEmoji = "✨"),
                InventoryItemEntity("skin_neon", ItemCategory.SKIN, "Neon Cyber Aura", "Futuristic neon highlights", 220, isUnlocked = false, iconEmoji = "⚡"),
                InventoryItemEntity("skin_galaxy", ItemCategory.SKIN, "Galaxy Cosmos", "Stardust & cosmic deep purple shimmer", 280, isUnlocked = false, iconEmoji = "🌌"),
                InventoryItemEntity("skin_sakura", ItemCategory.SKIN, "Sakura Blossom", "Delicate pink cherry blossom hue", 200, isUnlocked = false, iconEmoji = "🌸"),
                InventoryItemEntity("skin_golden", ItemCategory.SKIN, "Golden Legend", "Pure shimmering 24k golden coat", 400, isUnlocked = false, iconEmoji = "🏆"),

                // Worlds
                InventoryItemEntity("world_bedroom", ItemCategory.WORLD, "Cozy Bedroom", "A warm, plush bedroom sanctuary", 0, isUnlocked = true, iconEmoji = "🛏️"),
                InventoryItemEntity("world_garden", ItemCategory.WORLD, "Sunflower Garden", "Sunny flowerbeds and butterflies", 100, isUnlocked = false, iconEmoji = "🌻"),
                InventoryItemEntity("world_park", ItemCategory.WORLD, "Sunny City Park", "Green lawns, leafy trees & benches", 150, isUnlocked = false, iconEmoji = "🌳"),
                InventoryItemEntity("world_beach", ItemCategory.WORLD, "Tropical Beach", "Warm golden sands & soothing ocean", 220, isUnlocked = false, iconEmoji = "🏖️"),
                InventoryItemEntity("world_forest", ItemCategory.WORLD, "Mystic Pine Forest", "Enchanted woods and whispering pines", 280, isUnlocked = false, iconEmoji = "🌲"),
                InventoryItemEntity("world_night", ItemCategory.WORLD, "Starry Night", "Glowing fireflies under midnight sky", 350, isUnlocked = false, iconEmoji = "🌌"),

                // Furniture & Decor
                InventoryItemEntity("furn_cloud_bed", ItemCategory.FURNITURE, "Cloud Bed", "Soft like sleeping on a marshmallow", 0, isUnlocked = true, iconEmoji = "☁️"),
                InventoryItemEntity("furn_scratch_tree", ItemCategory.FURNITURE, "Play Tree Tower", "Climbing post with hanging balls", 90, isUnlocked = false, iconEmoji = "🪵"),
                InventoryItemEntity("furn_royal_sofa", ItemCategory.FURNITURE, "Royal Velvet Couch", "Luxurious plush lounge", 130, isUnlocked = false, iconEmoji = "🛋️"),
                InventoryItemEntity("furn_bonsai", ItemCategory.FURNITURE, "Glowing Pet Bonsai", "Adds natural calming zen vibes", 65, isUnlocked = false, iconEmoji = "🪴"),
                InventoryItemEntity("furn_bookshelf", ItemCategory.FURNITURE, "Mini Library", "Stacked with pet adventure comics", 85, isUnlocked = false, iconEmoji = "📚")
            )
            db.inventoryDao().insertItems(initialInventory)

            // Initial Daily Missions
            val initialMissions = listOf(
                DailyMissionEntity("m_feed", "Tasty Mealtime", "Feed your pet 3 times today", MissionType.FEED, target = 3, currentProgress = 0, rewardCoins = 50, rewardXp = 30),
                DailyMissionEntity("m_clean", "Bubbly Spa Time", "Give your pet a warm bath", MissionType.CLEAN, target = 1, currentProgress = 0, rewardCoins = 40, rewardXp = 25),
                DailyMissionEntity("m_play", "Arcade Fun", "Play 2 mini-games", MissionType.PLAY_MINIGAME, target = 2, currentProgress = 0, rewardCoins = 60, rewardXp = 40),
                DailyMissionEntity("m_pet", "Cuddle Session", "Pet your companion 10 times", MissionType.PET_INTERACT, target = 10, currentProgress = 0, rewardCoins = 35, rewardXp = 20),
                DailyMissionEntity("m_happy", "Joyful Companion", "Keep pet happiness above 80%", MissionType.KEEP_HAPPY, target = 1, currentProgress = 1, rewardCoins = 45, rewardXp = 30)
            )
            db.missionDao().insertMissions(initialMissions)

            // Initial Achievements
            val initialAchievements = listOf(
                AchievementEntity("ach_first_snack", "First Feast", "Feed your companion for the first time", "🍖", currentProgress = 0, targetProgress = 1, rewardCoins = 30, rewardXp = 20),
                AchievementEntity("ach_clean_freak", "Sparkle Clean", "Give your pet 5 relaxing bubble baths", "🛁", currentProgress = 0, targetProgress = 5, rewardCoins = 50, rewardXp = 40),
                AchievementEntity("ach_gamer_pro", "Arcade Master", "Play mini-games 10 times", "🎮", currentProgress = 0, targetProgress = 10, rewardCoins = 80, rewardXp = 60),
                AchievementEntity("ach_fashionista", "Stylin' Pet", "Equip an outfit or accessory in the closet", "🎩", currentProgress = 0, targetProgress = 1, rewardCoins = 45, rewardXp = 30),
                AchievementEntity("ach_level_up", "Growing Up", "Reach Companion Level 3", "⭐", currentProgress = 1, targetProgress = 3, rewardCoins = 100, rewardXp = 80),
                AchievementEntity("ach_coins", "Coin Collector", "Accumulate 300 total coins in your bank", "🪙", currentProgress = 200, targetProgress = 300, rewardCoins = 60, rewardXp = 40),
                AchievementEntity("ach_streak", "Loyal Companion", "Maintain a 3-day daily streak", "🔥", currentProgress = 1, targetProgress = 3, rewardCoins = 90, rewardXp = 70),
                AchievementEntity("ach_memory", "Memory Genius", "Win a game of Memory Match", "🃏", currentProgress = 0, targetProgress = 1, rewardCoins = 50, rewardXp = 35),
                AchievementEntity("ach_reaction", "Lightning Reflex", "Score 5+ in Reaction Tap", "⚡", currentProgress = 0, targetProgress = 5, rewardCoins = 50, rewardXp = 35),
                AchievementEntity("ach_catcher", "Treasure Catcher", "Catch 15 coins in Coin Catcher", "🪙", currentProgress = 0, targetProgress = 15, rewardCoins = 50, rewardXp = 35),
                AchievementEntity("ach_world", "Wanderlust", "Unlock and travel to a new world", "🌎", currentProgress = 0, targetProgress = 1, rewardCoins = 80, rewardXp = 50)
            )
            db.achievementDao().insertAchievements(initialAchievements)
        }
    }
}
