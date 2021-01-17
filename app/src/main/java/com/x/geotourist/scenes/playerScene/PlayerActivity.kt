package com.x.geotourist.scenes.playerScene

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.x.geotourist.R
import com.x.geotourist.scenes.mapScene.MapActivity
import com.x.geotourist.utils.Utils
import dagger.android.AndroidInjection
import java.io.File
import javax.inject.Inject

class   PlayerActivity : AppCompatActivity() {
    @Inject
    lateinit var simpleExoPlayer: SimpleExoPlayer

    @Inject
    lateinit var simpleCache: SimpleCache

    @Inject
    lateinit var cacheDataSourceFactory: CacheDataSourceFactory

    @Inject
    lateinit var dataSourceFactory: DataSource.Factory

    lateinit var playerView: PlayerView
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_player)
        playerView = findViewById(R.id.playerView)
        playVideo(intent.getStringExtra("url"))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onPause() {
        simpleExoPlayer.playWhenReady = false
        super.onPause()


    }

    override fun onResume() {
        super.onResume()
        simpleExoPlayer.playWhenReady = true
    }

    private fun playVideo(url: String) {
        val concatenatedSource = ConcatenatingMediaSource(
            ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(Uri.parse(url))
        )
        simpleExoPlayer.prepare(concatenatedSource)

        playerView.player = simpleExoPlayer

    }


}