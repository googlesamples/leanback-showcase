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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.List;

/**
 * All the interaction with the video in main screen will go through this activity
 */
public class ScreenChannelRowActivity extends Activity {

    private static final String PLAY_VIDEO_ACTION_PATH = "playvideo";

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

        VideoContent videoContent = null;
        String videoId = getVideoID(getIntent().getData());

        /**
         * Populate playlists firstly
         */
        ChannelContents.createPlaylists(this);
        for (int i = 0; i < ChannelContents.sChannelContents.size(); ++i) {
            List<VideoContent> videos = ChannelContents.sChannelContents.get(i).getVideos();
            for (VideoContent candidateVideo : videos) {
                if (TextUtils.equals(candidateVideo.getVideoId(), videoId)) {
                    videoContent = candidateVideo;
                    break;
                }
            }
        }

        if (null != videoContent) {
            Intent playVideo = new Intent(this, VideoPlaybackActivity.class);
            playVideo.putExtra(VideoPlaybackActivity.VIDEO_CONTENT, videoContent);
            startActivity(playVideo);
        }
        finish();
    }
}
