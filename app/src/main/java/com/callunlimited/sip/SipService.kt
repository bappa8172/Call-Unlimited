package com.callunlimited.sip

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.callunlimited.data.CredentialApi
import com.callunlimited.data.RemoteConfigManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SipService : Service() {

    @Inject
    lateinit var sipManager: SipManager

    @Inject
    lateinit var credentialApi: CredentialApi

    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        // Android 14 (API 34) requires specifying foreground service type if defined in manifest
        if (Build.VERSION.SDK_INT >= 30) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fetchCredentialsAndRegister()
        return START_STICKY
    }

    private fun fetchCredentialsAndRegister() {
        serviceScope.launch {
            try {
                remoteConfigManager.fetchAndActivate()
                val credUrl = remoteConfigManager.getCredentialUrl()
                val creds = credentialApi.getCredentials(credUrl)
                
                sipManager.register(
                    username = creds.username,
                    domain = "${creds.server}:${creds.port}",
                    password = creds.password
                )
            } catch (e: Exception) {
                Log.e("SipService", "Failed to fetch credentials", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Unlimited Background Service",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "SipServiceChannel"
        private const val NOTIFICATION_ID = 1
    }
}
