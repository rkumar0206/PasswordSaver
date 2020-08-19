package com.rohitthebest.passwordsaver.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.other.Constants
import com.rohitthebest.passwordsaver.other.Constants.SAVED_PASSWORD_SERVICE_MESSAGE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class UploadSavedPasswordService : Service() {

    private val TAG = "SavedPasswordService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val message = intent?.getStringExtra(SAVED_PASSWORD_SERVICE_MESSAGE)

        val gson = Gson()
        val type = object : TypeToken<Password>() {}.type

        val password: Password = gson.fromJson(message, type)

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_baseline_visibility_off_24)
            .setContentTitle("Saving Encrypted Password to Cloud...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        startForeground(12, notification)

        val docRef = FirebaseFirestore.getInstance()
            .collection(getString(R.string.savedPasswords))
            .document(
                "${System.currentTimeMillis().toString(36)}_${Random.nextInt(1000, 1000000)
                    .toString(36)}_${password.uid}"
            )

        CoroutineScope(Dispatchers.IO).launch {

            val flag: Boolean = uploadToFirestore(docRef, password)

            if (flag) {
                Log.i(TAG, "Upload Saved Password Successful")
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun uploadToFirestore(docRef: DocumentReference, password: Password): Boolean {

        return try {

            docRef.set(password)
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