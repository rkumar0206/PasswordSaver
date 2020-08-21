package com.rohitthebest.passwordsaver.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appSettingsTable")
data class AppSetting(
    var mode: String? = "",
    var appPassword: String?,
    var uid: String?,
    var enterPasswordForCopy: String? = "true",
    var enterPasswordForVisibility: String? = "true"
) {

    @PrimaryKey(autoGenerate = false)
    var id: Int? = null

    constructor() : this(
        "",
        "",
        "",
        "true",
        "true"
    )
}