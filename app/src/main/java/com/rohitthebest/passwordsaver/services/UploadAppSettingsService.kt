package com.rohitthebest.passwordsaver.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.other.Constants.APP_SETTING_SERVICE_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.NOTIFICATION_CHANNEL_ID
import com.rohitthebest.passwordsaver.other.Constants.NO_INTERNET_MESSAGE
import com.rohitthebest.passwordsaver.util.Functions.Companion.convertFromJsonToAppSetting
import com.rohitthebest.passwordsaver.util.Functions.Companion.isInternetAvailable
import com.rohitthebest.passwordsaver.util.Functions.Companion.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UploadAppSettingsService : Service() {

    private val TAG = "AppSettingsService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val message = intent?.getStringExtra(APP_SETTING_SERVICE_MESSAGE)

        val appSetting: AppSetting? = convertFromJsonToAppSetting(message)

        val notification = NotificationCompat.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_baseline_visibility_off_24)
            .setContentTitle("Saving...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(10, notification)

        val docRef = FirebaseFirestore.getInstance()
            .collection(getString(R.string.appSetting))
            .document(appSetting?.uid!!)

        if (isInternetAvailable(this)) {
            CoroutineScope(Dispatchers.IO).launch {

                val flag: Boolean = insertDataToFireStore(docRef, appSetting)

                if (flag) {
                    Log.i(TAG, "App password Saved to firestore.")
                    stopSelf()
                }
            }
        } else {
            showToast(this, NO_INTERNET_MESSAGE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private suspend fun insertDataToFireStore(
        docRef: DocumentReference,
        appSetting: AppSetting
    ): Boolean {

        return try {

            docRef.set(appSetting)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    override fun onBind(p0: Intent?): IBinder? {

        return null
    }
}