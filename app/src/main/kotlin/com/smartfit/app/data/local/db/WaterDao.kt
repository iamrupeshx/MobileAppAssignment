package com.smartfit.app.data.local.db

import androidx.room.*
import com.smartfit.app.data.model.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT SUM(glasses) FROM water_logs WHERE userId = :userId AND date >= :fromMs")
    fun getTodayWater(userId: Int, fromMs: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(waterLog: WaterLog)

    @Query("DELETE FROM water_logs WHERE userId = :userId AND date >= :fromMs")
    suspend fun clearTodayWater(userId: Int, fromMs: Long)

    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date >= :fromMs ORDER BY id DESC LIMIT 1")
    suspend fun getLastEntryToday(userId: Int, fromMs: Long): WaterLog?

    @Delete
    suspend fun delete(waterLog: WaterLog)
}
