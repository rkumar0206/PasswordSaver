package com.rohitthebest.passwordsaver.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.passwordsaver.database.dao.PasswordDao
import com.rohitthebest.passwordsaver.database.entity.Password

@Database(
    entities = [Password::class],
    version = 1
)
abstract class PasswordDatabase : RoomDatabase() {

    abstract fun getPasswordDao(): PasswordDao
}