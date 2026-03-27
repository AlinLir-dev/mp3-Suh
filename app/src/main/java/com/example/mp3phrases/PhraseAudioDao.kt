package com.example.mp3phrases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseAudioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PhraseAudio)

    @Query("SELECT * FROM phrase_audio ORDER BY id DESC")
    fun observeAll(): Flow<List<PhraseAudio>>

    @Query("SELECT * FROM phrase_audio ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandom(): PhraseAudio?
}
