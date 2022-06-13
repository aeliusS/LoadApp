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
        var fileName = "Unknown"
        var status = "Unknown"
        val extras = intent.extras
        if (extras != null) {
            fileName = extras.getString("File") ?: fileName
            status = extras.getString("Status") ?: status
        }
        Log.d("DetailActivity", "File name: $fileName. Status: $status")

        binding.contentDetail.fileNameText.text = fileName
        binding.contentDetail.statusText.text = status
        binding.contentDetail.statusText.setTextColor(
            getColor(if (status == "Fail") R.color.red else R.color.colorPrimaryDark)
        )

        binding.contentDetail.buttonOk.setOnClickListener {
            Log.d("DetailActivity", "Closing detail screen")
            finish()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 0
    }

}
