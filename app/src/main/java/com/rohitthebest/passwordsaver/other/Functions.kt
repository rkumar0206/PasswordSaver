package com.rohitthebest.passwordsaver.other

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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

        /*fun convertDrawableToBitmap(resources: Resources, imageId: Int): Bitmap? {

            var bitmap: Bitmap? = null
            try {
                bitmap = BitmapFactory.decodeResource(resources, imageId)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return bitmap
        }*/


        /*fun shareAsText(message: String?, subject: String?, context: Context) {

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, message)
            context.startActivity(Intent.createChooser(intent, "Share Via"))

        }*/

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

        /* fun showAlertMessage(context: Context, message: String? = "", title: String? = "") {

             AlertDialog.Builder(context)
                 .setTitle(title)
                 .setMessage(message)
                 .setPositiveButton("Ok") { dialog, _ ->

                     dialog.dismiss()
                 }
                 .create()
                 .show()

         }*/

    }
}