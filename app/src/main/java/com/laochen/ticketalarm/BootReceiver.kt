package com.laochen.ticketalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action ||
            Intent.ACTION_LOCKED_BOOT_COMPLETED == intent.action
        ) {
            if (Prefs.isSet(context)) {
                val trigger = Prefs.triggerAt(context)
                if (trigger > System.currentTimeMillis()) {
                    AlarmScheduler.schedule(context, trigger, Prefs.preMin(context))
                }
            }
        }
    }
}
