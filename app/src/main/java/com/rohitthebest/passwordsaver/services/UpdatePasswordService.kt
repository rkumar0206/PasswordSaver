package com.rohitthebest.passwordsaver.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.other.Constants
import com.rohitthebest.passwordsaver.other.Constants.UPDATE_PASSWORD_SERVICE_MESSAGE
import com.rohitthebest.passwordsaver.other.Functions.Companion.convertFromJsonToPassword
import com.rohitthebest.passwordsaver.other.Functions.Companion.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdatePasswordService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val message = intent?.getStringExtra(UPDATE_PASSWORD_SERVICE_MESSAGE)

        val password = convertFromJsonToPassword(message)

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_baseline_visibility_off_24)
            .setContentTitle("Updating Encrypted Password to Cloud...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        startForeground(15, notification)

        val docRef = FirebaseFirestore.getInstance()
            .collection(getString(R.string.savedPasswords))
            .document(password?.key!!)


        val map: MutableMap<String, Any?> = HashMap()

        map["accountName"] = password.accountName
        map["password"] = password.password

        CoroutineScope(Dispatchers.IO).launch {

            val flag = updatePassword(docRef, map)

            if (flag) {

                withContext(Dispatchers.Main) {

                    showToast(this@UpdatePasswordService, "Password Updated")
                    stopSelf()
                }
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun updatePassword(
        docRef: DocumentReference,
        map: MutableMap<String, Any?>
    ): Boolean {

        return try {

            docRef.update(map)
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