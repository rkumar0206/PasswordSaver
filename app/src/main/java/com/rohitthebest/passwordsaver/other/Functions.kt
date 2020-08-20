package com.rohitthebest.passwordsaver.other

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.util.CheckNetworkConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Functions {

    companion object {

        private const val TAG = "Functions"

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

        fun convertToJson(password: Password?): String? {

            val gson = Gson()
            return gson.toJson(password)
        }

        fun convertFromJsonToPassword(jsonString: String): Password? {

            val gson = Gson()
            val type = object : TypeToken<Password?>() {}.type

            return gson.fromJson(jsonString, type)
        }

    }
}