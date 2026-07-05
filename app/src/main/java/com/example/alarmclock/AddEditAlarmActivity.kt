package com.example.alarmclock

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmclock.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditAlarmActivity : AppCompatActivity() {

    private lateinit var btnSelectTime: Button
    private lateinit var btnSelectDate: Button
    private lateinit var etLabel: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var tvScreenTitle: TextView

    private var editingAlarm: Alarm? = null

    // Defaults to current time + 1 minute or editing alarm values
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_edit_alarm)

        btnSelectTime = findViewById(R.id.btnSelectTime)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        etLabel = findViewById(R.id.etLabel)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        tvScreenTitle = findViewById(R.id.tvScreenTitle)

        // Set default calendar values (current time)
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
        selectedMinute = calendar.get(Calendar.MINUTE)
        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)

        val alarmId = intent.getIntExtra("alarm_id", -1)
        if (alarmId != -1) {
            // Edit mode
            editingAlarm = AlarmStorage.getAlarmById(this, alarmId)
            editingAlarm?.let { alarm ->
                tvScreenTitle.text = "Alarm Edit karo"
                selectedHour = alarm.hour
                selectedMinute = alarm.minute
                selectedYear = alarm.year
                selectedMonth = alarm.month
                selectedDay = alarm.day
                etLabel.setText(alarm.label)
                btnDelete.visibility = android.view.View.VISIBLE
            }
        } else {
            tvScreenTitle.text = "Navu Alarm"
        }

        updateTimeButtonText()
        updateDateButtonText()

        btnSelectTime.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                updateTimeButtonText()
            }, selectedHour, selectedMinute, false).show()
        }

        btnSelectDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                selectedYear = year
                selectedMonth = month
                selectedDay = dayOfMonth
                updateDateButtonText()
            }, selectedYear, selectedMonth, selectedDay)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        btnSave.setOnClickListener { saveAlarm() }
        btnDelete.setOnClickListener { deleteAlarm() }
    }

    private fun updateTimeButtonText() {
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
        }
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        btnSelectTime.text = "Time: " + format.format(tempCal.time)
    }

    private fun updateDateButtonText() {
        val tempCal = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay)
        }
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        btnSelectDate.text = "Date: " + format.format(tempCal.time)
    }

    private fun saveAlarm() {
        val hour = selectedHour
        val minute = selectedMinute
        val year = selectedYear
        val month = selectedMonth
        val day = selectedDay
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