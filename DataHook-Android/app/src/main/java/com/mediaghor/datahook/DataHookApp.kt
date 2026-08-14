package com.mediaghor.datahook

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.mediaghor.datahook.di.AppContainer

class DataHookApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.default_notification_channel_id),
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Alerts when your endpoints receive new submissions."
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
