package com.rohitthebest.passwordsaver.database.repository

import com.rohitthebest.passwordsaver.database.dao.AppSettingDao
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import javax.inject.Inject

class AppSettingRepository @Inject constructor(
    var dao: AppSettingDao
) {

    suspend fun insert(appSetting: AppSetting) = dao.insert(appSetting)

    suspend fun delete(appSetting: AppSetting) = dao.delete(appSetting)

    fun getData() = dao.getData()

    fun getAppSettingById(id : Int) = dao.getAppSetting(id)
}