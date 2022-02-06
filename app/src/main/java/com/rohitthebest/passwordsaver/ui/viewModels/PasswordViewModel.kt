package com.rohitthebest.passwordsaver.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.database.repository.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordViewModel @Inject constructor(
    var repository: PasswordRepository
) : ViewModel() {

    fun insert(password: Password) = viewModelScope.launch {

        repository.insert(password)
    }

    fun delete(password: Password) = viewModelScope.launch {

        repository.delete(password)
    }

    fun update(password: Password) = viewModelScope.launch {
        repository.update(password)
    }

    fun getAllPasswordsList() = repository.getAllPasswords().asLiveData()

    fun getPasswordByKey(passwordKey: String) =
        repository.getPasswordByKey(passwordKey).asLiveData()

}