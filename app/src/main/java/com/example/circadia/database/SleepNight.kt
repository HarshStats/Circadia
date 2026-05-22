package com.example.circadia.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_sleep_quality_table")
data class SleepNight(
    @PrimaryKey(autoGenerate = true)
    var nightId: Long = 0L,

    var startTimeMilli: Long = System.currentTimeMillis(),

    var endTimeMilli: Long = startTimeMilli,

    var movementScore: Float = 0f,

    var sleepEfficiencyPercentage: Int = 0
)