package com.rohitthebest.passwordsaver.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.other.Constants.NO_INTERNET_MESSAGE
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        fun showNoInternetMessage(context: Context) {

            showToast(context, NO_INTERNET_MESSAGE)
        }

        fun showKeyboard(activity: Activity, view: View) {
            try {

                val inputMethodManager =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        fun hideKeyBoard(activity: Activity) {

            try {

                CoroutineScope(Dispatchers.Main).launch {

                    Log.i(TAG, "Function: hideKeyboard")
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

        private fun checkUrl(url: String): String {

            var urll = ""
            try {
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    urll = url
                } else if (url.isNotEmpty()) {
                    urll = "https://www.google.com/search?q=$url"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return urll

        }

        fun openLinkInBrowser(url: String?, context: Context) {

            if (isInternetAvailable(context)) {
                url?.let {

                    try {
                        Log.d(TAG, "Loading Url in default browser.")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkUrl(it)))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        showToast(context, e.message.toString())
                        e.printStackTrace()
                    }
                }
            } else {
                showToast(context, NO_INTERNET_MESSAGE)
            }
        }

        @RequiresApi(Build.VERSION_CODES.P)
        inline fun checkForFingerPrintValidation(
            activity: Activity,
            authenticationCallback: BiometricPrompt.AuthenticationCallback,
            crossinline onNegativeBtnClicked: () -> Unit
        ) {

            /**check for fingerprint validation**/

            val biometricPrompt = BiometricPrompt.Builder(activity)
                .setTitle("Please use your fingerprint")
                .setSubtitle("Authentication required")
                .setDescription("This app has fingerprint protection to keep your password secret.")
                .setNegativeButton(
                    "Use your password",
                    activity.mainExecutor
                ) { _, _ ->

                    onNegativeBtnClicked()

                }.build()

            biometricPrompt.authenticate(
                getCancellationSignal(activity),
                activity.mainExecutor,
                authenticationCallback
            )
        }

        fun getCancellationSignal(context: Context): CancellationSignal {

            val cancellationSignal = CancellationSignal()

            cancellationSignal.setOnCancelListener {

                showToast(context, "Authentication was cancelled")
            }

            return cancellationSignal
        }

        // for checking the biometric support in a device
        fun checkBiometricSupport(activity: Activity): Boolean {

            val keyguardManager =
                activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if (!keyguardManager.isKeyguardSecure) {

                showToast(activity, "Fingerprint authentication has not been enabled.")
                return false
            }

            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.USE_BIOMETRIC
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                showToast(activity, "Permission denied")
                return false
            }

            return activity.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        }

        @SuppressLint("CheckResult")
        inline fun checkForPasswordValidation(
            context: Context,
            appSetting: AppSetting,
            negativeButtonText: String,
            crossinline onSuccess: () -> Unit,
            crossinline onFailure: () -> Unit,
            crossinline onNegativeButtonClicked: (MaterialDialog) -> Unit
        ) {

            MaterialDialog(context).show {

                title(text = "Password")
                positiveButton(text = "Confirm")
                cancelOnTouchOutside(false)

                input(
                    hint = "Enter the app password",
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    allowEmpty = false
                ) { _, inputString ->

                    val encryptPassword = EncryptData().encryptWithSHA(inputString.toString())

                    if (encryptPassword == appSetting.appPassword) {

                        onSuccess()

                    } else {

                        onFailure()
                    }
                }
            }.negativeButton(text = negativeButtonText) {

                onNegativeButtonClicked(it)
            }
        }


    }
}