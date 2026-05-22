package com.example.circadia

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class WakeUpActivity : AppCompatActivity() {

    private var ringtone: Ringtone? = null
    private var y1 = 0f
    private val MIN_DISTANCE = 150

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_wake_up)

        val tvTime = findViewById<TextView>(R.id.tvWakeUpTime)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvTime.text = sdf.format(Date())

        // Play alarm sound
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(applicationContext, notification)
        ringtone?.play()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                y1 = event.y
            }
            MotionEvent.ACTION_UP -> {
                val y2 = event.y
                val deltaY = y2 - y1

                if (abs(deltaY) > MIN_DISTANCE) {
                    if (y2 < y1) {
                        // Swipe Up - Dismiss
                        dismissAlarm()
                    } else {
                        // Swipe Down - Snooze
                        snoozeAlarm()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun dismissAlarm() {
        ringtone?.stop()
        Toast.makeText(this, "Alarm Dismissed", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun snoozeAlarm() {
        ringtone?.stop()
        
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeTime = System.currentTimeMillis() + 5 * 60 * 1000 // 5 minutes
        
        val alarmClockInfo = AlarmManager.AlarmClockInfo(snoozeTime, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

        Toast.makeText(this, "Snoozed for 5 minutes", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
    }
}