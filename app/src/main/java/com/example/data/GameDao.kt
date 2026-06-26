package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProfile(profile: PlayerProfile)

    @Query("SELECT * FROM level_progress")
    fun getAllLevelProgress(): Flow<List<LevelProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevelProgress(progress: LevelProgress)

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    suspend fun getLevelProgressById(levelId: String): LevelProgress?
}
