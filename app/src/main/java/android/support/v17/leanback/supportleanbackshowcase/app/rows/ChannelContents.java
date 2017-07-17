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
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.support.media.tv.Channel;
import android.support.media.tv.ChannelLogoUtils;
import android.support.media.tv.PreviewProgram;
import android.support.media.tv.TvContractCompat;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v17.leanback.supportleanbackshowcase.utils.Utils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
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

    /* package */void setChannelUnPublished() {
        mChannelPublished = false;
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

    /**
     * Async Task to remove channel from home screen
     */
    public static final class RemoveChannelInMainScreen extends AsyncTask<ChannelContents, Void, Void> {
        private static final String TAG = "RemoveChannelInMainScreen";

        /**
         * Executor must provide context and Listener so to execute LoadAddedChannels task
         */
        private Context mContext;
        private LoadAddedChannels.Listener mListener;

        public RemoveChannelInMainScreen(Context context, LoadAddedChannels.Listener listener) {
            mContext = context;
            mListener = listener;
        }

        private void deleteChannel(Context context, long channelId) {
            int rowsDeleted = context.getContentResolver().delete(
                    TvContractCompat.buildChannelUri(channelId), null, null);
            if (rowsDeleted < 1) {
                Log.e(TAG, "Delete channel failed");
            }
        }

        @Override
        protected Void doInBackground(ChannelContents... params) {
            ChannelContents playlist = params[0];
            deleteChannel(mContext, playlist.getChannelId());
            new LoadAddedChannels(mContext, mListener);
            return null;
        }
    }


    /**
     * Async Task to add channel to main screen/ remove channel from main screen
     */

    public static final class CreateChannelInMainScreen extends AsyncTask<ChannelContents, Void, Long> {
        private static final String TAG = "CreateChannelInMainScreen";

        private static final String SCHEME = "rowsnewapi";
        private static final String PACKAGE_NAME
                = "android.support.v17.leanback.supportleanbackshowcase";
        private static final String VIDEO_PLAY_ACTIVITY = "playvideo";
        private static final String MAIN_ACTIVITY = "startapp";
        public static final String CONTENT_ANDROID_MEDIA_TV_PREVIEW_PROGRAM
                = "content://android.media.tv/preview_program";

        // this object is used ot call startActivityForResult method from this Async Task
        private Activity mActivity;

        private static final int ADD_CHANNEL_REQUEST = 1;

        public CreateChannelInMainScreen(Activity context) {
            mActivity = context;
        }

        /**
         * Channel Logo for published program
         */
        private void createChannelLogo(Context context, long channelId,
                                       @DrawableRes int drawableId) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);
            ChannelLogoUtils.storeChannelLogo(context, channelId, bitmap);
        }

        /**
         * The input Id is necessary so user can use the program in home screen to go back to this
         * application
         */
        private String createInputId(Context context) {
            ComponentName cName = new ComponentName(context, DynamicVideoRowsActivity.class.getName());
            return TvContractCompat.buildInputId(cName);
        }

        /**
         * A transaction id will be assigned to the added channel
         */
        private long addChannel(Context context, ChannelContents channelContents) {
            // This ID is associated with current context
            String channelInputId = createInputId(context);
            Channel channel = new Channel.Builder()
                    .setDisplayName(channelContents.getName())
                    .setDescription(channelContents.getDescription())
                    .setType(TvContractCompat.Channels.TYPE_PREVIEW)
                    .setInputId(channelInputId)
                    .setAppLinkIntentUri(Uri.parse(SCHEME + "://" + PACKAGE_NAME
                            + "/" + MAIN_ACTIVITY))
                    .setInternalProviderId(channelContents.getChannelContentsId())
                    .build();

            /**
             * The interaction between current app and launcher interface is through ContentProvider
             * All the published data should be inserted into content provider
             */
            Uri channelUri = context.getContentResolver().insert(TvContractCompat.Channels.CONTENT_URI,
                    channel.toContentValues());
            if (channelUri == null || channelUri.equals(Uri.EMPTY)) {
                Log.e(TAG, "Insert channel failed");
                return 0;
            }
            long channelId = ContentUris.parseId(channelUri);
            channelContents.setChannelPublishedId(channelId);

            createChannelLogo(context, channelId, R.drawable.row_app_icon);

            List<VideoContent> videos = channelContents.getVideos();

            int weight = videos.size();
            for (int i = 0; i < videos.size(); ++i, --weight) {
                VideoContent clip = videos.get(i);
                final String clipId = clip.getVideoId();

                /**
                 * Enable preview feature for the video added to the home screen
                 */
                PreviewProgram program = new PreviewProgram.Builder()
                        .setChannelId(channelId)
                        .setTitle(clip.getTitle())
                        .setDescription(clip.getDescription())
                        .setPosterArtUri(Uri.parse(clip.getCardImageUrl()))
                        .setIntentUri(Uri.parse(SCHEME + "://" + PACKAGE_NAME
                                + "/" + VIDEO_PLAY_ACTIVITY + "/" + clipId))
                        .setPreviewVideoUri(Uri.parse(clip.getPreviewVideoUrl()))
                        .setInternalProviderId(clipId)
                        .setWeight(weight)
                        .setPosterArtAspectRatio(clip.getAspectRatio())
                        .setType(TvContractCompat.PreviewPrograms.TYPE_MOVIE)
                        .build();

                Uri preview_uri =
                        Uri.parse(CONTENT_ANDROID_MEDIA_TV_PREVIEW_PROGRAM);
                Uri programUri = context.getContentResolver().insert(preview_uri,
                        program.toContentValues());
                if (programUri == null || programUri.equals(Uri.EMPTY)) {
                    Log.e(TAG, "Insert program failed");
                } else {
                    clip.setProgramId(ContentUris.parseId(programUri));
                }
            }
            return channelId;
        }

        @Override
        protected Long doInBackground(ChannelContents... params) {
            return addChannel(mActivity, params[0]);
        }

        @Override
        protected void onPostExecute(Long channelId) {
            Intent intent = new Intent(TvContract.ACTION_REQUEST_CHANNEL_BROWSABLE);
            intent.putExtra(TvContractCompat.EXTRA_CHANNEL_ID, channelId);
            try {
                mActivity.startActivityForResult(intent, ADD_CHANNEL_REQUEST);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "could not start add channel approval UI", e);
            }
        }
    }
}


