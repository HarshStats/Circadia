package com.example.circadia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.circadia.database.SleepDatabase
import com.example.circadia.database.SleepNight
import kotlinx.coroutines.*

class SleepTrackingService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val CHANNEL_ID = "SleepTrackerChannel"

    private var movementScore = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var startTimeMilli = 0L

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTimeMilli = System.currentTimeMillis()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Circadia Sleep Tracker")
            .setContentText("Monitoring your sleep quality...")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(1, notification)

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val deltaX = Math.abs(lastX - x)
            val deltaY = Math.abs(lastY - y)
            val deltaZ = Math.abs(lastZ - z)

            if (deltaX > 2 || deltaY > 2 || deltaZ > 2) {
                movementScore += (deltaX + deltaY + deltaZ)
            }

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        val endTimeMilli = System.currentTimeMillis()
        val score = movementScore
        val start = startTimeMilli
        
        sensorManager.unregisterListener(this)
        
        // Use GlobalScope or a scope that won't be cancelled immediately to ensure save happens
        // Or perform it synchronously if acceptable for a short duration, but IO is better.
        // We'll use serviceScope but launch with NonCancellable if needed, 
        // however serviceScope itself will be cancelled if we don't handle it.
        
        runBlocking {
            withContext(Dispatchers.IO) {
                saveSleepDataSync(start, endTimeMilli, score)
            }
        }

        serviceScope.cancel()
        super.onDestroy()
    }

    private fun saveSleepDataSync(start: Long, end: Long, score: Float) {
        val databaseDao = SleepDatabase.getInstance(applicationContext).sleepDatabaseDao

        val timeInBedHours = (end - start) / (1000 * 60 * 60).toFloat()
        val estimatedAwakeTime = score / 500f
        val efficiency = if (timeInBedHours > 0) {
            ((timeInBedHours - estimatedAwakeTime) / timeInBedHours * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        val night = SleepNight(
            startTimeMilli = start,
            endTimeMilli = end,
            movementScore = score,
            sleepEfficiencyPercentage = efficiency
        )

        databaseDao.insert(night)
        Log.d("SleepTracker", "Saved to DB! Score: $score, Efficiency: $efficiency%")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Tracking Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}