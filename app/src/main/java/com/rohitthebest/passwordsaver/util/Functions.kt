package com.rohitthebest.passwordsaver.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.passwordsaver.other.Constants.NO_INTERNET_MESSAGE
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Functions {

    companion object {

        private const val TAG = "Functions"
        private val mAuth = Firebase.auth

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

        fun showNoInternetMessage(context: Context) {

            showToast(context, NO_INTERNET_MESSAGE)
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

        fun copyToClipBoard(activity: Activity, text: String) {

            val clipboardManager =
                activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val clipData = ClipData.newPlainText("url", text)

            clipboardManager.setPrimaryClip(clipData)

            showToast(activity, "copied")

        }

        fun getUid(): String? {

            if (mAuth.currentUser == null) {

                return ""
            }

            return mAuth.currentUser?.uid
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

        fun View.show() {

            try {
                this.visibility = View.VISIBLE

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        fun View.hide() {

            try {
                this.visibility = View.GONE

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun Long.toStringM(radix: Int = 0): String {

            val values = arrayOf(
                "0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h",
                "i",
                "j",
                "k",
                "l",
                "m",
                "n",
                "o",
                "p",
                "q",
                "r",
                "s",
                "t",
                "u",
                "v",
                "w",
                "x",
                "y",
                "z",
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
                "H",
                "I",
                "J",
                "K",
                "L",
                "M",
                "N",
                "O",
                "P",
                "Q",
                "R",
                "S",
                "T",
                "U",
                "V",
                "W",
                "X",
                "Y",
                "Z",
                "!",
                "@",
                "#",
                "$",
                "%",
                "^",
                "&"
            )
            var str = ""
            var d = this
            var r: Int

            if (radix in 1..69) {

                if (d <= 0) {
                    return d.toString()
                }

                while (d != 0L) {

                    r = (d % radix).toInt()
                    d /= radix
                    str = values[r] + str
                }

                return str
            }

            return d.toString()
        }
    }
}