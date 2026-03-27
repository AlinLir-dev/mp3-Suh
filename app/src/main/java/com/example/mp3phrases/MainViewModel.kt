package com.example.mp3phrases

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.get(application).phraseAudioDao()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    val files: StateFlow<List<PhraseAudio>> = dao.observeAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun addUri(uri: Uri) {
        val app = getApplication<Application>()
        val name = app.contentResolver.resolveName(uri) ?: "Фраза"

        runCatching {
            app.contentResolver.takePersistableUriPermission(
                uri,
                IntentFlags.READ
            )
        }

        viewModelScope.launch {
            dao.insert(PhraseAudio(displayName = name, uri = uri.toString()))
            _status.value = "Добавлен файл: $name"
        }
    }

    fun playNow(item: PhraseAudio) {
        val app = getApplication<Application>()
        runCatching { AudioPlayer.play(app, item.uri) }
            .onSuccess { _status.value = "Воспроизведение: ${item.displayName}" }
            .onFailure { _status.value = "Не удалось воспроизвести: ${item.displayName}" }
    }

    fun startAuto() {
        val app = getApplication<Application>()
        if (files.value.isEmpty()) {
            _status.value = "Сначала добавьте хотя бы один MP3"
            return
        }
        AutoPlayScheduler.start(app)
        _status.value = "Авто-режим включен (случайно 5–15 минут)"
    }

    fun stopAuto() {
        AutoPlayScheduler.stop(getApplication())
        _status.value = "Авто-режим остановлен"
    }
}

private object IntentFlags {
    const val READ = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
}

private fun ContentResolver.resolveName(uri: Uri): String? {
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.getString(0)
        }
    }
    return null
}
