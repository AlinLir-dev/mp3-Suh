package com.example.mp3phrases

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrase_audio")
data class PhraseAudio(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val uri: String
)
