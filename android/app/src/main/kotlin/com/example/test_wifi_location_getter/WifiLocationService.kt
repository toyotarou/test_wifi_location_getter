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


import android.net.wifi.WifiManager
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import com.google.android.gms.location.*
import android.content.Context


class WifiLocationService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val NOTIF_ID = 1
    private val CHANNEL_ID = "wifi_location"


    private lateinit var wifiManager: WifiManager
    private lateinit var fused: FusedLocationProviderClient


    ///
    companion object {
        @Volatile
        var coordinateApi: CoordinatePushApi? = null
    }

    ///
    override fun onCreate() {
        super.onCreate()



        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        fused = LocationServices.getFusedLocationProviderClient(this)



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
//            sendDummy()
            sendReal()

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
    private fun sendReal() {
        if (checkSelfPermission(android.Manifest.permission.NEARBY_WIFI_DEVICES)
            != PackageManager.PERMISSION_GRANTED
        ) return

        wifiManager.startScan()

        val results: List<ScanResult> = wifiManager.scanResults

        val best = results.maxByOrNull { it.level } ?: return

        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc == null) return@addOnSuccessListener
            val now = LocalDateTime.now()
            val coord = Coordinate(
                lat = loc.latitude,
                lng = loc.longitude,
                ssid = best.SSID ?: "(unknown)",
                epochMillis = System.currentTimeMillis(),
                date = now.toLocalDate().toString(),
                time = now.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME)
            )
            coordinateApi?.onCoordinate(coord) {}
        }
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
