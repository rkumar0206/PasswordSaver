package com.rohitthebest.passwordsaver.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.database.repository.PasswordRepository
import kotlinx.coroutines.launch

class PasswordViewModel @ViewModelInject constructor(
    var repository: PasswordRepository
) : ViewModel() {

    fun insert(password: Password) = viewModelScope.launch {

        repository.insert(password)
    }

    fun delete(password: Password) = viewModelScope.launch {

        repository.delete(password)
    }

    fun getAllPasswordsList() = repository.getAllPasswords()

    fun getPasswordByAccountName(accountName: String) =
        repository.getPasswordByAccountName(accountName)
}