package com.smartfit.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

// ── Remote response DTOs ──────────────────────────────────────────────────────

data class ExerciseDto(
    @SerializedName("id")           val id: Int = 0,
    @SerializedName("name")         val name: String = "",
    @SerializedName("bodyPart")     val bodyPart: String = "",
    @SerializedName("equipment")    val equipment: String = "",
    @SerializedName("target")       val target: String = "",
    @SerializedName("gifUrl")       val gifUrl: String = "",
    @SerializedName("instructions") val instructions: List<String> = emptyList()
)

data class NinjaExerciseDto(
    @SerializedName("name")         val name: String,
    @SerializedName("type")         val type: String,
    @SerializedName("muscle")       val muscle: String,
    @SerializedName("equipment")    val equipment: String,
    @SerializedName("difficulty")   val difficulty: String,
    @SerializedName("instructions") val instructions: String
)

data class NinjaImageDto(
    @SerializedName("url")          val url: String = ""
)

// ── Retrofit API interfaces ───────────────────────────────────────────────────

interface ExerciseApiService {
    @GET("exercises")
    suspend fun getExercises(
        @Query("limit")  limit: Int  = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<ExerciseDto>>

    @GET("exercises/bodyPart/{bodyPart}")
    suspend fun getExercisesByBodyPart(
        @retrofit2.http.Path("bodyPart") bodyPart: String,
        @Query("limit")  limit: Int = 15
    ): Response<List<ExerciseDto>>
}

interface NinjaApiService {
    @GET("v1/exercises")
    suspend fun getExercises(
        @Header("X-Api-Key") apiKey: String,
        @Query("muscle") muscle: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("offset") offset: Int = 0
    ): Response<List<NinjaExerciseDto>>

    @GET("v1/images")
    suspend fun getImages(
        @Header("X-Api-Key") apiKey: String,
        @Query("query") query: String
    ): Response<List<NinjaImageDto>>
}

// ── Retrofit singleton ────────────────────────────────────────────────────────

object RetrofitClient {
    private const val EXERCISE_DB_URL = "https://exercisedb.p.rapidapi.com/"
    private const val NINJA_API_URL    = "https://api.api-ninjas.com/"

    val apiService: ExerciseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(EXERCISE_DB_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExerciseApiService::class.java)
    }

    val ninjaService: NinjaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NINJA_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NinjaApiService::class.java)
    }
}
