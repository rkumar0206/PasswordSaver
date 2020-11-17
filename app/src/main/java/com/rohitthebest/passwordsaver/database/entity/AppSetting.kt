package com.rohitthebest.passwordsaver.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appSettingsTable")
data class AppSetting(
    var mode: String = "",
    var appPassword: String,
    var uid: String?,
    var isPasswordRequiredForCopy: String = "true",
    var isPasswordRequiredForVisibility: String = "true",
    var isFingerprintEnabled: String = "false",
    var key: String
) {

    @PrimaryKey(autoGenerate = false)
    var id: Int? = null

    constructor() : this(
        "",
        "",
        "",
        "true",
        "true",
        "false",
        ""
    )
}