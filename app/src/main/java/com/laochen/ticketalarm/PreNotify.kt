package com.laochen.ticketalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object PreNotify {
    private const val CHANNEL_ID = "pre_channel"

    fun ensureChannel(context: Context) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            val ch = NotificationChannel(CHANNEL_ID, "开抢提醒", NotificationManager.IMPORTANCE_HIGH)
            ch.description = "开抢前提前提醒"
            mgr.createNotificationChannel(ch)
        }
    }

    fun show(context: Context, event: String) {
        ensureChannel(context)
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("即将开抢")
            .setContentText("「$event」马上开始，准备！")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        mgr.notify(2001, n)
    }
}
