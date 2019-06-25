package org.theta.delivery.sample

import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.WindowManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_player.*
import org.theta.deliverysdk.ThetaDelivery
import org.theta.deliverysdk.datasource.ThetaDataSourceListener
import org.theta.deliverysdk.datasource.ThetaHlsDataSourceFactory
import org.theta.deliverysdk.models.*
import java.util.*

class PlayerActivity : AppCompatActivity() {

    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true
    private lateinit var adapter: LogsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var trafficToPeers = 0L
    private var trafficFromPeers = 0L
    private var trafficFromCDN = 0L

    private var playerListener: Player.EventListener? = null
    private val streamUrl = "[your_m3u8_url]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        ThetaDelivery.init(this)

        initializeLogsRecycler()
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        ThetaDelivery.destroy(this)
        super.onDestroy()
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(
                    DefaultRenderersFactory(this),
                    DefaultTrackSelector(),
                    DefaultLoadControl()
            )

            playerView.player = player
            player?.playWhenReady = playWhenReady
            player?.seekTo(0)
            player?.addListener(getPlayerListener())
        }

        val mediaSource = buildMediaSource(Uri.parse(streamUrl))
        player?.prepare(mediaSource, true, false)
    }

    private fun initializeLogsRecycler() {
        layoutManager = LinearLayoutManager(this)
        logsRecycler.layoutManager = layoutManager
        logsRecycler.setHasFixedSize(true)

        adapter = LogsAdapter(this, ArrayList())
        logsRecycler.adapter = adapter
    }

    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = player?.playWhenReady ?: true
            player?.release()
            player = null
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = ThetaHlsDataSourceFactory(
                this,
                Util.getUserAgent(this, "DeliverySDK"),
                BANDWIDTH_METER,
                ThetaConfig(
                        streamUrl,
                        "[unique_user_id]"),
                object : ThetaDataSourceListener {
                    override fun onInfoEvent(thetaInfoEvent: ThetaInfoEvent) {
                        Log.d("PlayerActivity", thetaInfoEvent.message)
                        runOnUiThread { addRecyclerMessage(thetaInfoEvent) }
                    }

                    override fun onTrafficEvent(trafficEvent: ThetaTrafficEvent) {
                        runOnUiThread {
                            addRecyclerMessage(trafficEvent)
                            displayTrafficValue(trafficEvent)
                        }

                    }

                    override fun onPeersChangedEvent(peersEvent: ThetaPeersChangedEvent) {
                        runOnUiThread {
                            addRecyclerMessage(peersEvent)
                            displayPeers(peersEvent)
                        }

                    }

                    override fun onAccountUpdatedEvent(userWalletEvent: ThetaUserWalletEvent) {

                    }

                    override fun onErrorEvent(errorEvent: ThetaErrorEvent) {
                        runOnUiThread { addRecyclerMessage(errorEvent) }
                    }
                }
        )

        return HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun addRecyclerMessage(event: ThetaDeliveryEvent) {
        adapter.addLog(ThetaEventWithDate(event, Calendar.getInstance().time))

        val scrollToLast = layoutManager.findLastCompletelyVisibleItemPosition() > adapter.itemCount - 10
        if (scrollToLast) {
            logsRecycler.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun displayPeers(peersEvent: ThetaPeersChangedEvent) {
        val textBuilder = StringBuilder()
        textBuilder.append(peersEvent.totalPeers)
        textBuilder.append(" ")
        textBuilder.append(resources.getQuantityString(R.plurals.peers, peersEvent.totalPeers))
        trafficPeersText.text = textBuilder.toString()
    }

    private fun displayTrafficValue(trafficEvent: ThetaTrafficEvent) {
        when (trafficEvent.name) {
            ThetaTrafficEvent.FROM_CDN -> {
                trafficFromCDN += trafficEvent.size
                trafficFromCDNText.text = formatTrafficValue(trafficFromCDN)
            }
            ThetaTrafficEvent.FROM_PEERS -> {
                trafficFromPeers += trafficEvent.size
                trafficFromPeersText.text = formatTrafficValue(trafficFromPeers)
            }
            ThetaTrafficEvent.TO_PEERS -> {
                trafficToPeers += trafficEvent.size
                trafficToPeersText.text = formatTrafficValue(trafficToPeers)
            }
        }
    }

    private fun formatTrafficValue(value: Long): String {
        val formattedValue = value.toDouble()
        val valueBuilder = StringBuilder()
        when {
            formattedValue > 1000000000 -> {
                valueBuilder.append(String.format("%.1f", formattedValue / 1000000000))
                valueBuilder.append(" GB")
            }
            formattedValue > 1000000 -> {
                valueBuilder.append(String.format("%.1f", formattedValue / 1000000))
                valueBuilder.append(" MB")
            }
            formattedValue > 1000 -> {
                valueBuilder.append(String.format("%.1f", formattedValue / 1000))
                valueBuilder.append(" KB")
            }
            else -> {
                valueBuilder.append(String.format("%.1f", formattedValue))
                valueBuilder.append(" Bytes")
            }
        }

        return valueBuilder.toString()
    }

    private fun getPlayerListener(): Player.EventListener {
        if (playerListener == null) {
            playerListener = object : Player.EventListener {
                override fun onTimelineChanged(timeline: Timeline, manifest: Any, reason: Int) {
                    if (player != null && player?.bufferedPosition ?: 0 <= -2000) {
                        player?.seekToDefaultPosition()
                        runOnUiThread { addRecyclerMessage(ThetaInfoEvent("onTimelineChanged, Going back to live")) }
                    }
                }

                override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {

                }

                override fun onLoadingChanged(isLoading: Boolean) {

                }

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                }

                override fun onRepeatModeChanged(repeatMode: Int) {

                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

                }

                override fun onPlayerError(error: ExoPlaybackException) {
                    Log.d("OnPlayerError", "message: " + error.message)
                }

                override fun onPositionDiscontinuity(reason: Int) {

                }

                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {

                }

                override fun onSeekProcessed() {

                }
            }
        }

        return playerListener!!
    }

    companion object {

        private val BANDWIDTH_METER = DefaultBandwidthMeter()
    }

}
