package com.rohitthebest.passwordsaver.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.*
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.other.Constants.NO_INTERNET_MESSAGE
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

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

        suspend fun generatePasswordPdfDocumentAndExportToStorage(
            activity: Activity,
            passwordList: List<Password>,
            userPassword: String,
            appSecretKey: String
        ) {

            withContext(Dispatchers.IO) {

                val passwordParagraph = makeParagraphOfPassword(passwordList, appSecretKey)

                exportSavedImagesLinkToFile(
                    activity,
                    passwordParagraph,
                    userPassword,
                    System.currentTimeMillis().toString()
                )
            }
        }

        private fun makeParagraphOfPassword(
            passwordList: List<Password>,
            appSecretKey: String
        ): Paragraph {

            val font: Font =
                FontFactory.getFont(FontFactory.COURIER, 14f, BaseColor.BLACK)

            val paragraph = Paragraph("", font)

            if (passwordList.isNotEmpty()) {

                paragraph.add("Passwords (${passwordList.size})\n")
                paragraph.add("---------------------------------------------------\n\n")

                passwordList.forEach { password ->

                    paragraph.add("SiteName : ${password.siteName}\n")
                    paragraph.add("UserName : ${password.userName}\n")
                    paragraph.add(
                        "Password : ${
                            try {
                                EncryptData().decryptAES(password.password, appSecretKey)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                e.message
                            }
                        }\n"
                    )
                    if (password.siteLink.isValid()) paragraph.add("Site Url : ${password.siteLink}\n")
                    paragraph.add("\n---------------------------------------------------\n\n")
                }
            }

            return paragraph
        }

        private suspend fun exportSavedImagesLinkToFile(
            activity: Activity,
            paragraph: Paragraph,
            userPassword: String,
            fileName: String = "password"
        ) {

            withContext(Dispatchers.IO) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    val resolver = activity.contentResolver

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "${Environment.DIRECTORY_DOCUMENTS}/PasswordSaver"
                        )
                    }

                    val uri = resolver?.insert(
                        MediaStore.Files.getContentUri("external"),
                        contentValues
                    )

                    uri?.let { pdfUri ->

                        resolver.openOutputStream(pdfUri).use { fout ->

                            try {

                                makePdfOfPasswordWithEncryption(fout, paragraph, userPassword)

                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }

                    }
                } else {

                    try {
                        val file = Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/PasswordSaver/$fileName.pdf")

                        makePdfOfPasswordWithEncryption(
                            FileOutputStream(file),
                            paragraph,
                            userPassword
                        )

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }
        }

        private suspend fun makePdfOfPasswordWithEncryption(
            fout: OutputStream?,
            passwordParagraph: Paragraph,
            userPassword: String
        ) {
            withContext(Dispatchers.IO) {

                try {
                    val document = Document()

                    Log.d(TAG, "makePdfOfPasswordWithEncryption: $passwordParagraph")

                    val pdfWriter = PdfWriter.getInstance(document, fout)

                    pdfWriter.setEncryption(
                        userPassword.toByteArray(),
                        UUID.randomUUID().toString().toByteArray(),
                        PdfWriter.ALLOW_PRINTING,
                        PdfWriter.ENCRYPTION_AES_128
                    )

                    document.open()

                    document.add(passwordParagraph)

                    document.close()

                    fout?.close()

                } catch (e: java.lang.Exception) {

                    e.printStackTrace()
                }

            }
        }
    }
}