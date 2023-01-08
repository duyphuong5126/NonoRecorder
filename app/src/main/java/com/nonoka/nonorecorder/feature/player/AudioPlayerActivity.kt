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
import com.google.android.exoplayer2.Player.Listener
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.IntentConstants.extraFilePaths
import com.nonoka.nonorecorder.constant.IntentConstants.extraStartPosition
import com.nonoka.nonorecorder.databinding.ActivityAudioPlayerBinding
import java.io.File
import timber.log.Timber


class AudioPlayerActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var exoPlayer: ExoPlayer

    private lateinit var viewBinding: ActivityAudioPlayerBinding

    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAudioPlayerBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        val fileList = intent.getStringArrayExtra(extraFilePaths)?.map {
            File(it)
        } ?: return
        val mediaItemList: List<MediaItem> = fileList.map {
            MediaItem.fromUri(Uri.fromFile(it))
        }
        val startPosition = intent.getIntExtra(extraStartPosition, -1)
        if (mediaItemList.isEmpty() || startPosition < 0) {
            return
        }

        val renderersFactory = buildRenderersFactory(applicationContext, true)
        val trackSelector = DefaultTrackSelector(applicationContext)
        exoPlayer = ExoPlayer.Builder(applicationContext, renderersFactory)
            .setTrackSelector(trackSelector)
            .build().apply {
                trackSelectionParameters =
                    DefaultTrackSelector.Parameters.Builder(applicationContext).build()
                playWhenReady = false
            }

        exoPlayer.setMediaItems(mediaItemList, startPosition, 0)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        viewBinding.audioPlayer.player = exoPlayer
        viewBinding.audioPlayer.showController()
        viewBinding.buttonBack.setOnClickListener(this)
        listener = object : Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                val currentIndex = player.currentMediaItemIndex
                if (currentIndex >= 0) {
                    var title = mediaItemList[currentIndex].mediaMetadata.title
                    if (title == null) {
                        title = fileList[currentIndex].nameWithoutExtension
                    }
                    Timber.d("currentIndex=$currentIndex, title=$title")
                    viewBinding.trackTitle.text = title
                }
            }
        }
        listener?.let(exoPlayer::addListener)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_back -> onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.let(exoPlayer::removeListener)
        listener = null
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
        fun start(context: Context, filePathList: List<String>, startPosition: Int) {
            context.startActivity(Intent(context, AudioPlayerActivity::class.java).apply {
                putExtra(extraFilePaths, filePathList.toTypedArray())
                putExtra(extraStartPosition, startPosition)
            })
        }
    }
}