package org.scrollloom.service.capture

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.scrollloom.App
import org.scrollloom.R

class LoomMediaProjectionService : Service() {

    private var currentProjection: MediaProjection? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopProjection()
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            ?: Activity.RESULT_CANCELED

        @Suppress("DEPRECATION")
        val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA)
        }

        if (resultCode == Activity.RESULT_OK && resultData != null) {
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val projection = projectionManager.getMediaProjection(resultCode, resultData)
            if (projection != null) {
                currentProjection = projection
                MediaProjectionHolder.set(projection)
                projection.registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() {
                        Log.i(TAG, "MediaProjection stopped by system")
                        stopProjection()
                        stopSelf()
                    }
                }, null)
                App.instance.appComponent.loomRepository.updateProjectionGranted(true)
                Log.i(TAG, "MediaProjection acquired successfully")
            } else {
                Log.e(TAG, "getMediaProjection returned null")
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun stopProjection() {
        try {
            currentProjection?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping MediaProjection", e)
        }
        currentProjection = null
        MediaProjectionHolder.clear()
        App.instance.appComponent.loomRepository.updateProjectionGranted(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProjection()
        Log.i(TAG, "LoomMediaProjectionService destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ScrollLoom 屏幕捕获",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持 Android 10 离线屏幕捕获通道"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ScrollLoom 截屏服务运行中")
            .setContentText("正在保持截屏通道（100% 离线，无网络权限）")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "LoomProjService"
        private const val CHANNEL_ID = "loom_projection_channel"
        private const val NOTIFICATION_ID = 2001

        const val ACTION_START = "org.scrollloom.action.START_PROJECTION"
        const val ACTION_STOP = "org.scrollloom.action.STOP_PROJECTION"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"

        fun start(context: Context, resultCode: Int, data: Intent) {
            val intent = Intent(context, LoomMediaProjectionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, data)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LoomMediaProjectionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
