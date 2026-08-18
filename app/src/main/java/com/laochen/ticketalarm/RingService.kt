package com.laochen.ticketalarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat

class RingService : Service() {

    companion object {
        const val CHANNEL_ID = "ring_channel"
        const val NOTIF_ID = 3001
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val event = Prefs.eventName(this)

        // 震动
        vibrate()

        // 跳转意图（全屏）
        val jumpIntent = Intent(this, JumpActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val fullScreenPi = PendingIntent.getActivity(
            this, 0, jumpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("开抢！")
            .setContentText("「$event」已开始，立即抢票")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPi, true)
            .setAutoCancel(true)
            .build()

        startForeground(NOTIF_ID, notif)

        // 60 秒后自动收尾（若用户已跳转走）
        Handler(Looper.getMainLooper()).postDelayed({
            stopForeground(true)
            stopSelf()
        }, 60_000L)

        return START_NOT_STICKY
    }

    private fun ensureChannel() {
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            val ch = NotificationChannel(
                CHANNEL_ID, "开抢提醒（前台）",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "到点全屏弹窗 + 震动"
                setBypassDnd(true)
                setShowBadge(true)
            }
            mgr.createNotificationChannel(ch)
        }
    }

    private fun vibrate() {
        val pattern = longArrayOf(0, 400, 200, 400, 200, 800)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
