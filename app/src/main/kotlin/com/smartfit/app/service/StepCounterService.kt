package com.smartfit.app.service

import android.app.*
import android.content.*
import android.hardware.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.smartfit.app.MainActivity
import com.smartfit.app.data.local.datastore.UserPreferences
import com.smartfit.app.data.local.db.SmartFitDatabase
import com.smartfit.app.data.model.ActivityLog
import com.smartfit.app.data.repository.ActivityRepository
import com.smartfit.app.data.remote.RetrofitClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class StepCounterService : Service(), SensorEventListener {
    private val TAG = "StepCounterService"
    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "step_counter_channel"

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var repository: ActivityRepository
    private lateinit var userPrefs: UserPreferences
    private var userId: Int = -1

    private var initialStepCount = -1
    private var currentStepsInSession = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, getNotification("Starting step counter..."))

        val db = SmartFitDatabase.getInstance(this)
        userPrefs = UserPreferences(this)
        repository = ActivityRepository(
            db.activityDao(),
            db.userDao(),
            db.foodDao(),
            RetrofitClient.apiService,
            RetrofitClient.ninjaService
        )

        serviceScope.launch {
            userId = userPrefs.loggedInUserId.first()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            Log.e(TAG, "Step counter sensor not available")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceBoot = event.values[0].toInt()

            if (initialStepCount == -1) {
                initialStepCount = totalStepsSinceBoot
            }

            val newSteps = totalStepsSinceBoot - initialStepCount
            if (newSteps > currentStepsInSession) {
                val diff = newSteps - currentStepsInSession
                currentStepsInSession = newSteps
                updateStepsInDb(diff)
                updateNotification("Steps today: $currentStepsInSession")
            }
        }
    }

    private fun updateStepsInDb(diff: Int) {
        if (userId == -1) return
        serviceScope.launch {
            val calories = (diff * 0.04).toInt()
            repository.addActivity(
                ActivityLog(
                    userId = userId,
                    activityType = "Walking",
                    durationMinutes = 1,
                    caloriesBurned = calories,
                    steps = diff,
                    notes = "Auto-detected"
                )
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SmartFit Active")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = getNotification(content)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Step Counter", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
