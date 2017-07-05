/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v17.leanback.supportleanbackshowcase.input.player;

import android.content.Context;
import android.media.PlaybackParams;
import android.os.Handler;
import android.view.Surface;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.media.tv.companionlibrary.TvPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LeanbackPlayer implements
        SimpleExoPlayer.VideoListener,
        ExoPlayer.EventListener,
        AudioRendererEventListener,
        TvPlayer {
    private static final int STATE_IDLE = ExoPlayer.STATE_IDLE;
    private static final int STATE_PREPARING = ExoPlayer.STATE_BUFFERING;
    private static final int RENDERER_BUILDING_STATE_IDLE = 1;
    private static final int RENDERER_BUILDING_STATE_BUILDING = 2;
    private static final int RENDERER_BUILDING_STATE_BUILT = 3;

    private final RendererBuilder rendererBuilder;
    private final SimpleExoPlayer player;
    private final Handler mainHandler;
    private final CopyOnWriteArrayList<Listener> listeners;
    private final List<Callback> mTvPlayerCallbacks;

    private int rendererBuildingState;
    private int lastReportedPlaybackState;
    private boolean lastReportedPlayWhenReady;

    private Renderer mAudioRenderer;

    /* package */ interface RendererBuilder {
        void buildRenderers(LeanbackPlayer player);

        void cancel();
    }

    public interface Listener {
        void onStateChanged(boolean playWhenReady, int playbackState);
    }

    public LeanbackPlayer(RendererBuilder rendererBuilder, Context context) {
        this.rendererBuilder = rendererBuilder;
        player = ExoPlayerFactory.newSimpleInstance
                (context, new DefaultTrackSelector(), new DefaultLoadControl());
        player.setVideoListener(this);
        player.addListener(this);
        mainHandler = new Handler();
        listeners = new CopyOnWriteArrayList<>();
        mTvPlayerCallbacks = new CopyOnWriteArrayList<>();
        lastReportedPlaybackState = STATE_IDLE;
        rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void prepare() {
        if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILT) {
            player.stop();
        }
        rendererBuilder.cancel();
        rendererBuildingState = RENDERER_BUILDING_STATE_BUILDING;
        maybeReportPlayerState();
        rendererBuilder.buildRenderers(this);
    }

    /* package */ void onRenderers(Renderer audioRenderer, MediaSource mMediaSource) {
        this.mAudioRenderer = audioRenderer;
        player.prepare(mMediaSource);
        rendererBuildingState = RENDERER_BUILDING_STATE_BUILT;
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        player.setPlayWhenReady(playWhenReady);
    }

    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }

    public void release() {
        rendererBuilder.cancel();
        rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
        player.release();
    }

    private int getPlaybackState() {
        if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILDING) {
            return STATE_PREPARING;
        }
        int playerState = player.getPlaybackState();
        if (rendererBuildingState == RENDERER_BUILDING_STATE_BUILT && playerState == STATE_IDLE) {
            return STATE_PREPARING;
        }
        return playerState;
    }

    /* package */ Handler getMainHandler() {
        return mainHandler;
    }

    /**
     * Implement TvPlayer interface
     */
    @Override
    public void setVolume(float volume) {
        ExoPlayer.ExoPlayerMessage m = new ExoPlayer.ExoPlayerMessage(mAudioRenderer, C.MSG_SET_VOLUME, volume);
        player.sendMessages(m);
    }

    @Override
    public void setSurface(Surface surface) {
        player.setVideoSurface(surface);
    }

    @Override
    public void registerCallback(Callback callback) {
        mTvPlayerCallbacks.add(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mTvPlayerCallbacks.remove(callback);
    }

    @Override
    public void setPlaybackParams(PlaybackParams params) {
    }

    @Override
    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return player.getDuration();
    }

    @Override
    public void pause() {
        player.setPlayWhenReady(false);
    }

    @Override
    public void play() {
        player.setPlayWhenReady(true);
    }

    public void stop() {
        player.stop();
    }

    /**
     * Implement Exoplayer.EventListener interface
     */
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // Do nothing.
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        // Do nothing.
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        // Do nothing.
    }

    @Override
    public void onPositionDiscontinuity() {
        // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int state) {
        for (Callback tvCallback : mTvPlayerCallbacks) {
            if (playWhenReady && state == ExoPlayer.STATE_ENDED) {
                tvCallback.onCompleted();
            } else if (playWhenReady && state == ExoPlayer.STATE_READY) {
                tvCallback.onStarted();
            }
        }
        maybeReportPlayerState();
    }

    private void maybeReportPlayerState() {
        boolean playWhenReady = player.getPlayWhenReady();
        int playbackState = getPlaybackState();
        if (lastReportedPlayWhenReady != playWhenReady ||
                lastReportedPlaybackState != playbackState) {
            for (Listener listener : listeners) {
                listener.onStateChanged(playWhenReady, playbackState);
            }
            lastReportedPlayWhenReady = playWhenReady;
            lastReportedPlaybackState = playbackState;
        }
    }

    /**
     * Implement AudioRendererEventListener
     */
    @Override
    public void onAudioEnabled(DecoderCounters counters) {
        // Do nothing.
    }

    @Override
    public void onAudioSessionId(int audioSessionId) {
        // Do nothing.

    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        // Do nothing.
    }

    @Override
    public void onAudioInputFormatChanged(Format format) {
        // Do nothing.
    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
        // Do nothing.
    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {
        // Do nothing.
    }


    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        // Do nothing.
    }

    @Override
    public void onRenderedFirstFrame() {
        // Do nothing.
    }
}
