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
import android.os.Bundle;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v17.leanback.supportleanbackshowcase.app.media.VideoConsumptionExampleFragment;

/**
 * VideoPlaybackActivity for video playback that loads PlaybackFragment
 */
public class VideoPlaybackActivity extends Activity {

    public static final String VIDEO_CONTENT = "VideoContent";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.playback_rows_control);

        if (savedInstanceState == null) {
            VideoContent mVideo = getIntent().getParcelableExtra(VIDEO_CONTENT);
            VideoConsumptionExampleFragment videoPlaybackFragment =
                    VideoConsumptionExampleFragment.newInstance(mVideo);

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content, videoPlaybackFragment);
            fragmentTransaction.commit();
        }
    }
}
