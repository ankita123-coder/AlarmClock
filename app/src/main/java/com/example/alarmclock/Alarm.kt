package com.example.alarmapp

import org.json.JSONObject
import java.util.Calendar

/**
 * Ek alarm nu data model.
 * id            -> unique identifier (AlarmManager request code tarike pan vaparay che)
 * hour, minute  -> 24-hour format
 * year/month/day-> alarm ni date
 * enabled       -> toggle switch on/off
 * label         -> user e lakhelu naam (optional)
 */
data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val year: Int,
    val month: Int,   // 0-11 (Calendar format)
    val day: Int,
    var enabled: Boolean = true,
    var label: String = ""
) {
    /** Alarm na time+date parthi triggerAtMillis banave che */
    fun toTriggerMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, day, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("hour", hour)
        obj.put("minute", minute)
        obj.put("year", year)
        obj.put("month", month)
        obj.put("day", day)
        obj.put("enabled", enabled)
        obj.put("label", label)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Alarm {
            return Alarm(
                id = obj.getInt("id"),
                hour = obj.getInt("hour"),
                minute = obj.getInt("minute"),
                year = obj.getInt("year"),
                month = obj.getInt("month"),
                day = obj.getInt("day"),
                enabled = obj.getBoolean("enabled"),
                label = obj.optString("label", "")
            )
        }
    }
}