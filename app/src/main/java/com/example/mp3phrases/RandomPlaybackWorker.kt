package com.example.mp3phrases

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters

class RandomPlaybackWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!AutoPlayPrefs.isEnabled(applicationContext)) {
            return Result.success()
        }

        setForeground(createForegroundInfo())

        val dao = AppDatabase.get(applicationContext).phraseAudioDao()
        val next = dao.getRandom()
        if (next != null) {
            runCatching {
                AudioPlayer.play(applicationContext, next.uri)
            }
            AutoPlayScheduler.scheduleNext(applicationContext)
        } else {
            AutoPlayScheduler.stop(applicationContext)
        }

        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createChannelIfNeeded()

        val notification: Notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("MP3 Phrases")
            .setContentText("Автовоспроизведение активно")
            .setOngoing(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "MP3 autoplay",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "mp3_autoplay"
        private const val NOTIFICATION_ID = 1001
    }
}
