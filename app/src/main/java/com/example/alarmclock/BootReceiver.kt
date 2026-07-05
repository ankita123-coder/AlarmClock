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
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_TIME_CHANGED || 
            action == "android.intent.action.TIME_SET" || 
            action == Intent.ACTION_TIMEZONE_CHANGED) {
            
            val alarms = AlarmStorage.getAlarms(context)
            alarms.filter { it.enabled }.forEach { alarm ->
                // Jo alarm nu time pehla j jatu rahyu hoy to skip karo
                if (alarm.toTriggerMillis() > System.currentTimeMillis()) {
                    AlarmScheduler.schedule(context, alarm)
                } else {
                    // Update state to disabled since it has passed (for one-time date specific alarms)
                    alarm.enabled = false
                    AlarmStorage.updateAlarm(context, alarm)
                }
            }
        }
    }
}