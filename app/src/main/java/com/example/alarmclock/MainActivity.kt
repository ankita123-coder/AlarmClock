package com.example.alarmapp

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var rvAlarms: RecyclerView
    private lateinit var adapter: AlarmAdapter
    private lateinit var tvEmpty: android.widget.TextView

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Notification permission vagar alarm popup nahi dekhay", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvAlarms = findViewById(R.id.rvAlarms)
        tvEmpty = findViewById(R.id.tvEmpty)
        val fab: FloatingActionButton = findViewById(R.id.fabAddAlarm)

        rvAlarms.layoutManager = LinearLayoutManager(this)
        adapter = AlarmAdapter(
            alarms = AlarmStorage.getAlarms(this),
            onToggle = { alarm, isChecked -> onToggleAlarm(alarm, isChecked) },
            onClick = { alarm -> openEditAlarm(alarm) },
            onLongClick = { alarm -> confirmDelete(alarm) }
        )
        rvAlarms.adapter = adapter

        // FAB click -> Navu alarm add karva mate AddEditAlarmActivity khole
        fab.setOnClickListener {
            startActivity(Intent(this, AddEditAlarmActivity::class.java))
        }

        requestNeededPermissions()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val alarms = AlarmStorage.getAlarms(this)
        adapter.updateList(alarms)
        tvEmpty.visibility = if (alarms.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun onToggleAlarm(alarm: Alarm, isChecked: Boolean) {
        alarm.enabled = isChecked
        AlarmStorage.updateAlarm(this, alarm)
        if (isChecked) {
            AlarmScheduler.schedule(this, alarm)
            Toast.makeText(this, "Alarm ON kari didhu", Toast.LENGTH_SHORT).show()
        } else {
            AlarmScheduler.cancel(this, alarm)
            Toast.makeText(this, "Alarm OFF kari didhu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openEditAlarm(alarm: Alarm) {
        val intent = Intent(this, AddEditAlarmActivity::class.java)
        intent.putExtra("alarm_id", alarm.id)
        startActivity(intent)
    }

    private fun confirmDelete(alarm: Alarm) {
        AlertDialog.Builder(this)
            .setTitle("Alarm delete karvu che?")
            .setMessage("Aa alarm ne hamesha mate delete karso.")
            .setPositiveButton("Delete") { _, _ ->
                AlarmScheduler.cancel(this, alarm)
                AlarmStorage.deleteAlarm(this, alarm.id)
                refreshList()
                Toast.makeText(this, "Alarm delete thai gayu", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** Badhi jaruri permissions ek pachi ek check/request kariye chie */
    private fun requestNeededPermissions() {
        // 1) Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 2) Exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("Exact Alarm Permission")
                    .setMessage("Alarm barabar time upar vagva mate 'Alarms & reminders' permission allow karo.")
                    .setPositiveButton("Allow") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Later", null)
                    .show()
            }
        }

        // 3) Battery optimization ignore karva mate sujhav (background ma alarm miss na thay)
        val pm = getSystemService(POWER_SERVICE) as android.os.PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Kai devices upar aa action support nathi thatu, ignore kari devu
            }
        }
    }
}