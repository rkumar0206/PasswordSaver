package com.rohitthebest.passwordsaver.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rohitthebest.passwordsaver.database.entity.Password

@Dao
interface PasswordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(password: Password): Long

    @Delete
    suspend fun delete(password: Password)

    @Query("SELECT * FROM password_table ORDER BY timeStamp DESC")
    fun getAllPasswordsList(): LiveData<List<Password>>

    @Query("SELECT * FROM password_table WHERE userName = :accountName")
    fun getPasswordByAccountName(accountName: String): LiveData<Password>

    @Query("DELETE FROM password_table WHERE isSynced = :isSynced")
    suspend fun deleteBySync(isSynced: String)
}