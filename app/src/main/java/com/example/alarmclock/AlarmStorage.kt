package com.example.alarmclock

import android.content.Context
import org.json.JSONArray

/**
 * Alarms ne SharedPreferences ma JSON tarike save kariye chie.
 * Room database vagar pan simple ane reliable rahe tevu.
 */
object AlarmStorage {

    private const val PREF_NAME = "alarm_prefs"
    private const val KEY_ALARMS = "alarms_json"
    private const val KEY_NEXT_ID = "next_id"

    fun getAlarms(context: Context): MutableList<Alarm> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ALARMS, "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<Alarm>()
        for (i in 0 until arr.length()) {
            list.add(Alarm.fromJson(arr.getJSONObject(i)))
        }
        // Sabse pehla alarm upar dekhay tevi rite time pramane sort
        list.sortWith(compareBy { it.toTriggerMillis() })
        return list
    }

    private fun saveAlarms(context: Context, alarms: List<Alarm>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        alarms.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_ALARMS, arr.toString()).apply()
    }

    fun generateNextId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val next = prefs.getInt(KEY_NEXT_ID, 1)
        prefs.edit().putInt(KEY_NEXT_ID, next + 1).apply()
        return next
    }

    fun addAlarm(context: Context, alarm: Alarm) {
        val alarms = getAlarms(context)
        alarms.add(alarm)
        saveAlarms(context, alarms)
    }

    fun updateAlarm(context: Context, updated: Alarm) {
        val alarms = getAlarms(context)
        val idx = alarms.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            alarms[idx] = updated
        } else {
            alarms.add(updated)
        }
        saveAlarms(context, alarms)
    }

    fun deleteAlarm(context: Context, id: Int) {
        val alarms = getAlarms(context)
        alarms.removeAll { it.id == id }
        saveAlarms(context, alarms)
    }

    fun getAlarmById(context: Context, id: Int): Alarm? {
        return getAlarms(context).find { it.id == id }
    }
}