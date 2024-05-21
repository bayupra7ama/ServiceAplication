package com.example.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyForegroundService : Service() {

    companion object{
        private const val NOTIFICATION_ID = 1
        private const val CHANEL_ID = "chanel_01"
        private const val CHANEL_NAME = "chanel_bayu"
        internal val TAG = MyForegroundService::class.java.simpleName
    }

    val serviceJob = Job()
    val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    override fun onBind(intent: Intent): IBinder {
       throw UnsupportedOperationException("not yet implement")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()

//        startForeground(NOTIFICATION_ID, notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            startForeground(NOTIFICATION_ID,notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        }else{
            startForeground(NOTIFICATION_ID,notification)
        }

        Log.d(TAG, "onStartCommand: Service Dijalankan")
        serviceScope.launch {
            for (i in 1 .. 50 ){
                delay(1000)
                Log.d(TAG, "Do something: $i ")
            }


            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
            stopForeground(STOP_FOREGROUND_DETACH)
         }else {
            stopForeground(true)
            }
            stopSelf()
            Log.d(TAG, "Service dihentikan")
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()

        Log.d(TAG, "onDestroy: service dihentikan")

    }

    private fun buildNotification() : Notification{
        val notificationIntent = Intent(this , MainActivity::class.java)
        val pendingFlag : Int = if (Build.VERSION.SDK_INT >= 23){
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,pendingFlag)

        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, CHANEL_ID)
            .setContentTitle("ForeGroundService")
            .setContentText("saat ini foreground service sedang berjalan.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            notificationBuilder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANEL_ID, CHANEL_NAME,NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANEL_NAME
            notificationBuilder.setChannelId(CHANEL_ID)
            mNotificationManager.createNotificationChannel(channel)
        }
        return notificationBuilder.build()
    }
}