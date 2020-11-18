package com.rohitthebest.passwordsaver.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password

class ConversionWithGson {

    companion object {

        private val gson = Gson()

        fun convertPasswordToJson(password: Password?): String? {

            return gson.toJson(password)
        }


        fun convertFromJsonToPassword(jsonString: String?): Password? {


            val type = object : TypeToken<Password?>() {}.type

            return gson.fromJson(jsonString, type)
        }

        fun convertPasswordListToJson(passwordList: ArrayList<Password>?): String? {

            return gson.toJson(passwordList)
        }

        fun convertFromPasswordListJsonToPasswordList(jsonString: String?): ArrayList<Password>? {

            val type = object : TypeToken<ArrayList<Password?>>() {}.type

            return gson.fromJson(jsonString, type)
        }

        fun convertAppSettingToJson(appSetting: AppSetting?): String? {

            return gson.toJson(appSetting)
        }

        fun convertJsonToAppSetting(jsonString: String?): AppSetting? {

            val type = object : TypeToken<AppSetting?>() {}.type

            return gson.fromJson(jsonString, type)
        }
    }
}