/*
 * Copyright 2015 The Android Open Source Project
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

package android.support.v17.leanback.supportleanbackshowcase.input.service;

import android.content.ComponentName;
import android.content.Context;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.media.tv.TvTrackInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v17.leanback.supportleanbackshowcase.input.player.LeanbackPlayer;
import android.support.v17.leanback.supportleanbackshowcase.input.player.RendererBuilderFactory;
import android.util.Log;

import com.google.android.media.tv.companionlibrary.BaseTvInputService;
import com.google.android.media.tv.companionlibrary.EpgSyncJobService;
import com.google.android.media.tv.companionlibrary.TvPlayer;
import com.google.android.media.tv.companionlibrary.model.Advertisement;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.google.android.media.tv.companionlibrary.model.RecordedProgram;
import com.google.android.media.tv.companionlibrary.utils.TvContractUtils;


/**
 * TvInputService which provides a full implementation of EPG, subtitles, multi-audio, parental
 * controls, and overlay view.
 */
public class TvInputService extends BaseTvInputService {
    private static final boolean DEBUG = true;
    private static final long EPG_SYNC_DELAYED_PERIOD_MS = 1000 * 2; // 2 Seconds


    /**
     * Gets the track id of the track type and track index.
     *
     * @param trackType  the type of the track e.g. TvTrackInfo.TYPE_AUDIO
     * @param trackIndex the index of that track within the media. e.g. 0, 1, 2...
     * @return the track id for the type & index combination.
     */
    private static String getTrackId(int trackType, int trackIndex) {
        return trackType + "-" + trackIndex;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public final Session onCreateSession(String inputId) {
        TvInputSessionImpl session = new TvInputSessionImpl(this, inputId);
        session.setOverlayViewEnabled(true);
        return super.sessionCreated(session);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public android.media.tv.TvInputService.RecordingSession onCreateRecordingSession(String inputId) {
        return new RecordingSession(this, inputId);
    }

    class TvInputSessionImpl extends BaseTvInputService.Session implements
            LeanbackPlayer.Listener{
        private static final String UNKNOWN_LANGUAGE = "und";

        private LeanbackPlayer mPlayer;
        private String mInputId;
        private Context mContext;

        TvInputSessionImpl(Context context, String inputId) {
            super(context, inputId);
            mContext = context;
            mInputId = inputId;
        }

        @Override
        public boolean onPlayProgram(Program program, long startPosMs) {
            if (program == null) {
                requestEpgSync(getCurrentChannelUri());
                notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
                return false;
            }
            createPlayer(program.getInternalProviderData().getVideoType(),
                    Uri.parse(program.getInternalProviderData().getVideoUrl()));
            if (startPosMs > 0) {
                mPlayer.seekTo(startPosMs);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
            }
            mPlayer.setPlayWhenReady(true);
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public boolean onPlayRecordedProgram(RecordedProgram recordedProgram) {
            createPlayer(recordedProgram.getInternalProviderData().getVideoType(),
                    Uri.parse(recordedProgram.getInternalProviderData().getVideoUrl()));

            long recordingStartTime = recordedProgram.getInternalProviderData()
                    .getRecordedProgramStartTime();
            mPlayer.seekTo(recordingStartTime - recordedProgram.getStartTimeUtcMillis());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
            }
            mPlayer.setPlayWhenReady(true);
            return true;
        }

        public TvPlayer getTvPlayer() {
            return mPlayer;
        }

        @Override
        public boolean onTune(Uri channelUri) {
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
            releasePlayer();
            return super.onTune(channelUri);
        }

        @Override
        public void onPlayAdvertisement(Advertisement advertisement) {
            createPlayer(TvContractUtils.SOURCE_TYPE_HTTP_PROGRESSIVE,
                    Uri.parse(advertisement.getRequestUrl()));
        }

        private void createPlayer(int videoType, Uri videoUrl) {
            releasePlayer();
            mPlayer = new LeanbackPlayer(RendererBuilderFactory.createRendererBuilder(
                    mContext, videoType, videoUrl), mContext);
            mPlayer.addListener(this);
            mPlayer.prepare();
        }

        @Override
        public void onSetCaptionEnabled(boolean enabled) {
        }

        @Override
        public boolean onSelectTrack(int type, String trackId) {
            return false;
        }

        private void releasePlayer() {
            if (mPlayer != null) {
                mPlayer.removeListener(this);
                mPlayer.setSurface(null);
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
        }

        @Override
        public void onRelease() {
            super.onRelease();
            releasePlayer();
        }

        @Override
        public void onBlockContent(TvContentRating rating) {
            super.onBlockContent(rating);
            releasePlayer();
        }


        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            if (mPlayer == null) {
                return;
            }

            if (playWhenReady ) {
                String audioId = getTrackId(TvTrackInfo.TYPE_AUDIO,
                        0);
                String videoId = getTrackId(TvTrackInfo.TYPE_VIDEO,
                        0);

                notifyTrackSelected(TvTrackInfo.TYPE_AUDIO, audioId);
                notifyTrackSelected(TvTrackInfo.TYPE_VIDEO, videoId);
                notifyVideoAvailable();
            }
        }


        public void requestEpgSync(final Uri channelUri) {
            EpgSyncJobService.requestImmediateSync(TvInputService.this, mInputId,
                    new ComponentName(TvInputService.this, LeanbackTvProgramSync.class));
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    onTune(channelUri);
                }
            }, EPG_SYNC_DELAYED_PERIOD_MS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private class RecordingSession extends BaseTvInputService.RecordingSession {
        private static final String TAG = "RecordingSession";
        private String mInputId;
        private long mStartTimeMs;

        public RecordingSession(Context context, String inputId) {
            super(context, inputId);
            mInputId = inputId;
        }

        @Override
        public void onTune(Uri uri) {
            super.onTune(uri);
            if (DEBUG) {
                Log.d(TAG, "onStartRecording");
            }
            mStartTimeMs = System.currentTimeMillis();
        }

        @Override
        public void onStopRecording(Program programToRecord) {
            if (DEBUG) {
                Log.d(TAG, "onStopRecording");
            }
            // In this sample app, since all of the content is VOD, the video URL is stored.
            // If the video was live, the start and stop times should be noted using
            // RecordedProgram.Builder.setStartTimeUtcMillis and .setEndTimeUtcMillis.
            // The recordingstart time will be saved in the InternalProviderData.
            // Additionally, the stream should be recorded and saved as
            // a new file.
            long currentTime = System.currentTimeMillis();
            InternalProviderData internalProviderData = programToRecord.getInternalProviderData();
            internalProviderData.setRecordingStartTime(mStartTimeMs);
            RecordedProgram recordedProgram = new RecordedProgram.Builder(programToRecord)
                        .setInputId(mInputId)
                        .setRecordingDataUri(
                                programToRecord.getInternalProviderData().getVideoUrl())
                        .setRecordingDurationMillis(currentTime - mStartTimeMs)
                        .setInternalProviderData(internalProviderData)
                        .build();
            notifyRecordingStopped(recordedProgram);
        }

        @Override
        public void onStopRecordingChannel(Channel channelToRecord) {
            if (DEBUG) {
                Log.d(TAG, "onStopRecording");
            }
            // Program sources in this sample always include program info, so execution here
            // indicates an error.
            notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
            return;
        }

        @Override
        public void onRelease() {
            if (DEBUG) {
                Log.d(TAG, "onRelease");
            }
        }
    }
}
