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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.media.tv.Channel;
import android.support.media.tv.ChannelLogoUtils;
import android.support.media.tv.PreviewProgram;
import android.support.media.tv.TvContractCompat;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.util.Log;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This fragment is designed specific for user to choose which channel they
 * want to publish/ un-publish
 * <p>
 * It extends the GuidedStepFragment to share the similar UI as GuidedStepFragment
 */
public class PublishChannelFragment extends GuidedStepFragment {

    private static final String TAG = "PublishChannelFragment";
    private static final boolean DEBUG = true;

    /**
     * Request code when add channel activity (UI) is started
     */
    private static final int ADD_CHANNEL_REQUEST = 1;

    /**
     * Bitmap which will be put at the front of each checkbox
     */
    private static final int OPTION_DRAWABLE = R.drawable.row_app_banner;

    private List<ChannelPlaylistId> mPublishedChannelContents = new ArrayList<>();

    /**
     * Helper function to add non-checked Action to this fragment
     */
    private static void addAction(List<GuidedAction> actions, Context context, long id,
                                  String title, String desc) {
        actions.add(new GuidedAction.Builder(context)
                .id(id)
                .title(title)
                .description(desc)
                .build());
    }

    /**
     * Helper function to add checked Action to this fragment
     * <p>
     * In this fragment, the checked action is customized as checkbox
     */
    private static void addCheckedAction(List<GuidedAction> actions,
                                         int iconResId,
                                         Context context,
                                         String title,
                                         String desc,
                                         int id,
                                         boolean isActionChecked) {
        GuidedAction guidedAction = new GuidedAction.Builder(context)
                .title(title)
                .description(desc)
                .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
                .icon(context.getResources().getDrawable(iconResId))
                .build();
        guidedAction.setId(id);
        /**
         * Set checkbox status to false initially
         */
        guidedAction.setChecked(isActionChecked);
        actions.add(guidedAction);
    }

    /**
     * Using different theme as attached activity for consistent UI design requirement
     *
     * The theme can be customized in themes.xml
     */
    @Override
    public int onProvideTheme() {
        return R.style.Theme_Leanback_GuidedStep;
    }

    /**
     * This function will be executed every time when the permission request activity is finished.
     * <p>
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

                /**
                 * Every time when add channel activity is finished, the LoadAddedChannels async task
                 * will be executed to fetch channels' publish status
                 */
                new LoadAddedChannels();
                Toast.makeText(this.getActivity(), "Channel Added!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "could not add channel");
            }
        }
    }

    /**
     * The list of channel contents, obtained from ChannelContents class
     */
    private List<ChannelContents> mChannelContents;

    @Override
    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.setting_page_title);
        String breadcrumb = getString(R.string.setting_page_breadcrumb);
        String description = getString(R.string.setting_page_description);
        Drawable icon = getActivity().getDrawable(R.drawable.title_android_tv);

        /**
         * Every time when this fragment is created, the LoadAddedChannels async task will be
         * executed to fetch channels' publish status
         * And create the guide list accordingly
         */
        new LoadAddedChannels();
        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }

    @Override
    public GuidanceStylist onCreateGuidanceStylist() {
        return new GuidanceStylist() {
            @Override
            public int onProvideLayoutId() {
                return R.layout.setting_page;
            }
        };
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        ChannelContents.createPlaylists(this.getActivity());

        mChannelContents = ChannelContents.sChannelContents;

        /**
         * The id of check box was set to the index of current channel
         */
        for (int i = 0; i < ChannelContents.sChannelContents.size(); i++) {
            addCheckedAction(actions,
                    OPTION_DRAWABLE,
                    getActivity(),
                    mChannelContents.get(i).getName(),
                    mChannelContents.get(i).getDescription(),
                    i,
                    mChannelContents.get(i).isChannelPublished());
        }
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        /**
         * Find channel through action ID
         */
        int currentId = (int) action.getId();
        ChannelContents selectedChannelContents = mChannelContents.get(currentId);

        /**
         * Add/ Remove channel from Home Screen using Async task
         * to make sure the UI thread will not be blocked
         */
        if (action.isChecked()) {
            new CreateChannelInMainScreen().execute(selectedChannelContents);
        } else {
            Toast.makeText(this.getActivity(),
                    getResources().getString(R.string.channel_removed_from_home_screen),
                    Toast.LENGTH_SHORT).show();
            new RemoveChannelInMainScreen().execute(selectedChannelContents);
        }
    }


    /**
     * Async Task to add channel to main screen/ remove channel from main screen
     */

    /* package */ final class CreateChannelInMainScreen extends AsyncTask<ChannelContents, Void, Long> {
        private static final String SCHEME = "rowsnewapi";
        private static final String PACKAGE_NAME
                = "android.support.v17.leanback.supportleanbackshowcase";
        private static final String VIDEO_PLAY_ACTIVITY = "playvideo";
        private static final String MAIN_ACTIVITY = "startapp";
        public static final String CONTENT_ANDROID_MEDIA_TV_PREVIEW_PROGRAM
                = "content://android.media.tv/preview_program";

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
            return addChannel(PublishChannelFragment.this.getActivity(), params[0]);
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

    /**
     * Async Task to remove channel from home screen
     */
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
            new LoadAddedChannels();
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

    /**
     * Async task to load published channel
     * So the publish status will be preserved every time when the setting activity is created
     * <p>
     * Usage: Just create the object (new LoadAddedChannels()), then the async task will be executed
     */
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
        /**
         * every time when the async task loadChannels is executed, the published channel list
         * will be updated accordingly
         */
        mPublishedChannelContents.clear();
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
     * when the async task is finished, this function will be executed to assign channel's publish
     * status based on if this channel is published or not
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
            } else {
                /**
                 * If user has unpublished a channel, update the visibility to the most updated
                 * status
                 */
                channelContents.setChannelUnPublished();
            }
        }

    }

}

