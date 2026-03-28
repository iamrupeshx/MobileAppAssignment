package com.smartfit.app.data.local.db

import androidx.room.*
import com.smartfit.app.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // FIX: changed from ABORT to IGNORE — prevents silent crash on duplicate,
    // returns -1 instead so the ViewModel can show a clean "email already exists" error
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<User?>
}
