package com.example.mp3phrases

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object AutoPlayScheduler {
    const val UNIQUE_WORK_NAME = "random_mp3_playback"

    fun scheduleNext(context: Context) {
        val delayMin = Random.nextLong(from = 5, until = 16)
        val request = OneTimeWorkRequestBuilder<RandomPlaybackWorker>()
            .setInitialDelay(delayMin, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun start(context: Context) {
        AutoPlayPrefs.setEnabled(context, true)
        scheduleNext(context)
    }

    fun stop(context: Context) {
        AutoPlayPrefs.setEnabled(context, false)
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
