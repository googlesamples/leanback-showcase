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

package android.support.v17.leanback.supportleanbackshowcase.app.rows;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v17.leanback.supportleanbackshowcase.app.media.VideoConsumptionExampleFragment;
import android.support.v17.leanback.supportleanbackshowcase.app.media.VideoExampleActivity;
import android.text.TextUtils;

import java.util.List;

/**
 * VideoPlaybackActivity for video playback that loads PlaybackFragment
 *
 * This activity can be invoked in two scenarios,
 * 1. In DynamicRowFragment, when the video clips is clicked, this activity will be
 *    fired up to play the video
 * 2. When the channel is added to the home screen, click the video content will also start
 *    since this activity can receive the broadcast which is declared in AndroidManifest.xml
 *
 *    <data
 *        android:scheme="rowsnewapi"
 *        android:host="android.support.v17.leanback.supportleanbackshowcase"
 *        android:pathPrefix="/playvideo"
 *        />
 */
public class VideoPlaybackActivity extends Activity {

    private static final String PLAY_VIDEO_ACTION_PATH = "playvideo";

    /**
     * This is the helper function to extract the videoId (an unique id which will not be
     * changed along with the transaction)
     */
    private String getVideoID(Uri uri) {
        List<String> paths = uri.getPathSegments();
        if (paths.size() == 2 && TextUtils.equals(paths.get(0), PLAY_VIDEO_ACTION_PATH)) {
            return paths.get(1);

        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.playback_rows_control);

        VideoContent videoContent = null;

        /**
         * If this activity is started from the home screen (that is the only information
         * we have is videoId)
         * a videoContent will be constructed through it, and put as an extra to the intent
         * So VideoConsumptionExampleFragment can find the video source to play
         */
        Uri videoUri = getIntent().getData();
        if (videoUri != null) {
            String videoId = getVideoID(getIntent().getData());
            /**
             * Populate play lists firstly
             */
            ChannelContents.initializePlaylists(this);

            /**
             * Iterate through all video clips to find the video content which is selected
             * from home screen
             */
            for (int i = 0; i < ChannelContents.sChannelContents.size(); ++i) {
                List<VideoContent> videos = ChannelContents.sChannelContents.get(i).getVideos();
                for (VideoContent candidateVideo : videos) {
                    if (TextUtils.equals(candidateVideo.getVideoId(), videoId)) {
                        videoContent = candidateVideo;
                        break;
                    }
                }
            }

            getIntent().putExtra(VideoExampleActivity.TAG, videoContent);
        }

        VideoConsumptionExampleFragment videoPlaybackFragment = new VideoConsumptionExampleFragment();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(android.R.id.content, videoPlaybackFragment);
        fragmentTransaction.commit();
    }
}
