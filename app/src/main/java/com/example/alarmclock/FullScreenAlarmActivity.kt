package com.example.alarmclock

import android.app.KeyguardManager
import android.app.NotificationManager
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmclock.R
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Aa activity lockscreen upar j full screen batay che - alarm vagvani rite j.
 * Snooze ane Dismiss buttons apya che.
 */
class FullScreenAlarmActivity : AppCompatActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var alarmId: Int = 0
    private var label: String = ""
    private var hour: Int = 0
    private var minute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== Lockscreen upar batavva ane screen chalu karva mate =====
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        setContentView(R.layout.activity_fullscreen)

        alarmId = intent.getIntExtra("alarm_id", 0)
        label = intent.getStringExtra("alarm_label") ?: ""
        hour = intent.getIntExtra("alarm_hour", 0)
        minute = intent.getIntExtra("alarm_minute", 0)

        val tvLabel: TextView = findViewById(R.id.tvAlarmLabel)
        val tvTime: TextView = findViewById(R.id.tvAlarmTime)
        val btnSnooze: Button = findViewById(R.id.btnSnooze)
        val btnDismiss: Button = findViewById(R.id.btnDismiss)

        tvLabel.text = if (label.isNotBlank()) label else "Alarm"
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
        cal.set(java.util.Calendar.MINUTE, minute)
        tvTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)

        playAlarmSoundAndVibrate()

        btnDismiss.setOnClickListener { dismissAlarm() }
        btnSnooze.setOnClickListener { snoozeAlarm() }
    }

    private fun playAlarmSoundAndVibrate() {
        try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone?.isLooping = true
            ringtone?.play()
        } catch (e: Exception) {
            // Ringtone na male to ignore kari devu, vibration to thase j
        }

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 500, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopSoundAndVibration() {
        ringtone?.stop()
        vibrator?.cancel()
    }

    private fun dismissAlarm() {
        stopSoundAndVibration()
        clearNotification()
        // One-time alarm hoi, dismiss karva par enabled = false kari deva thi
        // list ma toggle pan off dekhashe.
        AlarmStorage.getAlarmById(this, alarmId)?.let { alarm ->
            alarm.enabled = false
            AlarmStorage.updateAlarm(this, alarm)
        }
        finish()
    }

    private fun snoozeAlarm() {
        stopSoundAndVibration()
        clearNotification()
        AlarmStorage.getAlarmById(this, alarmId)?.let { alarm ->
            AlarmScheduler.scheduleSnooze(this, alarm, 5)
        }
        finish()
    }

    private fun clearNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(alarmId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSoundAndVibration()
    }

    // Back button thi alarm band na thay - Dismiss/Snooze button j vaparva pade
    override fun onBackPressed() {
        // Intentionally kaink nathi karvu
    }
}