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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DeleteService : Service() {

    private val TAG = "DeleteService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(COLLECTION_KEY)
        val key = intent?.getStringExtra(DOCUMENT_KEY)
        val randomId = intent?.getIntExtra(RANDOM_ID_KEY, 1346)

        val notification = NotificationCompat.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_baseline_delete_24)
            .setContentTitle("Deleting from cloud.")
            .build()

        startForeground(randomId!!, notification)

        val docRef = FirebaseFirestore.getInstance()
            .collection(collection!!)
            .document(key!!)

        CoroutineScope(Dispatchers.IO).launch {

            if (deleteFromFireStore(docRef)) {

                stopSelf()
                Log.d(TAG, "onStartCommand: document deleted from collection $collection")
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun deleteFromFireStore(docRef: DocumentReference): Boolean {

        return try {

            docRef.delete().await()
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