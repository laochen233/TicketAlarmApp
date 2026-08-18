package com.laochen.ticketalarm

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val NAME = "ticketalarm"
    private const val K_SET = "set"
    private const val K_EVENT = "event_name"
    private const val K_SCHEME = "scheme"
    private const val K_TRIGGER = "trigger_at"
    private const val K_PRE = "pre_min"

    private fun sp(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun save(ctx: Context, event: String, scheme: String, triggerAt: Long, preMin: Int) {
        sp(ctx).edit().apply {
            putBoolean(K_SET, true)
            putString(K_EVENT, event)
            putString(K_SCHEME, scheme)
            putLong(K_TRIGGER, triggerAt)
            putInt(K_PRE, preMin)
        }.apply()
    }

    fun clear(ctx: Context) = sp(ctx).edit().putBoolean(K_SET, false).apply()

    fun isSet(ctx: Context) = sp(ctx).getBoolean(K_SET, false)
    fun eventName(ctx: Context) = sp(ctx).getString(K_EVENT, "") ?: ""
    fun scheme(ctx: Context) = sp(ctx).getString(K_SCHEME, "") ?: ""
    fun triggerAt(ctx: Context) = sp(ctx).getLong(K_TRIGGER, 0L)
    fun preMin(ctx: Context) = sp(ctx).getInt(K_PRE, 0)
}
