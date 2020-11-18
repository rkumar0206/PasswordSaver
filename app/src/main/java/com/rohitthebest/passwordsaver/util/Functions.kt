package com.rohitthebest.passwordsaver.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Functions {

    companion object {

        private const val TAG = "Functions"
        val gson = Gson()

        fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
            try {
                Log.d(TAG, message)
                Toast.makeText(context, message, duration).show()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        fun isInternetAvailable(context: Context): Boolean {

            return CheckNetworkConnection().isInternetAvailable(context)
        }

        fun encryptPassword(data: String): String? {

            return try {

                EncryptData().encryptWithSHA(data)

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                ""
            }
        }

        fun hideKeyBoard(activity: Activity) {

            try {

                GlobalScope.launch {

                    closeKeyboard(activity)
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }


        suspend fun closeKeyboard(activity: Activity) {
            try {
                withContext(Dispatchers.IO) {

                    val view = activity.currentFocus

                    if (view != null) {

                        val inputMethodManager =
                            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                    }

                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

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

        fun convertFromJsonToAppSetting(jsonString: String?): AppSetting? {

            val type = object : TypeToken<AppSetting?>() {}.type

            return gson.fromJson(jsonString, type)
        }


    }
}