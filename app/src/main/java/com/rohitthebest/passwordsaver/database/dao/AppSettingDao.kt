package com.rohitthebest.passwordsaver.database.dao

import androidx.room.*
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appSetting: AppSetting): Long

    @Delete
    suspend fun delete(appSetting: AppSetting)

    @Query("SELECT * FROM app_setting_table LIMIT 1")
    fun getAppSetting(): Flow<AppSetting>

}