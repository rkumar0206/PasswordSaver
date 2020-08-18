package com.rohitthebest.passwordsaver.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.repository.AppSettingRepository
import com.rohitthebest.passwordsaver.other.Constants.APP_SETTING_ID
import kotlinx.coroutines.launch

class AppSettingViewModel @ViewModelInject constructor(
    var repository: AppSettingRepository
) : ViewModel() {

    fun insert(appSetting: AppSetting) = viewModelScope.launch {

        repository.insert(appSetting)
    }

    fun delete(appSetting: AppSetting) = viewModelScope.launch {

        repository.delete(appSetting)
    }

    fun getData() = repository.getData()

    fun getAppSettingByID() = repository.getAppSettingById(APP_SETTING_ID)

}