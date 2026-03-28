package com.smartfit.app.data.local.db

import androidx.room.*
import com.smartfit.app.data.model.FoodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_logs WHERE userId = :userId ORDER BY date DESC")
    fun getAllForUser(userId: Int): Flow<List<FoodLog>>

    @Query("SELECT * FROM food_logs WHERE userId = :userId AND date >= :fromMs ORDER BY date DESC")
    fun getSince(userId: Int, fromMs: Long): Flow<List<FoodLog>>

    @Query("SELECT SUM(calories) FROM food_logs WHERE userId = :userId AND date >= :fromMs")
    suspend fun totalCaloriesSince(userId: Int, fromMs: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodLog: FoodLog): Long

    @Delete
    suspend fun delete(foodLog: FoodLog)

    @Update
    suspend fun update(foodLog: FoodLog)
}
