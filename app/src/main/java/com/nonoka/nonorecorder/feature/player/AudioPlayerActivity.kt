package com.nonoka.nonorecorder.feature.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
import com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.IntentConstants.extraFilePath
import com.nonoka.nonorecorder.databinding.ActivityAudioPlayerBinding
import java.io.File


class AudioPlayerActivity : AppCompatActivity(), Player.Listener, View.OnClickListener {
    private lateinit var exoPlayer: ExoPlayer

    private lateinit var viewBinding: ActivityAudioPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAudioPlayerBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        val mediaUri: Uri = intent.getStringExtra(extraFilePath)?.let {
            val audioFile = File(it)
            viewBinding.trackTitle.text = audioFile.nameWithoutExtension
            Uri.fromFile(audioFile)
        } ?: return

        val renderersFactory = buildRenderersFactory(applicationContext, true)
        val trackSelector = DefaultTrackSelector(applicationContext)
        exoPlayer = ExoPlayer.Builder(applicationContext, renderersFactory)
            .setTrackSelector(trackSelector)
            .build().apply {
                trackSelectionParameters =
                    DefaultTrackSelector.Parameters.Builder(applicationContext).build()
                addListener(this@AudioPlayerActivity)
                playWhenReady = false
            }

        val mediaItem = MediaItem.fromUri(mediaUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        viewBinding.audioPlayer.player = exoPlayer
        viewBinding.audioPlayer.showController()
        viewBinding.buttonBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_back -> onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.release()
    }

    private fun buildRenderersFactory(
        context: Context,
        preferExtensionRenderer: Boolean
    ): RenderersFactory {
        val extensionRendererMode = if (preferExtensionRenderer) {
            EXTENSION_RENDERER_MODE_PREFER
        } else {
            EXTENSION_RENDERER_MODE_ON
        }

        return DefaultRenderersFactory(context.applicationContext)
            .setExtensionRendererMode(extensionRendererMode)
            .setEnableDecoderFallback(true)
    }

    companion object {
        @JvmStatic
        fun start(context: Context, filePath: String) {
            context.startActivity(Intent(context, AudioPlayerActivity::class.java).apply {
                putExtra(extraFilePath, filePath)
            })
        }
    }
}