package com.rohitthebest.passwordsaver.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_table")
data class Password(
    var siteName: String? = "",
    var userName: String?,
    var password: String?,
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
        "",
        "",
        "",
        null
    )
}