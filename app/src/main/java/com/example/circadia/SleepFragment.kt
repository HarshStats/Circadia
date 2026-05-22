package com.example.circadia

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class SleepFragment : Fragment() {

    private var isTracking = false
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvCurrentDate: TextView
    private lateinit var tvNextAlarmTime: TextView
    private lateinit var tvStartText: TextView
    private lateinit var ivStatusIcon: ImageView
    
    private val handler = Handler(Looper.getMainLooper())
    private val timeUpdater = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, 60000) // Update every minute
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)

        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate)
        tvNextAlarmTime = view.findViewById(R.id.tvNextAlarmTime)
        tvStartText = view.findViewById(R.id.tvStartText)
        ivStatusIcon = view.findViewById(R.id.ivStatusIcon)
        val btnToggleSleep = view.findViewById<View>(R.id.btnCircularStart)

        handler.post(timeUpdater)

        btnToggleSleep.setOnClickListener {
            isTracking = !isTracking
            val serviceIntent = Intent(requireContext(), SleepTrackingService::class.java)

            if (isTracking) {
                // Change UI to "Sleeping" state
                tvStartText.text = "STOP TRACKING"
                tvStartText.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red))
                ivStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.error_red))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireContext().startForegroundService(serviceIntent)
                } else {
                    requireContext().startService(serviceIntent)
                }
            } else {
                // Change UI back to "Awake" state
                tvStartText.text = "GO TO SLEEP"
                tvStartText.setTextColor(ContextCompat.getColor(requireContext(), R.color.neon_blue))
                ivStatusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.neon_blue))

                requireContext().stopService(serviceIntent)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        updateClock()
        updateNextAlarm()
    }

    private fun updateClock() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        val now = Date()
        
        tvCurrentTime.text = timeFormat.format(now)
        tvCurrentDate.text = dateFormat.format(now)
    }

    private fun updateNextAlarm() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm = alarmManager.nextAlarmClock
        
        if (nextAlarm != null) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvNextAlarmTime.text = sdf.format(Date(nextAlarm.triggerTime))
        } else {
            tvNextAlarmTime.text = "None set"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timeUpdater)
    }
}