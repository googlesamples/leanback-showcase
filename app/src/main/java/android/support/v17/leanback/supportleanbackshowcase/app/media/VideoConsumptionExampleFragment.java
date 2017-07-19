/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.support.v17.leanback.supportleanbackshowcase.app.media;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v17.leanback.app.VideoFragment;
import android.support.v17.leanback.app.VideoFragmentGlueHost;
import android.support.v17.leanback.media.MediaPlayerAdapter;
import android.support.v17.leanback.media.PlaybackGlue;
import android.support.v17.leanback.supportleanbackshowcase.app.rows.VideoPlaybackActivity;
import android.support.v17.leanback.supportleanbackshowcase.app.rows.VideoContent;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.util.Log;

/**
 * Video playback fragment
 *
 * Use static method newInstance to create the fragment with specified video resource
 */
public class VideoConsumptionExampleFragment extends VideoFragment {

    /**
     * Default video to play when this fragment cannot load valid video resource
     */
    private static final String URL = "https://storage.googleapis.com/android-tv/Sample videos/"
            + "April Fool's 2013/Explore Treasure Mode with Google Maps.mp4";
    public static final String TAG = "VideoConsumption";
    private VideoMediaPlayerGlue<MediaPlayerAdapter> mMediaPlayerGlue;
    final VideoFragmentGlueHost mHost = new VideoFragmentGlueHost(this);

    static void playWhenReady(PlaybackGlue glue) {
        if (glue.isPrepared()) {
            glue.play();
        } else {
            glue.addPlayerCallback(new PlaybackGlue.PlayerCallback() {
                @Override
                public void onPreparedStateChanged(PlaybackGlue glue) {
                    if (glue.isPrepared()) {
                        glue.removePlayerCallback(this);
                        glue.play();
                    }
                }
            });
        }
    }

    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener
            = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int state) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMediaPlayerGlue = new VideoMediaPlayerGlue(getActivity(),
                new MediaPlayerAdapter(getActivity()));
        mMediaPlayerGlue.setHost(mHost);
        AudioManager audioManager = (AudioManager) getActivity()
                .getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.w(TAG, "video player cannot obtain audio focus!");
        }

        mMediaPlayerGlue.setMode(PlaybackControlsRow.RepeatAction.INDEX_NONE);

//        Parcelable intentData = getArguments().getParcelable(VideoConsumptionExampleFragment.TAG);

        Parcelable intentData = getActivity().getIntent().getParcelableExtra(VideoExampleActivity.TAG);
        MediaMetaData intentMetaData;
        /**
         * If there is no intent data just play the default video
         */
        if (intentData == null) {

            /**
             * When there is no intent data, just playing the default video clip
             */
            mMediaPlayerGlue.setTitle("Diving with Sharks");
            mMediaPlayerGlue.setSubtitle("A Googler");
            mMediaPlayerGlue.getPlayerAdapter().setDataSource(Uri.parse(URL));
        } else{
            /**
             * If the intent data is an instance of VideoContent
             * key fields will be extracted to create a new object from MediaMetaData class
             */
            if (intentData instanceof VideoContent) {
                VideoContent intentVideoData = (VideoContent) intentData;
                intentMetaData = new MediaMetaData();
                intentMetaData.setMediaSourcePath(intentVideoData.getVideoUrl());
                intentMetaData.setMediaArtistName(intentVideoData.getDescription());
                intentMetaData.setMediaTitle(intentVideoData.getTitle());
            } else {
                /**
                 * If the intent data is an instance of MediaMetaData,
                 * it will be converted to MediaMetaData directly
                 */
                intentMetaData = (MediaMetaData)intentData;
            }

            /**
             * Set media meta information through media player glue
             */
            mMediaPlayerGlue.setTitle(intentMetaData.getMediaTitle());
            mMediaPlayerGlue.setSubtitle(intentMetaData.getMediaArtistName());
            mMediaPlayerGlue.getPlayerAdapter().setDataSource(
                    Uri.parse(intentMetaData.getMediaSourcePath()));

        }

        PlaybackSeekDiskDataProvider.setDemoSeekProvider(mMediaPlayerGlue);
        playWhenReady(mMediaPlayerGlue);
        setBackgroundType(BG_LIGHT);
    }

    @Override
    public void onPause() {
        if (mMediaPlayerGlue != null) {
            mMediaPlayerGlue.pause();
        }
        super.onPause();
    }
}
