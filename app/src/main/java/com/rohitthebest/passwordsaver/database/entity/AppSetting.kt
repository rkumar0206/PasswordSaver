package com.rohitthebest.passwordsaver.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_setting_table")
data class AppSetting(
    var appPassword: String,
    var secretKey: String,
    var securityQuestion: String,
    var securityAnswer: String,
    var isPasswordRequiredForDeleting: Boolean = true,
    var isFingerprintEnabled: Boolean = false,
    var key: String
) {

    @PrimaryKey(autoGenerate = false)
    var id: Int? = null

    constructor() : this(
        "",
        "",
        "",
        "",
        true,
        false,
        ""
    )
}