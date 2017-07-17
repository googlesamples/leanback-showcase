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
import android.database.Cursor;
import android.media.tv.TvContract;
import android.os.AsyncTask;
import android.support.media.tv.TvContractCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Async task to load published channel
 * So the publish status will be loaded every time when the setting activity is created
 *
 * Usage: Just create the object (new LoadAddedChannels()), then the async task will be executed
 */
public class LoadAddedChannels extends AsyncTask<Void, Void, Void> {

    /**
     * ContentProvider projection scheme and related column ID
     */
    private static final String[] CHANNELS_MAP_PROJECTION =
            {TvContractCompat.Channels._ID, TvContractCompat.Channels.COLUMN_INTERNAL_PROVIDER_ID};
    private static final int CHANNELS_COLUMN_ID_INDEX = 0;
    private static final int CHANNELS_COLUMN_INTERNAL_PROVIDER_ID_INDEX = 1;

    /**
     * All published channel using ChannelPlaylistId to represent
     */
    private List<ChannelPlaylistId> mPublishedChannelContents = new ArrayList<>();

    /**
     * To execute this async task properly (dealing with content resolver) executor must provide
     * the context
     */
    Context mContext;

    /**
     * Registered Listener for this aysnc task
     */
    private Listener mListener;

    interface Listener {
        void onPublishedChannelsLoaded(List<ChannelPlaylistId> publishedChannels);
    }

    LoadAddedChannels(Context context, Listener listener) {
        mListener = listener;
        mContext = context;
        this.execute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        loadChannels();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        onPublishedChannelsLoaded(mPublishedChannelContents);
    }


    private void loadChannels() {

        /**
         * every time when the async task loadChannels is executed, the published channel list
         * will be updated accordingly
         */
        mPublishedChannelContents.clear();
        try (Cursor cursor = mContext.getContentResolver().query(TvContract.Channels.CONTENT_URI,
                CHANNELS_MAP_PROJECTION, null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (!cursor.isNull(
                            CHANNELS_COLUMN_INTERNAL_PROVIDER_ID_INDEX)) {
                        // Found a row that contains a non-null provider id.
                        String id = cursor.getString(CHANNELS_COLUMN_INTERNAL_PROVIDER_ID_INDEX);
                        long channelId = cursor.getLong(CHANNELS_COLUMN_ID_INDEX);
                        mPublishedChannelContents.add(new ChannelPlaylistId(id, channelId));
                    }
                }
            }
        }
    }

    /**
     * At the end of the async task, the callback from registered listener will be executed
     * to keep track of channels publish status
     */
    public void onPublishedChannelsLoaded(List<ChannelPlaylistId> publishedChannels) {
        mListener.onPublishedChannelsLoaded(publishedChannels);
    }
}
