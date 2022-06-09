package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // dismiss the notification
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID)

        // get the file name and status
        val extras = intent.extras
        if (extras != null) {
            val fileName = extras.getString("File") ?: "Unknown file"
            val status = extras.getString("Status") ?: "Unknown status"

            Log.d("DetailActivity","File name: $fileName. Status: $status")
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 0
    }

}
