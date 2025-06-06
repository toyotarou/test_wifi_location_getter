package com.example.test_wifi_location_getter

import io.flutter.embedding.android.FlutterActivity


import android.content.Intent
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.FlutterEngine

import com.example.test_wifi_location_getter.CoordinatePushApi
import com.example.test_wifi_location_getter.ServiceControlApi


class MainActivity : FlutterActivity() {

    ///
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val pushApi = CoordinatePushApi(flutterEngine.dartExecutor.binaryMessenger)
        WifiLocationService.coordinateApi = pushApi

        ServiceControlApi.setUp(
            flutterEngine.dartExecutor.binaryMessenger,
            object : ServiceControlApi {
                override fun startService() {
                    val i = Intent(this@MainActivity, WifiLocationService::class.java)
                    ContextCompat.startForegroundService(this@MainActivity, i)
                }

                override fun stopService() {
                    stopService(Intent(this@MainActivity, WifiLocationService::class.java))
                }
            }
        )
    }

}
