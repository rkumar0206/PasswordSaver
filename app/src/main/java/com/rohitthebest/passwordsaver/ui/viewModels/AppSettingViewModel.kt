package com.rohitthebest.passwordsaver.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.repository.AppSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingViewModel @Inject constructor(
    var repository: AppSettingRepository
) : ViewModel() {

    fun insert(appSetting: AppSetting) = viewModelScope.launch {

        repository.insert(appSetting)
    }

    fun delete(appSetting: AppSetting) = viewModelScope.launch {

        repository.delete(appSetting)
    }

    fun getAppSetting() = repository.getAppSetting().asLiveData()

}