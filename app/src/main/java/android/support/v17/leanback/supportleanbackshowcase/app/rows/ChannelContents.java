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

import android.content.Context;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v17.leanback.supportleanbackshowcase.utils.Utils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is mainly used for the channel which has been added to the main-screen
 */
public final class ChannelContents {

    /**
     * All video resource inside this channel
     */
    @SerializedName("videos")
    private List<VideoContent> mVideos;

    /**
     * A unique ID for different channels
     * This is channel is mainly for internal use, and won't be changed
     */
    @SerializedName("channelId")
    private String mChannelContentsId;

    /**
     * The channel name shown in the main screen after adding it
     */
    @SerializedName("category")
    private String mName;

    /**
     * The channel's description shown in the main screen after adding it
     */
    private final String mDescription = "Demo To Show how to add channel from App to main screen";

    /**
     * Is current channel published or not
     * This is the symbol to toggle add/ remove behavior
     */
    private boolean mChannelPublished;

    /**
     * This ID is to identify the channel which has been added to main screen
     */
    private long mChannelId;

    /* package */ String getName() {
        return mName;
    }

    /* package */ List<VideoContent> getVideos() {
        return mVideos;
    }

    /* package */ String getDescription() {
        return mDescription;
    }

    /* package */String getChannelContentsId() {
        return mChannelContentsId;
    }

    /* package */boolean isChannelPublished() {
        return mChannelPublished;
    }

    /* package */void setChannelPublishedId(long id) {
        mChannelPublished = true;
        mChannelId = id;
    }

    /* package */long getChannelId() {
        return mChannelId;
    }


    /* package */ static List<ChannelContents> sChannelContents;

    /* package */ static void createPlaylists(Context context) {
        if (sChannelContents == null) {

            sChannelContents = new ArrayList<>();
            String json = Utils.inputStreamToString(
                    context.getResources().openRawResource(R.raw.movie));

            /**
             * Populate playlist from json file
             */
            ChannelContents[] channels = new Gson().fromJson(json, ChannelContents[].class);
            for (int i = 0; i < channels.length; i++) {
                sChannelContents.add(channels[i]);
            }
        }
    }
}
