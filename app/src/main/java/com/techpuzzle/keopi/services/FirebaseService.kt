package com.techpuzzle.keopi.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.ui.main.MainActivity
import com.techpuzzle.keopi.utils.Constants.Companion.FIREBASE_NOTIFICATIONS_CHANNEL_ID
import kotlin.random.Random

class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, MainActivity::class.java).also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            message.data.apply {
                it.putExtra("_id", get("_id").toString())
                it.putExtra("address", get("address").toString())
                it.putExtra("bio", get("bio").toString())
                it.putExtra("cjenikId", get("cjenikId").toString())
                it.putExtra("name", get("name").toString())
                it.putExtra("capacity", get("capacity").toString())
                it.putExtra("betting", get("betting").toString())
                it.putExtra("location", get("location").toString())
                it.putExtra("areaId", get("areaId").toString())
                it.putExtra("latitude", get("latitude").toString())
                it.putExtra("longitude", get("longitude").toString())
                it.putExtra("music", get("music").toString())
                it.putExtra("age", get("age").toString())
                it.putExtra("smoking", get("smoking").toString())
                it.putExtra("startingWorkTime", get("startingWorkTime").toString())
                it.putExtra("endingWorkTime", get("endingWorkTime").toString())
                it.putExtra("picture", get("picture").toString())
                it.putExtra("instagram", get("instagram").toString())
                it.putExtra("facebook", get("facebook").toString())
                it.putExtra("terrace", get("terrace").toString())
                it.putExtra("dart", get("dart").toString())
                it.putExtra("billiards", get("billiards").toString())
                it.putExtra("hookah", get("hookah").toString())
                it.putExtra("playground", get("playground").toString())
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            FLAG_ONE_SHOT
        )
        val notification = NotificationCompat.Builder(this, FIREBASE_NOTIFICATIONS_CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        Log.d("FirebaseMessagingServic", message.data.toString())

        notificationManager.notify(notificationID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "keopiChannel"
        val channel = NotificationChannel(
            FIREBASE_NOTIFICATIONS_CHANNEL_ID,
            channelName,
            IMPORTANCE_HIGH
        ).apply {
            description = "Keopi firebase push notifications"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }
}