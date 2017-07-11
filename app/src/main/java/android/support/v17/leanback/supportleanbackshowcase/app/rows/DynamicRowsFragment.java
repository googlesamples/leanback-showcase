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
import android.graphics.drawable.Drawable;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.media.tv.Channel;
import android.support.media.tv.ChannelLogoUtils;
import android.support.media.tv.PreviewProgram;
import android.support.media.tv.TvContractCompat;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The main fragment which is responsible for all interaction
 *
 * like adding/ removing rows from app to launcher, play video within the app
 */
public class DynamicRowsFragment extends BrowseFragment {

    private static final String TAG = "DynamicRowsFragment";
    private static final boolean DEBUG = true;
    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int ADD_CHANNEL_REQUEST = 1;

    private final Handler mHandler = new Handler();
    private DisplayMetrics mMetrics;
    private URI mBackgroundURI;
    private BackgroundManager mBackgroundManager;
    private Runnable mBackgroudUpdateRunnable;
    /**
     * All channel contents related to this app.
     */
    private List<ChannelContents> mChannelContents;

    /**
     * The collection of published channel contents
     */
    private List<ChannelPlaylistId> mPublishedChannelContents = new ArrayList<>();

    /**
     * Fragment UI setup
     *
     * The following function/ class is closely related to the UI which is related to the fragment
     * itself
     */

    private static final class CardPresenterSelector extends PresenterSelector {
        private CardPresenter mCardPresenter = new CardPresenter();
        private final AddRemoveButtonPresenter mAddRemoveButtonPresenter;

        CardPresenterSelector(Context context) {
            mAddRemoveButtonPresenter = new AddRemoveButtonPresenter(context);
        }

        public Presenter getPresenter(Object item) {
            if (item instanceof VideoContent) {
                return mCardPresenter;
            } else {
                return mAddRemoveButtonPresenter;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ChannelContents.createPlaylists(this.getActivity());

        mChannelContents = ChannelContents.sChannelContents;
        loadRows();

        /**
         * Evey time when the fragment is created, this async task will be executed
         * to load which channel has been published on main screen
         * So the status of adding/ removing button will be toggled accordingly
         */
        new LoadAddedChannels();

        prepareBackgroundManager();
        setupEventListeners();
        mBackgroudUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (mBackgroundURI != null) {
                    updateBackgroundImage(mBackgroundURI.toString());
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mBackgroudUpdateRunnable);
    }

    /**
     * This function will be executed every time when the permission request activity is finished.
     *
     * If the answer allows to add channel to main screen, Async task LoadAddedChannels will be
     * executed to load which channel has been published to main screen.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_CHANNEL_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (DEBUG) {
                    Log.d(TAG, "channel added");
                }
                new LoadAddedChannels();
            } else {
                Log.e(TAG, "could not add channel");
            }
        }
    }

    /**
     * Populate card/ adding-removing button into ArrayObjectAdapter
     */
    private void loadRows() {
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        for (int i = 0; i < mChannelContents.size(); i++) {
            ChannelContents playlist = mChannelContents.get(i);
            List<VideoContent> clips = playlist.getVideos();
            CardPresenterSelector presenterSelector = new CardPresenterSelector(getContext());
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(presenterSelector);
            listRowAdapter.add(playlist);
            for (int j = 0; j < clips.size(); ++j) {
                listRowAdapter.add(clips.get(j));
            }
            HeaderItem header = new HeaderItem(i, playlist.getName());
            rowsAdapter.add(new ListRow(header, listRowAdapter));
        }
        setAdapter(rowsAdapter);
    }

    /**
     * Async Task to add channel to main screen/ remove channel from main screen
     */

    private final class CreateChannelInMainScreen extends AsyncTask<ChannelContents, Void, Long> {
        private static final String SCHEME = "rowsnewapi";
        private static final String PACKAGE_NAME = "android.support.v17.leanback.supportleanbackshowcase";
        private static final String VIDEO_PLAY_ACTIVITY = "playvideo";
        private static final String MAIN_ACTIVITY = "startapp";
        public static final String CONTENT_ANDROID_MEDIA_TV_PREVIEW_PROGRAM = "content://android.media.tv/preview_program";

        private void createChannelLogo(Context context, long channelId,
                                       @DrawableRes int drawableId) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);
            ChannelLogoUtils.storeChannelLogo(context, channelId, bitmap);
        }

        private String createInputId(Context context) {
            ComponentName cName = new ComponentName(context, DynamicVideoRowsActivity.class.getName());
            return TvContractCompat.buildInputId(cName);
        }


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
            return addChannel(DynamicRowsFragment.this.getActivity(), params[0]);
        }

        @Override
        protected void onPostExecute(Long channelId) {
            Intent intent = new Intent(TvContract.ACTION_REQUEST_CHANNEL_BROWSABLE);
            intent.putExtra(TvContractCompat.EXTRA_CHANNEL_ID, channelId);
            try {
                startActivityForResult(intent, ADD_CHANNEL_REQUEST);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "could not start add channel approval UI", e);
            }
        }
    }

    private final class RemoveChannelInMainScreen extends AsyncTask<ChannelContents, Void, Void> {

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
            deleteChannel(getActivity(), playlist.getChannelId());
            return null;
        }
    }

    /**
     * ContentProvider project scheme
     */
    private static final String[] CHANNELS_MAP_PROJECTION =
            {TvContractCompat.Channels._ID, TvContractCompat.Channels.COLUMN_INTERNAL_PROVIDER_ID};

    /**
     * Set query index according to the projection map specific to channel
     */
    private static final int CHANNELS_COLUMN_ID_INDEX = 0;
    private static final int CHANNELS_COLUMN_INTERNAL_PROVIDER_ID_INDEX = 1;

    /**
     * An internal class to group the information of channel Contents ID (internal ID) and published
     * ID
     * The reason to have this class is to use channel contents ID (which is unique and won't be
     * changed for each channel) to locate the published ID (which will be changed alongwith
     * the transaction on content provider)
     */
    static final class ChannelPlaylistId {
        String mId;
        long mChannelId;

        ChannelPlaylistId(String id, long channelId) {
            mId = id;
            mChannelId = channelId;
        }

    }

    private class LoadAddedChannels extends AsyncTask<Void, Void, Void> {
        LoadAddedChannels() {
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
    }

    private void loadChannels() {
        try (Cursor cursor = getActivity().getContentResolver().query(TvContract.Channels.CONTENT_URI,
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
     * when the async task is finished, this function will be executed to toggle channel's status
     * based on if this channel is published or not
     */
    public void onPublishedChannelsLoaded(List<ChannelPlaylistId>
                                                  publishedChannels) {
        HashMap<String, ChannelPlaylistId> loadedChannels = new HashMap<>();
        for (ChannelPlaylistId channelPlaylist : publishedChannels) {
            loadedChannels.put(channelPlaylist.mId, channelPlaylist);
        }

        for (ChannelContents channelContents : mChannelContents) {
            ChannelPlaylistId channelPlaylistId =
                    loadedChannels.get(channelContents.getChannelContentsId());
            if (channelPlaylistId != null) {
                channelContents.setChannelPublishedId(channelPlaylistId.mChannelId);
            }
        }

        ArrayObjectAdapter rowsAdapter = (ArrayObjectAdapter) getAdapter();
        for (int ndx = 0, size = rowsAdapter.size(); ndx < size; ++ndx) {
            ListRow listRow = (ListRow) rowsAdapter.get(ndx);
            ArrayObjectAdapter listRowAdapter = (ArrayObjectAdapter) listRow.getAdapter();
            listRowAdapter.notifyArrayItemRangeChanged(0, 1);
        }
    }



    /**
     * Click and select event listener
     */

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (isAdded()) {
                if (item instanceof VideoContent) {
                    VideoContent clip = (VideoContent) item;
                    Intent intent = new Intent(getActivity(), VideoPlaybackActivity.class);
                    intent.putExtra(VideoPlaybackActivity.VIDEO_CONTENT, clip);
                    startActivity(intent);
                } else if (item instanceof ChannelContents) {
                    AddRemoveButtonPresenter.AddRemoveChannelViewHolder addRemoveChannelViewHolder
                            = (AddRemoveButtonPresenter.AddRemoveChannelViewHolder) itemViewHolder;
                    ChannelContents playlist = (ChannelContents) item;
                    if (addRemoveChannelViewHolder.getMode() ==
                            AddRemoveButtonPresenter.AddRemoveChannelViewHolder.ADD_MODE) {
                        new CreateChannelInMainScreen().execute(playlist);

                    } else {
                        new RemoveChannelInMainScreen().execute(playlist);
                        addRemoveChannelViewHolder.setMode(
                                AddRemoveButtonPresenter.AddRemoveChannelViewHolder.ADD_MODE);
                        Toast.makeText(getActivity(), getString(R.string.channel_removed),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof VideoContent) {
                mBackgroundURI = ((VideoContent) item).getBackgroundImageURI();
                startBackgroundTimer();
            }
        }
    }

    /**
     * Helper functions for background image loading and associated animation
     */

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    protected void updateBackgroundImage(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .into(new SimpleTarget<Drawable>(width, height) {
                    @Override
                    public void onResourceReady(Drawable resource,
                                                Transition<? super Drawable> glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
        mHandler.removeCallbacks(mBackgroudUpdateRunnable);
    }

    private void startBackgroundTimer() {
        mHandler.removeCallbacks(mBackgroudUpdateRunnable);
        mHandler.postDelayed(mBackgroudUpdateRunnable, BACKGROUND_UPDATE_DELAY);
    }
}
