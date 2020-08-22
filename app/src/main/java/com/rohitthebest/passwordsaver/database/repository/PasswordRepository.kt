package com.rohitthebest.passwordsaver.database.repository

import com.rohitthebest.passwordsaver.database.dao.PasswordDao
import com.rohitthebest.passwordsaver.database.entity.Password
import javax.inject.Inject

class PasswordRepository @Inject constructor(
    var dao: PasswordDao
) {

    suspend fun insert(password: Password) = dao.insert(password)

    suspend fun delete(password: Password) = dao.delete(password)

    suspend fun deleteBySync(isSynced: String) = dao.deleteBySync(isSynced)

    fun getAllPasswords() = dao.getAllPasswordsList()

    fun getPasswordByAccountName(accountName: String) = dao.getPasswordByAccountName(accountName)
}