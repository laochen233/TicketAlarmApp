package com.laochen.ticketalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AlarmScheduler.ACTION_PRE -> {
                PreNotify.show(context, Prefs.eventName(context))
            }
            AlarmScheduler.ACTION_MAIN -> {
                // 主闹钟：启动前台服务，负责全屏锁屏弹窗 + 震动 + 跳转
                val svc = Intent(context, RingService::class.java)
                context.startForegroundService(svc)
            }
        }
    }
}
