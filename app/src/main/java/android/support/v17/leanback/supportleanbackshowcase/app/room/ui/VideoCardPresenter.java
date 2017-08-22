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

package android.support.v17.leanback.supportleanbackshowcase.app.room.ui;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.DatabaseCreator;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.dao.VideoDao;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;
import android.support.v17.leanback.supportleanbackshowcase.app.room.network.NetworkLiveData;
import android.support.v17.leanback.supportleanbackshowcase.app.room.network.NetworkManagerUtil;
import android.support.v17.leanback.supportleanbackshowcase.app.room.util.SharedPreferenceUtil;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

/**
 * The video card presenter which can respond to long click action and present current video's
 * state (Downloading/ Removing/ Downloaded)
 */
public class VideoCardPresenter extends Presenter {

    // For debugging purpose
    private static final boolean DEBUG = false;
    private static final String TAG = "VideoCardPresenter";

    private static final String VIDEO = "video";
    private static final String BACKGROUND = "background";
    private static final String CARD = "card";

    // The default resource when the network or local content are not available.
    private static int sSelectedBackgroundColor = -1;
    private static int sDefaultBackgroundColor = -1;
    private static Drawable sDefaultCardImage;


    /**
     * The view holder which will encapsulate all the information related to currently bond video.
     */
    private final class CardViewHolder extends ViewHolder implements
            View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
        private VideoEntity mVideo;
        private Context mContext;
        private PopupMenu mPopupMenu;
        private LifecycleOwner mOwner;

        // This dao is required to update the video information in database
        private VideoDao mVideoDao;

        // when glide library cannot fetch data from internet, and there is no local content, it
        // will be used as place holder
        private RequestOptions mDefaultPlaceHolder;
        private Drawable mDefaultBackground;

        private ImageCardView mCardView;


        CardViewHolder(ImageCardView view, Context context) {
            super(view);
            mContext = context;
            Context wrapper = new ContextThemeWrapper(mContext, R.style.MyPopupMenu);
            mPopupMenu = new PopupMenu(wrapper, view);
            mPopupMenu.inflate(R.menu.popup_menu);

            mPopupMenu.setOnMenuItemClickListener(this);
            view.setOnLongClickListener(this);

            mOwner = (LifecycleOwner)  mContext;
            mVideoDao = DatabaseCreator.getInstance().getDatabase().videoDao();

            mDefaultBackground = mContext.getResources().getDrawable(R.drawable.no_cache_no_internet, null);
            mDefaultPlaceHolder = new RequestOptions().
                    placeholder(mDefaultBackground);

            mCardView = (ImageCardView) CardViewHolder.this.view;
            Resources resources = mCardView.getContext().getResources();
            mCardView.setMainImageDimensions(Math.round(
                    resources.getDimensionPixelSize(R.dimen.card_width)),
                    resources.getDimensionPixelSize(R.dimen.card_height));
        }

        @Override
        public boolean onLongClick(View v) {
            mPopupMenu.show();
            return true;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.download_video_related_resource:
                    mVideo.setStatus("downloading");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            mVideoDao.updateVideo(mVideo);
                            return null;
                        }
                    }.execute();
                    NetworkManagerUtil.download(mVideo);
                    return true;
                case R.id.remove_video_related_resource:
                    mVideo.setStatus("removing");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            mVideoDao.updateVideo(mVideo);
                            return null;
                        }
                    }.execute();
                    RemoveFile();
                    return true;
                default:
                    return false;
            }
        }


        private void bind(VideoEntity video) {
            mVideo = video;

            mVideoDao.loadVideoById(video.getId()).observe(mOwner, new Observer<VideoEntity>() {
                @Override
                public void onChanged(@Nullable final VideoEntity videoEntity) {
                    if (videoEntity != null) {

                        mCardView.setTitleText(videoEntity.getTitle());
                        if (isRemovable()) {
                            mCardView.setContentText(videoEntity.getStudio() + " (Downloaded)");
                        } else if (!videoEntity.getStatus().isEmpty() && !isDownloadable()){
                            mCardView.setContentText(videoEntity.getStudio() + " (" + videoEntity.getStatus() +")");
                        } else {
                            mCardView.setContentText(videoEntity.getStudio());
                        }

                        String loadedUri;
                        if (!videoEntity.getVideoCardImageLocalStorageUrl().isEmpty()) {
                            loadedUri = videoEntity.getVideoCardImageLocalStorageUrl();
                        } else {
                            loadedUri = videoEntity.getCardImageUrl();
                        }
                        if (videoEntity.getCardImageUrl() != null) {
                            Glide.with(mCardView.getContext())
                                    .load(loadedUri)
                                    .apply(mDefaultPlaceHolder)
                                    .into(mCardView.getMainImageView());
                        }

                        updatePopMenu(videoEntity);
                    }
                }
            });

        }

        /**
         * Helper function to update pop up menu's item based on network environment and video
         * entity's status
         *
         * @param videoEntity
         */
        private void updatePopMenu(final VideoEntity videoEntity) {
            if (isDownloadable()) {
                setInvisible(R.id.remove_video_related_resource);
                if (!SharedPreferenceUtil.isPermitted()) {
                    updatePopupMenuItem(R.id.download_video_related_resource, false,
                            "Download Video (No Permission)");
                } else {
                    NetworkLiveData.get(mContext).observe(mOwner, new Observer<Boolean>() {
                        @Override
                        public void onChanged(@Nullable Boolean isNetworkAvailable) {
                            if (isNetworkAvailable) {
                                updatePopupMenuItem(R.id.download_video_related_resource, true,
                                        "Download Video");
                            } else {
                                updatePopupMenuItem(R.id.download_video_related_resource, false,
                                        "Download Video (No Network)");
                            }
                        }
                    });
                }
            } else if (isRemovable()) {
                updatePopupMenuItem(R.id.remove_video_related_resource,  true, "Remove Local Video");
                setInvisible(R.id.download_video_related_resource);
            } else {
                updatePopupMenuItem(R.id.download_video_related_resource,  false,
                        videoEntity.getStatus());
                setInvisible(R.id.remove_video_related_resource);
            }
        }

        private void updatePopupMenuItem(int id,  boolean enabled, String title) {
            mPopupMenu.getMenu().findItem(id).setVisible(true).setTitle(title).setEnabled(enabled);
        }

        private void setInvisible(int id) {
            mPopupMenu.getMenu().findItem(id).setVisible(false);
        }


        private void RemoveFile() {
            new FileRemoving().execute(new VideoWithCategory(VIDEO, mVideo));
            new FileRemoving().execute(new VideoWithCategory(CARD, mVideo));
            new FileRemoving().execute(new VideoWithCategory(BACKGROUND, mVideo));
        }

        private class VideoWithCategory {
            private String mCategory;
            private VideoEntity mVideo;

            public VideoWithCategory(String category, VideoEntity video) {
                this.mCategory = category;
                this.mVideo = video;
            }

            public String getCategory() {
                return mCategory;
            }


            public VideoEntity getVideo() {
                return mVideo;
            }

        }

        private class FileRemoving extends AsyncTask<VideoWithCategory, Void, Void> {
            private static final int VIDEO_PATH_START_INDEX = 6;
            private String cat;
            private String url;
            private long id;
            @Override
            protected Void doInBackground(VideoWithCategory... videos) {
                VideoWithCategory par = videos[0];
                cat = par.getCategory();
                id = par.getVideo().getId();
                switch (cat) {
                    case BACKGROUND:
                        url = par.getVideo().getVideoBgImageLocalStorageUrl().substring(VIDEO_PATH_START_INDEX);
                        break;
                    case CARD:
                        url = par.getVideo().getVideoCardImageLocalStorageUrl().substring(VIDEO_PATH_START_INDEX);
                        break;
                    case VIDEO:
                        url = par.getVideo().getVideoLocalStorageUrl().substring(VIDEO_PATH_START_INDEX);
                        break;
                }
                File fileToDelete = new File(url);
                if (fileToDelete.exists()) {
                    try {
                        switch (cat) {
                            case BACKGROUND:
                                Thread.sleep(1000);
                                break;
                            case CARD:
                                Thread.sleep(2000);
                                break;
                            case VIDEO:
                                Thread.sleep(3000);
                                break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    fileToDelete.delete();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                switch (cat) {
                    case BACKGROUND:
                        mVideo.setVideoBgImageLocalStorageUrl("");
                        Toast.makeText(mContext, "bg " + id + " removed", Toast.LENGTH_SHORT).show();
                        break;
                    case CARD:
                        mVideo.setVideoCardImageLocalStorageUrl("");
                        Toast.makeText(mContext, "card " + id + " removed", Toast.LENGTH_SHORT).show();
                        break;
                    case VIDEO:
                        mVideo.setVideoLocalStorageUrl("");
                        Toast.makeText(mContext, "video " + id + " removed", Toast.LENGTH_SHORT).show();
                        break;
                }

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        mVideoDao.updateVideo(mVideo);
                        return null;
                    }
                }.execute();
            }
        }


        /**
         * When all the local storage paths (including video content, background and card image )
         * for the video entity is empty, and current working status is not downloading. It means
         * user can perform download video entity operation at this point
         *
         * @return If user can perform download video operation or not.
         */
        private boolean isDownloadable() {
         return mVideo.getVideoCardImageLocalStorageUrl().isEmpty()
                 && mVideo.getVideoBgImageLocalStorageUrl().isEmpty()
                 && mVideo.getVideoLocalStorageUrl().isEmpty()
                 && !mVideo.getStatus().equals("downloading") ;
        }

        /**
         * When all the local storage paths (including video content, background and card image )
         * for the video entity is not empty, and current working status is not removing. It means
         * user can perform remove video entity operation at this point
         *
         * @return If user can perform remove video operation or not.
         */
        private boolean isRemovable() {
            return !mVideo.getVideoCardImageLocalStorageUrl().isEmpty()
                    && !mVideo.getVideoBgImageLocalStorageUrl().isEmpty()
                    && !mVideo.getVideoLocalStorageUrl().isEmpty()
                    && !mVideo.getStatus().equals("removing");
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context mContext = parent.getContext();
        sDefaultBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.default_background);
        sSelectedBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.selected_background);
        sDefaultCardImage =
                parent.getResources().getDrawable(R.drawable.no_cache_no_internet, null);
        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new CardViewHolder(cardView, mContext);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Object item) {
        VideoEntity video = (VideoEntity) item;
        CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        cardViewHolder.bind(video);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }

    /**
     * Helper function to update selected video clip's background color. Info field should also
     * be updated for consistent ui.
     *
     * @param view
     * @param selected
     */
    private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;

        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }
}
