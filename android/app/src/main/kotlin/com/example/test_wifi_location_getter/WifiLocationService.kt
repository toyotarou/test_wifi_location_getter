package com.example.test_wifi_location_getter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import java.time.*
import java.time.format.DateTimeFormatter
import com.example.test_wifi_location_getter.Coordinate
import com.example.test_wifi_location_getter.CoordinatePushApi

class WifiLocationService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val NOTIF_ID = 1
    private val CHANNEL_ID = "wifi_location"

    ///
    companion object {
        @Volatile
        var coordinateApi: CoordinatePushApi? = null
    }

    ///
    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotif("Scanning…"))
        handler.post(scanTask)
    }

    ///
    override fun onDestroy() {
        handler.removeCallbacks(scanTask)
        super.onDestroy()
    }

    ///
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    ///
    override fun onBind(intent: Intent?): IBinder? = null

    ///
    private val scanTask = object : Runnable {
        override fun run() {
            sendDummy()
            handler.postDelayed(this, 60_000)
        }
    }

    ///
    private fun sendDummy() {
        val now = LocalDateTime.now()

        val coord = Coordinate(
            lat = 35.0 + Math.random(),
            lng = 139.0 + Math.random(),
            ssid = "SampleSSID",
            epochMillis = System.currentTimeMillis(),
            date = now.toLocalDate().toString(),
            time = now.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME)
        )

        coordinateApi?.onCoordinate(coord) {}
    }

    ///
    private fun createChannel() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Wi-Fi Coordinate",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    ///
    private fun buildNotif(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wi-Fi Location")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
}
