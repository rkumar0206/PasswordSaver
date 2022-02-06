package com.rohitthebest.passwordsaver.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.passwordsaver.database.dao.AppSettingDao
import com.rohitthebest.passwordsaver.database.entity.AppSetting

@Database(
    entities = [AppSetting::class],
    version = 1,
    exportSchema = false
)
abstract class AppSettingDatabase : RoomDatabase() {

    abstract fun getAppSettingsDao(): AppSettingDao
}