package com.example.alarmapp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmAdapter(
    private var alarms: MutableList<Alarm>,
    private val onToggle: (Alarm, Boolean) -> Unit,
    private val onClick: (Alarm) -> Unit,
    private val onLongClick: (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    inner class AlarmViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvDateLabel: TextView = itemView.findViewById(R.id.tvDateLabel)
        val switchEnabled: SwitchCompat = itemView.findViewById(R.id.switchEnabled)
        val rowRoot: android.view.View = itemView.findViewById(R.id.rowRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.tvTime.text = timeFormat.format(alarm.toTriggerMillis())

        val dateStr = dateFormat.format(alarm.toTriggerMillis())
        holder.tvDateLabel.text = if (alarm.label.isNotBlank()) "$dateStr • ${alarm.label}" else dateStr

        // Listener remove kari ne set karvu jethi recycle thay tyare khota events na aave
        holder.switchEnabled.setOnCheckedChangeListener(null)
        holder.switchEnabled.isChecked = alarm.enabled
        holder.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            onToggle(alarm, isChecked)
        }

        holder.rowRoot.setOnClickListener { onClick(alarm) }
        holder.rowRoot.setOnLongClickListener {
            onLongClick(alarm)
            true
        }
    }

    override fun getItemCount(): Int = alarms.size

    fun updateList(newAlarms: List<Alarm>) {
        alarms = newAlarms.toMutableList()
        notifyDataSetChanged()
    }
}