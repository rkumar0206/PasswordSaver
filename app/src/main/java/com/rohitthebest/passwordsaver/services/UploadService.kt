package com.rohitthebest.passwordsaver.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.other.Constants.COLLECTION_KEY
import com.rohitthebest.passwordsaver.other.Constants.DOCUMENT_KEY
import com.rohitthebest.passwordsaver.other.Constants.NOTIFICATION_CHANNEL_ID
import com.rohitthebest.passwordsaver.other.Constants.RANDOM_ID_KEY
import com.rohitthebest.passwordsaver.other.Constants.UPLOAD_DATA_KEY
import com.rohitthebest.passwordsaver.util.ConversionWithGson.Companion.convertFromJsonToPassword
import com.rohitthebest.passwordsaver.util.ConversionWithGson.Companion.convertJsonToAppSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UploadService : Service() {

    private val TAG = "UploadService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(COLLECTION_KEY)
        val key = intent?.getStringExtra(DOCUMENT_KEY)
        val uploadData = intent?.getStringExtra(UPLOAD_DATA_KEY)
        val randomId = intent?.getIntExtra(RANDOM_ID_KEY, 1003)

        val image = if (collection == getString(R.string.appSetting)) {

            R.drawable.ic_baseline_settings_24
        } else {
            R.drawable.ic_baseline_visibility_24
        }

        val notification = NotificationCompat.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(image)
            .setContentTitle("Uploading app settings to cloud.")
            .build()

        startForeground(randomId!!, notification)

        val docRef = FirebaseFirestore.getInstance()
            .collection(collection!!)
            .document(key!!)

        CoroutineScope(Dispatchers.IO).launch {

            when (collection) {

                getString(R.string.appSetting) -> {

                    if (convertJsonToAppSetting(uploadData)?.let {
                            insertToFireStore(
                                docRef,
                                it
                            )
                        }!!) {

                        Log.d(
                            TAG,
                            "onStartCommand: Uploaded appSetting to collection $collection with key : $key"
                        )
                        stopSelf()
                    }
                }

                getString(R.string.savedPasswords) -> {

                    if (insertToFireStore(docRef, convertFromJsonToPassword(uploadData))) {

                        Log.d(
                            TAG,
                            "onStartCommand: Uploaded This category to collection $collection and key $key"
                        )
                        stopSelf()
                    }
                }

                else -> {

                    stopSelf()
                }
            }

        }


        return START_NOT_STICKY
    }

    private suspend fun insertToFireStore(docRef: DocumentReference, data: Any): Boolean {

        return try {

            Log.i(TAG, "insertToFireStore")

            docRef.set(data)
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