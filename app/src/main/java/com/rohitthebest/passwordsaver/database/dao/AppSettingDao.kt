package com.rohitthebest.passwordsaver.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.other.Constants.APP_SETTING_ID

@Dao
interface AppSettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appSetting: AppSetting): Long

    @Delete
    suspend fun delete(appSetting: AppSetting)

    @Query("SELECT * FROM appSettingsTable")
    fun getData(): LiveData<AppSetting>

    @Query("SELECT * FROM appSettingsTable WHERE id= :id ")
    fun getAppSetting(id : Int) : LiveData<List<AppSetting>>
}