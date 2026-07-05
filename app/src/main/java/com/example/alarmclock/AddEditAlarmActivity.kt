package com.example.alarmclock

import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmclock.R
import java.util.Calendar

class AddEditAlarmActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var datePicker: DatePicker
    private lateinit var etLabel: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var tvScreenTitle: TextView

    private var editingAlarm: Alarm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_edit_alarm)

        timePicker = findViewById(R.id.timePicker)
        datePicker = findViewById(R.id.datePicker)
        etLabel = findViewById(R.id.etLabel)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        tvScreenTitle = findViewById(R.id.tvScreenTitle)

        timePicker.setIs24HourView(false)

        // Aaje ni date thi pehla ni date pasand na thay tema mate minimum date set
        datePicker.minDate = System.currentTimeMillis() - 1000

        val alarmId = intent.getIntExtra("alarm_id", -1)
        if (alarmId != -1) {
            // Edit mode
            editingAlarm = AlarmStorage.getAlarmById(this, alarmId)
            editingAlarm?.let { alarm ->
                tvScreenTitle.text = "Alarm Edit karo"
                timePicker.hour = alarm.hour
                timePicker.minute = alarm.minute
                datePicker.updateDate(alarm.year, alarm.month, alarm.day)
                etLabel.setText(alarm.label)
                btnDelete.visibility = android.view.View.VISIBLE
            }
        } else {
            tvScreenTitle.text = "Navu Alarm"
        }

        btnSave.setOnClickListener { saveAlarm() }
        btnDelete.setOnClickListener { deleteAlarm() }
    }

    private fun saveAlarm() {
        val hour = timePicker.hour
        val minute = timePicker.minute
        val year = datePicker.year
        val month = datePicker.month
        val day = datePicker.dayOfMonth
        val label = etLabel.text.toString().trim()

        val chosenMillis = Calendar.getInstance().apply {
            set(year, month, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (chosenMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "Bhavishya nu time/date pasand karo", Toast.LENGTH_SHORT).show()
            return
        }

        val alarm = if (editingAlarm != null) {
            // Jo edit thai rahyu hoy to juna alarm ni schedule pehla cancel kariye
            AlarmScheduler.cancel(this, editingAlarm!!)
            editingAlarm!!.copy(
                hour = hour, minute = minute,
                year = year, month = month, day = day,
                label = label, enabled = true
            )
        } else {
            Alarm(
                id = AlarmStorage.generateNextId(this),
                hour = hour, minute = minute,
                year = year, month = month, day = day,
                enabled = true, label = label
            )
        }

        AlarmStorage.updateAlarm(this, alarm)
        AlarmScheduler.schedule(this, alarm)

        Toast.makeText(this, "Alarm save thai gayu", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteAlarm() {
        editingAlarm?.let { alarm ->
            AlarmScheduler.cancel(this, alarm)
            AlarmStorage.deleteAlarm(this, alarm.id)
            Toast.makeText(this, "Alarm delete thai gayu", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}