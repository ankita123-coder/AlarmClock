package com.example.alarmapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * AlarmManager sathe schedule / cancel karva mate helper.
 * Exact alarm vaparyu chhe jethi alarm barabar time upar j vaage.
 */
object AlarmScheduler {

    private fun buildPendingIntent(context: Context, alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("alarm_label", alarm.label)
            putExtra("alarm_hour", alarm.hour)
            putExtra("alarm_minute", alarm.minute)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun canScheduleExact(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.canScheduleExactAlarms()
        } else true
    }

    fun schedule(context: Context, alarm: Alarm) {
        if (!alarm.enabled) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, alarm)
        val triggerAt = alarm.toTriggerMillis()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                // Exact alarm permission nathi -> normal (thodu inexact) alarm
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(context: Context, alarm: Alarm) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, alarm)
        am.cancel(pendingIntent)
    }

    /** Snooze mate: same alarm id thi thodi var pachi ferithi trigger */
    fun scheduleSnooze(context: Context, alarm: Alarm, minutesFromNow: Int = 5) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("alarm_label", alarm.label)
            putExtra("alarm_hour", alarm.hour)
            putExtra("alarm_minute", alarm.minute)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + minutesFromNow * 60_000L
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }
}