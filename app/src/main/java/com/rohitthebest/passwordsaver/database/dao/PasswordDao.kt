package com.rohitthebest.passwordsaver.database.dao

import androidx.room.*
import com.rohitthebest.passwordsaver.database.entity.Password
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(password: Password): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(password: Password)

    @Delete
    suspend fun delete(password: Password)

    @Query("SELECT * FROM password_table ORDER BY modified DESC")
    fun getAllPasswordsList(): Flow<List<Password>>

    @Query("SELECT * FROM password_table WHERE `key` = :passwordKey")
    fun getPasswordByKey(passwordKey: String): Flow<Password>
}