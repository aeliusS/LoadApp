package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.udacity.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private lateinit var binding: ActivityMainBinding

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // create the notification channel
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )

        prefs = application.getSharedPreferences("com.udacity", Context.MODE_PRIVATE)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.contentMain.customButton.setOnClickListener {
            if (!validRadioOption()) return@setOnClickListener
            binding.contentMain.customButton.setButtonLoading()

            // place the current file to be downloaded into shared preference to be retrieved later
            val selectedIndex = binding.contentMain.radioGroup.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(selectedIndex)
            lifecycleScope.launch {
                saveRadioOption(selectedIndex, radioButton.text.toString())
            }

            download(selectedIndex)
        }

    }

    private fun validRadioOption(): Boolean {
        if (binding.contentMain.radioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(applicationContext, R.string.select_file_message, Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private suspend fun saveRadioOption(radioId: Int, fileName: String) =
        withContext(Dispatchers.IO) {
            with(prefs.edit()) {
                putInt(SELECTED_RADIO_ID, radioId)
                putString(SELECTED_FILE_NAME, fileName)
                apply()
            }
        }

    private suspend fun getRadioOption(): RadioOption =
        withContext(Dispatchers.IO) {
            RadioOption(
                prefs.getInt(SELECTED_RADIO_ID, 0),
                prefs.getString(SELECTED_FILE_NAME, null) ?: "Unknown file"
            )

        }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                    .apply {
                        setShowBadge(true)
                    }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download finished"

            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) {
                Log.d("MainActivity", "Download finished")
                val statusString = getDownloadStatus(downloadID)
                Log.d("MainActivity", "Status: $statusString")

                lifecycleScope.launch {
                    val (radioId, fileName) = getRadioOption()
                    notificationManager.sendNotification(
                        notifyMessageBody(radioId),
                        fileName,
                        statusString
                    )
                }
                // binding.contentMain.customButton.setButtonCompletedDownload()
            }
        }
    }

    private fun download(selectedIndex: Int) {
        val request =
            DownloadManager.Request(Uri.parse(downloadURL(selectedIndex)))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun downloadURL(selectedIndex: Int): String {
        return when (selectedIndex) {
            R.id.glide_radio_button -> getString(R.string.glide_url)
            R.id.load_app_radio_button -> getString(R.string.load_app_url)
            R.id.retrofit_radio_button -> getString(R.string.retrofit_url)
            else -> ""
        }
    }

    private fun notifyMessageBody(radioId: Int): String {
        return when (radioId) {
            R.id.glide_radio_button -> getString(R.string.glide_option_prettify)
            R.id.load_app_radio_button -> getString(R.string.load_app_option_prettify)
            R.id.retrofit_radio_button -> getString(R.string.retrofit_option_prettify)
            else -> getString(R.string.custom_option_prettify)
        }
    }

    /**
     * Returns the download status as a String
     * Returns either Failed or Success
     * */
    private fun getDownloadStatus(id: Long): String {
        val downloadedFile = DownloadManager.Query()
            .setFilterById(id)
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val cursor = downloadManager.query(downloadedFile)

        // return if the query returned with no rows
        if (cursor.isAfterLast) return "Fail"
        cursor.moveToFirst()

        val column = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        var status = DownloadManager.STATUS_FAILED
        if (column > -1) status = cursor.getInt(column)

        return when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> "Success"
            else -> "Fail"
        }
    }

    private fun NotificationManager.sendNotification(
        messageBody: String,
        fileName: String,
        status: String
    ) {
        val downloadStatusIntent = Intent(applicationContext, DetailActivity::class.java)
        downloadStatusIntent.putExtra("File", fileName)
        downloadStatusIntent.putExtra("Status", status)

        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            downloadStatusIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        // the custom button
        action = NotificationCompat.Action(
            R.drawable.ic_assistant_black_24dp,
            getString(R.string.notification_button),
            pendingIntent
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            getString(R.string.notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(messageBody)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(action)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notify(NOTIFICATION_ID, builder.build())
    }

    data class RadioOption(var radioId: Int, var fileName: String)

    companion object {
        private const val NOTIFICATION_ID = 0
        private const val SELECTED_RADIO_ID = "SELECTED_RADIO_ID"
        private const val SELECTED_FILE_NAME = "SELECTED_FILE_NAME"
    }

}
