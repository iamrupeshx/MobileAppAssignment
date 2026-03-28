package com.smartfit.app.data.repository

import android.util.Log
import com.smartfit.app.data.local.db.*
import com.smartfit.app.data.model.*
import com.smartfit.app.data.remote.ExerciseApiService
import com.smartfit.app.data.remote.NinjaApiService
import com.smartfit.app.util.CalorieUtil
import com.smartfit.app.util.DateUtil
import kotlinx.coroutines.flow.Flow

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val userDao: UserDao,
    private val foodDao: FoodDao,
    private val apiService: ExerciseApiService,
    private val ninjaService: NinjaApiService
) {
    private val TAG = "ActivityRepository"
    private val NINJA_API_KEY = "5PaNI8tiZgfXUbKVA2xAmbboHfd0uf5mzMP8UZ7d"

    // ── Activity CRUD ────────────────────────────────────────────────
    fun getActivities(userId: Int): Flow<List<ActivityLog>> = activityDao.getActivitiesForUser(userId)

    suspend fun addActivity(activity: ActivityLog): Long {
        Log.d(TAG, "Inserting activity: ${activity.activityType} for user ${activity.userId}")
        return activityDao.insert(activity)
    }
    suspend fun updateActivity(activity: ActivityLog) {
        Log.d(TAG, "Updating activity id=${activity.id}")
        activityDao.update(activity)
    }
    suspend fun deleteActivity(activity: ActivityLog) {
        Log.d(TAG, "Deleting activity id=${activity.id}")
        activityDao.delete(activity)
    }

    // ── Food CRUD ────────────────────────────────────────────────────
    fun getFoodLogs(userId: Int): Flow<List<FoodLog>> = foodDao.getAllForUser(userId)
    fun getFoodLogsSince(userId: Int, fromMs: Long): Flow<List<FoodLog>> = foodDao.getSince(userId, fromMs)

    suspend fun addFoodLog(food: FoodLog): Long {
        Log.d(TAG, "Inserting food: ${food.foodName} (${food.calories} kcal) for user ${food.userId}")
        return foodDao.insert(food)
    }
    suspend fun deleteFoodLog(food: FoodLog) {
        Log.d(TAG, "Deleting food log id=${food.id}")
        foodDao.delete(food)
    }
    suspend fun getTodayFoodCalories(userId: Int): Int =
        foodDao.totalCaloriesSince(userId, DateUtil.todayStartMs()) ?: 0

    // ── Aggregates ───────────────────────────────────────────────────
    suspend fun getTodayBurnedCalories(userId: Int): Int =
        activityDao.totalCaloriesSince(userId, DateUtil.todayStartMs()) ?: 0
    suspend fun getTodaySteps(userId: Int): Int =
        activityDao.totalStepsSince(userId, DateUtil.todayStartMs()) ?: 0
    suspend fun getWeeklyBurned(userId: Int): Int =
        activityDao.totalCaloriesSince(userId, DateUtil.weekStartMs()) ?: 0

    // ── Users ────────────────────────────────────────────────────────
    suspend fun registerUser(user: User): Long {
        Log.d(TAG, "Registering user: ${user.email}")
        return userDao.insert(user)
    }
    suspend fun findUserByEmail(email: String): User? {
        Log.d(TAG, "Looking up user by email: $email")
        return userDao.findByEmail(email)
    }
    suspend fun findUserById(id: Int): User? = userDao.findById(id)
    fun observeUser(id: Int): Flow<User?> = userDao.observeById(id)
    suspend fun updateUser(user: User) {
        Log.d(TAG, "Updating user id=${user.id}")
        userDao.update(user)
    }

    // ── Workout Suggestions (Using Ninja API for text and image) ──────────────────
    suspend fun fetchWorkoutSuggestions(bodyPartId: Int? = null): List<WorkoutSuggestion> {
        Log.d(TAG, "Fetching workout suggestions from Ninja API, bodyPartId=$bodyPartId")
        return try {
            val muscle = bodyPartIdToMuscle(bodyPartId)
            val response = ninjaService.getExercises(NINJA_API_KEY, muscle = muscle)
            
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val exercises = response.body()!!
                exercises.mapIndexed { index, dto ->
                    val name = dto.name.replaceFirstChar { it.uppercase() }
                    
                    // Fetch real image from Ninja Image API
                    val imgResponse = ninjaService.getImages(NINJA_API_KEY, name)
                    val imgUrl = if (imgResponse.isSuccessful && !imgResponse.body().isNullOrEmpty()) {
                        imgResponse.body()!![0].url
                    } else {
                        getDiverseFallbackImage(name, dto.muscle)
                    }

                    WorkoutSuggestion(
                        id                = index,
                        name              = name,
                        category          = dto.muscle.replaceFirstChar { it.uppercase() },
                        difficulty        = dto.difficulty.replaceFirstChar { it.uppercase() },
                        durationMinutes   = (15..45).random(),
                        estimatedCalories = CalorieUtil.estimate(dto.muscle, 30, 70f),
                        imageUrl          = imgUrl,
                        description       = dto.instructions
                    )
                }
            } else {
                fetchFallbackSuggestions(bodyPartId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error fetching Ninja suggestions: ${e.message}", e)
            fetchFallbackSuggestions(bodyPartId)
        }
    }

    private suspend fun fetchFallbackSuggestions(bodyPartId: Int?): List<WorkoutSuggestion> {
        return try {
            val response = if (bodyPartId != null) {
                apiService.getExercisesByBodyPart(bodyPartIdToName(bodyPartId))
            } else {
                apiService.getExercises(limit = 20)
            }
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                response.body()!!.map { dto ->
                    val name = dto.name.replaceFirstChar { it.uppercase() }
                    WorkoutSuggestion(
                        id                = dto.id,
                        name              = name,
                        category          = dto.bodyPart.replaceFirstChar { it.uppercase() },
                        difficulty        = inferDifficulty(dto.target),
                        durationMinutes   = (20..45).random(),
                        estimatedCalories = CalorieUtil.estimate(dto.target, 30, 70f),
                        imageUrl          = getDiverseFallbackImage(name, dto.bodyPart),
                        description       = dto.instructions.joinToString("\n\n")
                    )
                }
            } else {
                offlineSuggestions()
            }
        } catch (e: Exception) {
            offlineSuggestions()
        }
    }

    private fun getDiverseFallbackImage(name: String, category: String = ""): String {
        val n = name.lowercase()
        val c = category.lowercase()
        
        val specific = when {
            n.contains("pickshaw carry") -> "https://images.unsplash.com/photo-1590239062391-808bef0e44b8?auto=format&fit=crop&w=800"
            n.contains("palms-down wrist curl") -> "https://images.unsplash.com/photo-1581009146145-b5ef03a7403f?auto=format&fit=crop&w=800"
            n.contains("front raise") && n.contains("lateral raise") -> "https://images.unsplash.com/photo-1541534741688-6078c6bd35e5?auto=format&fit=crop&w=800"
            n.contains("wrist roll up") -> "https://images.unsplash.com/photo-1591948971350-383b9f5a56ef?auto=format&fit=crop&w=800"
            n.contains("incline hammer curl") -> "https://images.unsplash.com/photo-1581009146145-b5ef03a7403f?auto=format&fit=crop&w=800"
            n.contains("push up") || n.contains("push-up") -> "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?auto=format&fit=crop&w=800"
            n.contains("bench press") -> "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800"
            n.contains("squat") -> "https://images.unsplash.com/photo-1574680096145-d05b474e2155?auto=format&fit=crop&w=800"
            n.contains("plank") -> "https://images.unsplash.com/photo-1566241142559-40e1bfc26ddc?auto=format&fit=crop&w=800"
            n.contains("burpee") -> "https://images.unsplash.com/photo-1599058917233-97f8474860cc?auto=format&fit=crop&w=800"
            n.contains("jumping jack") -> "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800"
            n.contains("dumbbell row") || n.contains("bent over row") -> "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?auto=format&fit=crop&w=800"
            n.contains("lunge") -> "https://images.unsplash.com/photo-1434608519344-49d77a699e1d?auto=format&fit=crop&w=800"
            n.contains("mountain climber") -> "https://images.unsplash.com/photo-1598971639058-fab3c023bf36?auto=format&fit=crop&w=800"
            n.contains("shoulder press") || n.contains("overhead press") -> "https://images.unsplash.com/photo-1541534741688-6078c6bd35e5?auto=format&fit=crop&w=800"
            n.contains("dip") -> "https://images.unsplash.com/photo-1590239062391-808bef0e44b8?auto=format&fit=crop&w=800"
            n.contains("deadlift") -> "https://images.unsplash.com/photo-1603503363848-69525251458d?auto=format&fit=crop&w=800"
            n.contains("curl") -> "https://images.unsplash.com/photo-1581009146145-b5ef03a7403f?auto=format&fit=crop&w=800"
            n.contains("pull up") || n.contains("pull-up") -> "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=800"
            n.contains("running") || n.contains("run") -> "https://images.unsplash.com/photo-1502904550040-7534597429ae?auto=format&fit=crop&w=800"
            n.contains("yoga") || n.contains("stretch") -> "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?auto=format&fit=crop&w=800"
            n.contains("crunch") || n.contains("sit up") -> "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800"
            else -> null
        }
        if (specific != null) return specific

        return when {
            c.contains("chest") -> "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800"
            c.contains("back") || c.contains("lats") || c.contains("traps") -> "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?auto=format&fit=crop&w=800"
            c.contains("quad") || c.contains("hamstring") || c.contains("glute") || c.contains("calve") || c.contains("leg") -> "https://images.unsplash.com/photo-1434608519344-49d77a699e1d?auto=format&fit=crop&w=800"
            c.contains("bicep") || c.contains("tricep") || c.contains("forearm") || c.contains("arm") -> "https://images.unsplash.com/photo-1581009146145-b5ef03a7403f?auto=format&fit=crop&w=800"
            c.contains("shoulder") || c.contains("deltoid") -> "https://images.unsplash.com/photo-1541534741688-6078c6bd35e5?auto=format&fit=crop&w=800"
            c.contains("abs") || c.contains("abdominals") || c.contains("core") || c.contains("waist") -> "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800"
            c.contains("cardio") -> "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800"
            else -> "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800"
        }
    }

    private fun bodyPartIdToName(id: Int) = when (id) {
        1 -> "chest"; 2 -> "back"; 3 -> "upper legs"; 4 -> "upper arms"
        5 -> "waist"; 6 -> "shoulders"; 7 -> "cardio"; else -> "cardio"
    }

    private fun bodyPartIdToMuscle(id: Int?) = when (id) {
        1 -> "chest"; 2 -> "lats"; 3 -> "quadriceps"; 4 -> "biceps"
        5 -> "abdominals"; 6 -> "shoulders"; else -> null
    }

    private fun inferDifficulty(target: String) = when (target.lowercase()) {
        "cardiovascular system", "abs" -> "Beginner"
        "quads", "hamstrings", "glutes", "lats", "pectorals" -> "Intermediate"
        "triceps", "biceps", "deltoids" -> "Intermediate"
        else -> listOf("Beginner", "Intermediate", "Advanced").random()
    }
    private fun offlineSuggestions() = listOf(
        WorkoutSuggestion(1, "Push-Ups", "Chest", "Beginner", 15, 80, "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?auto=format&fit=crop&w=800", "1. Start in a plank position.\n2. Lower your body until your chest nearly touches the floor.\n3. Push back up to the starting position."),
        WorkoutSuggestion(2, "Squats", "Legs", "Beginner", 20, 120, "https://images.unsplash.com/photo-1574680096145-d05b474e2155?auto=format&fit=crop&w=800", "1. Stand with feet shoulder-width apart.\n2. Lower your hips as if sitting in a chair.\n3. Keep your chest up and back straight.\n4. Return to standing."),
        WorkoutSuggestion(3, "Plank", "Core", "Beginner", 10, 60, "https://images.unsplash.com/photo-1566241142559-40e1bfc26ddc?auto=format&fit=crop&w=800", "1. Assume a push-up position but rest on your forearms.\n2. Keep your body in a straight line.\n3. Tighten your core and hold."),
        WorkoutSuggestion(4, "Burpees", "Full Body", "Advanced", 20, 200, "https://images.unsplash.com/photo-1599058917233-97f8474860cc?auto=format&fit=crop&w=800", "1. Start standing, drop into a squat.\n2. Kick feet back into a plank.\n3. Do a push-up, jump feet back to squat.\n4. Explosively jump up."),
        WorkoutSuggestion(5, "Jumping Jacks", "Cardio", "Beginner", 15, 100, "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800", "1. Stand with feet together.\n2. Jump while spreading legs and clapping hands overhead.\n3. Jump back to start."),
        WorkoutSuggestion(6, "Dumbbell Rows", "Back", "Intermediate", 25, 140, "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?auto=format&fit=crop&w=800", "1. Bend over at the waist with a flat back.\n2. Pull the weight to your hip.\n3. Lower with control."),
        WorkoutSuggestion(7, "Lunges", "Legs", "Intermediate", 20, 130, "https://images.unsplash.com/photo-1434608519344-49d77a699e1d?auto=format&fit=crop&w=800", "1. Step forward with one leg.\n2. Lower hips until both knees are bent at 90 degrees.\n3. Push back to start."),
        WorkoutSuggestion(8, "Mountain Climbers", "Core", "Intermediate", 15, 160, "https://images.unsplash.com/photo-1598971639058-fab3c023bf36?auto=format&fit=crop&w=800", "1. Start in a plank.\n2. Alternate driving knees toward your chest as fast as possible."),
        WorkoutSuggestion(9, "Shoulder Press", "Shoulders", "Intermediate", 25, 110, "https://images.unsplash.com/photo-1541534741688-6078c6bd35e5?auto=format&fit=crop&w=800", "1. Hold weights at shoulder height.\n2. Press upward until arms are straight.\n3. Lower slowly."),
        WorkoutSuggestion(10, "Tricep Dips", "Arms", "Beginner", 15, 90, "https://images.unsplash.com/photo-1590239062391-808bef0e44b8?auto=format&fit=crop&w=800", "1. Use a bench or chair.\n2. Lower your body by bending elbows.\n3. Push back up using only your triceps."),
    )
}
