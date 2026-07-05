package com.example.alarmclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Phone restart thay pachi AlarmManager na badha pending alarms clear
 * thai jay che, etle boot pachi ferithi badha ENABLED alarms schedule
 * karva mate aa receiver.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarms = AlarmStorage.getAlarms(context)
            alarms.filter { it.enabled }.forEach { alarm ->
                // Jo alarm nu time pehla j jatu rahyu hoy to skip karo
                if (alarm.toTriggerMillis() > System.currentTimeMillis()) {
                    AlarmScheduler.schedule(context, alarm)
                }
            }
        }
    }
}