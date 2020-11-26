package com.rohitthebest.passwordsaver.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rohitthebest.passwordsaver.other.Constants.OFFLINE

@Entity(tableName = "password_table")
data class Password(
    var siteName: String? = "",
    var userName: String?,
    var password: String?,
    var mode: String = OFFLINE,
    var uid: String?,
    var isSynced: String,
    var key: String?,
    var timeStamp: Long? = System.currentTimeMillis()
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    constructor() : this(
        "",
        "",
        "",
        OFFLINE,
        "",
        "",
        "",
        null
    )
}