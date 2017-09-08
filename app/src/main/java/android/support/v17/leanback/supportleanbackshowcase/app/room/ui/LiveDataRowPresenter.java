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
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v17.leanback.supportleanbackshowcase.app.room.SampleApplication;
import android.support.v17.leanback.supportleanbackshowcase.app.room.adapter.ListAdapter;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;
import android.support.v17.leanback.supportleanbackshowcase.app.room.viewmodel.VideosInSameCategoryViewModel;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The presenter for live data row
 */
public class LiveDataRowPresenter extends ListRowPresenter{

    private ListRow mRow;
    private List<StartEntranceListener> mEntranceListeners;
    private VideosInSameCategoryViewModel mViewModel;
    private LifecycleOwner mLifecycleOwner;

    public LiveDataRowPresenter() {
        super();
        mEntranceListeners = new ArrayList<>();
    }

    public interface StartEntranceListener {
        void startEntrance();
    }

    /**
     * Register Entrance Listener.
     */
    public void registerStartEntranceListener(StartEntranceListener listener) {
        mEntranceListeners.add(listener);
    }

    /**
     * Dispatch the event when the data is bound to the adapter.
     */
    public void notifyEntranceStarted() {
        for (int i = 0; i < mEntranceListeners.size(); i++) {
            mEntranceListeners.get(i).startEntrance();
        }
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        mRow = (ListRow)item;

        String category = mRow.getHeaderItem().getName();

        final ListAdapter<VideoEntity> adapter = (ListAdapter<VideoEntity>) mRow.getAdapter();

        FragmentActivity attachedFragmentActivity = (FragmentActivity) holder.view.getContext();

        // In our case, attached activity should be a lifecycle owner
        mLifecycleOwner = (LifecycleOwner) attachedFragmentActivity;

        // each category will have a separate view model
        VideosInSameCategoryViewModel.Factory factory =
                new VideosInSameCategoryViewModel.Factory(
                        SampleApplication.getInstance(), category);

        // view model will not be re-created as long as the lifecycle owner
        // lifecycle observer and tag doesn't change
        mViewModel = ViewModelProviders.of(attachedFragmentActivity, factory).get(
                        category, VideosInSameCategoryViewModel.class);

        // observe the live data when this row is bound to view holder
        mViewModel.getVideosInSameCategory().observe(mLifecycleOwner,
                new Observer<List<VideoEntity>>() {
                    @Override
                    public void onChanged(
                            @Nullable List<VideoEntity> videoEntities) {
                        if (videoEntities != null) {

                            // When the data is bound to the adapter, dispatch start Entrance
                            // transition event
                            notifyEntranceStarted();

                            adapter.setItems(videoEntities,
                                    new Comparator<VideoEntity>() {
                                        @Override
                                        public int compare(VideoEntity o1,
                                                           VideoEntity o2) {
                                            return o1.getId() == o2.getId() ? 0 : -1;
                                        }
                                    }, new Comparator<VideoEntity>() {
                                        @Override
                                        public int compare(VideoEntity o1,
                                                           VideoEntity o2) {
                                            return o1.equals(o2) ? 0 : -1;
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    protected void onUnbindRowViewHolder(RowPresenter.ViewHolder holder) {
        super.onUnbindRowViewHolder(holder);
        mViewModel.getVideosInSameCategory().removeObservers(mLifecycleOwner);
    }
}
