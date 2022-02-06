package com.rohitthebest.passwordsaver.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_table")
data class Password(
    var created: Long = System.currentTimeMillis(),
    var modified: Long = System.currentTimeMillis(),
    var siteName: String = "",
    var userName: String,
    var password: String,
    var key: String,
    var siteLink: String? = ""
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    constructor() : this(
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        "",
        "",
        "",
        "",
        ""
    )
}