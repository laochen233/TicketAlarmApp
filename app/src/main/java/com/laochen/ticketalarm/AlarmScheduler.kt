package com.laochen.ticketalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {
    const val ACTION_MAIN = "com.laochen.ticketalarm.ACTION_MAIN_ALARM"
    const val ACTION_PRE = "com.laochen.ticketalarm.ACTION_PRE_ALARM"
    private const val REQ_MAIN = 1001
    private const val REQ_PRE = 1002

    private fun pi(context: Context, action: String, req: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply { this.action = action }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getBroadcast(context, req, intent, flags)
    }

    fun schedule(context: Context, triggerAt: Long, preMin: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi(context, ACTION_MAIN, REQ_MAIN))
        if (preMin > 0) {
            val preAt = triggerAt - preMin * 60_000L
            if (preAt > System.currentTimeMillis()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, preAt, pi(context, ACTION_PRE, REQ_PRE))
            }
        }
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pi(context, ACTION_MAIN, REQ_MAIN))
        am.cancel(pi(context, ACTION_PRE, REQ_PRE))
    }
}
