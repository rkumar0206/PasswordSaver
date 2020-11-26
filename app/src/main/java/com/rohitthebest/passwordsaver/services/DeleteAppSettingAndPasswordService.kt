package com.rohitthebest.passwordsaver.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.tasks.await

class DeleteAppSettingAndPasswordService : Service() {

    private val TAG = "DeleteApp_Password"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

/*        val messageAppSetting = intent?.getStringExtra(DELETE_APPSETTING_SERVICE_MESSAGE)
        val messagePassword = intent?.getStringExtra(DELETE_PASSWORD_SERVICE_MESSAGE)

        var passwordList: ArrayList<Password>? = ArrayList()

        if (messagePassword != "") {

            passwordList = Functions.convertFromPasswordListJsonToPasswordList(messagePassword)
        }

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_baseline_visibility_off_24)
            .setContentTitle("Deleting all data from Cloud...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(30, notification)

        val appSettingDocRef = FirebaseFirestore.getInstance()
            .collection(getString(R.string.appSetting))
            .document(messageAppSetting!!)*/

/*
        CoroutineScope(Dispatchers.IO).launch {

            val flag = deleteDataFromFireStore(appSettingDocRef)

            if (messagePassword != "") {

                if (passwordList?.isNotEmpty()!!) {

                    for ((i, password) in passwordList.withIndex()) {

                        val docRef = FirebaseFirestore.getInstance()
                            .collection(getString(R.string.savedPasswords))
                            .document(password.key!!)

                        deleteDataFromFireStore(docRef)

                        if (i == (passwordList.size) - 1) {

                            if (flag) {

                                stopSelf()
                                Log.i(TAG, "All Data deleted from firestore.")
                            }
                        }
                    }
                } else if (flag) {

                    stopSelf()
                    Log.i(TAG, "App Setting Deleted from firestore")
                }
            } else if (flag) {

                stopSelf()
                Log.i(TAG, "App Setting Deleted from firestore")
            }
        }
*/

        return START_NOT_STICKY
    }

    private suspend fun deleteDataFromFireStore(
        docRef: DocumentReference
    ): Boolean {

        return try {
            docRef.delete()
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