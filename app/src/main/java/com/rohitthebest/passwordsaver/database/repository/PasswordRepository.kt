package com.rohitthebest.passwordsaver.database.repository

import com.rohitthebest.passwordsaver.database.dao.PasswordDao
import com.rohitthebest.passwordsaver.database.entity.Password
import javax.inject.Inject

class PasswordRepository @Inject constructor(
    var dao: PasswordDao
) {

    suspend fun insert(password: Password) = dao.insert(password)

    suspend fun update(password: Password) = dao.update(password)

    suspend fun delete(password: Password) = dao.delete(password)

    fun getAllPasswords() = dao.getAllPasswordsList()

    fun getPasswordByKey(passwordKey: String) = dao.getPasswordByKey(passwordKey)
}