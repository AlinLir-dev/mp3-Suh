package com.example.mp3phrases

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

object AudioPlayer {
    private var activePlayer: MediaPlayer? = null

    @Synchronized
    fun play(context: Context, uriText: String) {
        stopCurrent()

        val uri = Uri.parse(uriText)
        val player = MediaPlayer()
        activePlayer = player

        player.setDataSource(context, uri)
        player.setOnCompletionListener {
            it.release()
            clearIfActive(it)
        }
        player.setOnErrorListener { mp, _, _ ->
            mp.release()
            clearIfActive(mp)
            true
        }
        player.prepare()
        player.start()
    }

    @Synchronized
    fun stopCurrent() {
        activePlayer?.let {
            runCatching {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            it.release()
        }
        activePlayer = null
    }

    @Synchronized
    private fun clearIfActive(player: MediaPlayer) {
        if (activePlayer == player) {
            activePlayer = null
        }
    }
}
