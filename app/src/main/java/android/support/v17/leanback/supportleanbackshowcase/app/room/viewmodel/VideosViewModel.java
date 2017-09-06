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

package android.support.v17.leanback.supportleanbackshowcase.app.room.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.DatabaseHelper;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.CategoryEntity;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;

import java.util.List;

public class VideosViewModel extends AndroidViewModel {
    // live data connect to database
    private final LiveData<List<CategoryEntity>> mAllCategories;
    private final LiveData<List<VideoEntity>> mSearchResults;
    private final LiveData<VideoEntity> mVideoById;
    private final LiveData<List<VideoEntity>> mAllVideosByCategory;

    // mutable live data can be changed by ui controllers through setter
    private final MutableLiveData<String> mQuery = new MutableLiveData<>();
    private final MutableLiveData<Long> mVideoId = new MutableLiveData<>();
    private final MutableLiveData<String> mVideoCategory = new MutableLiveData<>();
    private DatabaseHelper mDatabaseHelper;

    public VideosViewModel(Application application) {
        super(application);

        mDatabaseHelper = DatabaseHelper.getInstance();

        mAllCategories = mDatabaseHelper
                .getDatabase(this.getApplication())
                .categoryDao()
                .loadAllCategories();

        /**
         * Using switch map function to react to the change of observed variable, the benefits of
         * this mapping method is we don't have to re-create the live data every time.
         */
        mAllVideosByCategory = Transformations.switchMap(
                mVideoCategory, new Function<String, LiveData<List<VideoEntity>>>() {
                    @Override
                    public LiveData<List<VideoEntity>> apply(final String category) {

                        /**
                         * Fetching live data from database
                         */
                        return mDatabaseHelper
                                .getDatabase(getApplication())
                                .videoDao()
                                .loadVideoInSameCateogry(category);
                    }
                });

        mSearchResults = Transformations.switchMap(
                mQuery, new Function<String, LiveData<List<VideoEntity>>>() {
                    @Override
                    public LiveData<List<VideoEntity>> apply(final String queryMessage) {

                        /**
                         * Fetching live data from database
                         */
                        return mDatabaseHelper
                                .getDatabase(getApplication())
                                .videoDao()
                                .searchVideos(queryMessage);
                    }
                });


        mVideoById = Transformations.switchMap(
                mVideoId, new Function<Long, LiveData<VideoEntity>>() {
                    @Override
                    public LiveData<VideoEntity> apply(final Long videoId) {

                        /**
                         * Fetching live data from database
                         */
                        return mDatabaseHelper
                                .getDatabase(getApplication())
                                .videoDao()
                                .loadVideoById(videoId);
                    }
                });

    }

    public LiveData<List<VideoEntity>> getSearchResult() {
        return mSearchResults;
    }

    public LiveData<VideoEntity> getVideoById() {
        return mVideoById;
    }

    public LiveData<List<VideoEntity>> getVideosInSameCategory() {
        return mAllVideosByCategory;
    }

    public LiveData<List<CategoryEntity>> getAllCategories() {
        return mAllCategories;
    }

    public void setQueryMessage(String queryMessage) {
        mQuery.setValue(queryMessage);
    }

    public void setVideoId(Long videoIdVal) {
        mVideoId.setValue(videoIdVal);
    }

    public void setCategory(String category) {
        mVideoCategory.setValue(category);
    }
}
