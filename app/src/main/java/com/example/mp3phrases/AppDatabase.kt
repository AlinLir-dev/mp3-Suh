package com.example.mp3phrases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PhraseAudio::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun phraseAudioDao(): PhraseAudioDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app.db"
                ).build().also { instance = it }
            }
        }
    }
}
