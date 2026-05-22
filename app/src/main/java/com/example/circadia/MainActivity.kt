package com.example.circadia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Load the SleepFragment by default when the app first opens
        if (savedInstanceState == null) {
            replaceFragment(SleepFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_sleep -> {
                    replaceFragment(SleepFragment())
                    true
                }
                R.id.nav_alarm -> {
                    replaceFragment(AlarmFragment())
                    true
                }
                R.id.nav_diary -> {
                    replaceFragment(DiaryFragment())
                    true
                }
                R.id.nav_music -> {
                    replaceFragment(MusicFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Helper function to swap the fragments
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}