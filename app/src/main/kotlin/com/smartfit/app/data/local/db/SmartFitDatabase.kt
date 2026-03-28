package com.smartfit.app.data.local.db

import android.content.Context
import android.util.Log
import androidx.room.*
import com.smartfit.app.data.model.ActivityLog
import com.smartfit.app.data.model.FoodLog
import com.smartfit.app.data.model.User

@Database(
    entities     = [User::class, ActivityLog::class, FoodLog::class],
    version      = 4,
    exportSchema = false
)
abstract class SmartFitDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao

    companion object {
        private const val TAG = "SmartFitDB"
        @Volatile private var INSTANCE: SmartFitDatabase? = null

        fun getInstance(context: Context): SmartFitDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SmartFitDatabase::class.java,
                    "smartfit_v2.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also {
                    INSTANCE = it
                    Log.d(TAG, "Database instance created")
                }
            }
    }
}
