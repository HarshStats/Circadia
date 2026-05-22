package com.example.circadia

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class MusicFragment : Fragment() {

    private var mediaPlayer: MediaPlayer? = null

    // Your Playlist
    private val trackIds = intArrayOf(
        R.raw.forest_rain,       // Make sure this matches your file name exactly
        R.raw.sleep_music_17,
        R.raw.sleep_music_16
    )
    private val trackNames = arrayOf(
        "Forest Rain",
        "Relaxing Sleep Vol. 17",
        "Relaxing Sleep Vol. 16"
    )

    private var currentIndex = 0
    private var isPlaying = false

    private lateinit var tvTrackTitle: TextView
    private lateinit var tvTrackStatus: TextView
    private lateinit var btnPlayPause: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_music, container, false)

        tvTrackTitle = view.findViewById(R.id.tvTrackTitle)
        tvTrackStatus = view.findViewById(R.id.tvTrackStatus)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)

        val btnPrev = view.findViewById<TextView>(R.id.btnPrev)
        val btnNext = view.findViewById<TextView>(R.id.btnNext)

        // Initialize UI with the first track
        updateUI()

        // Button Listeners
        btnPlayPause.setOnClickListener { togglePlayPause() }
        btnNext.setOnClickListener { playNext() }
        btnPrev.setOnClickListener { playPrevious() }

        return view
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            pauseAudio()
        } else {
            playAudio()
        }
    }

    private fun playAudio() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), trackIds[currentIndex])
            mediaPlayer?.isLooping = true
        }
        mediaPlayer?.start()
        isPlaying = true
        updateUI()
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        updateUI()
    }

    private fun playNext() {
        stopAndReleaseAudio()
        currentIndex = (currentIndex + 1) % trackIds.size // Loop back to start if at the end
        playAudio()
    }

    private fun playPrevious() {
        stopAndReleaseAudio()
        currentIndex = if (currentIndex - 1 < 0) trackIds.size - 1 else currentIndex - 1 // Loop to end if at start
        playAudio()
    }

    private fun stopAndReleaseAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        isPlaying = false
    }

    private fun updateUI() {
        tvTrackTitle.text = trackNames[currentIndex]

        if (isPlaying) {
            tvTrackStatus.text = "Playing"
            btnPlayPause.text = "||" // Pause icon
        } else {
            tvTrackStatus.text = "Paused"
            btnPlayPause.text = "▶" // Play icon
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAndReleaseAudio() // Prevent music from playing when app is closed
    }
}