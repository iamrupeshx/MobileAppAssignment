package com.smartfit.app.data.local.db

import androidx.room.*
import com.smartfit.app.data.model.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Query("SELECT * FROM activity_logs WHERE userId = :userId ORDER BY date DESC")
    fun getActivitiesForUser(userId: Int): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE userId = :userId AND date >= :fromMs ORDER BY date DESC")
    fun getActivitiesSince(userId: Int, fromMs: Long): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityLog): Long

    @Update
    suspend fun update(activity: ActivityLog)

    @Delete
    suspend fun delete(activity: ActivityLog)

    @Query("SELECT SUM(caloriesBurned) FROM activity_logs WHERE userId = :userId AND date >= :fromMs")
    suspend fun totalCaloriesSince(userId: Int, fromMs: Long): Int?

    @Query("SELECT SUM(steps) FROM activity_logs WHERE userId = :userId AND date >= :fromMs")
    suspend fun totalStepsSince(userId: Int, fromMs: Long): Int?
}
