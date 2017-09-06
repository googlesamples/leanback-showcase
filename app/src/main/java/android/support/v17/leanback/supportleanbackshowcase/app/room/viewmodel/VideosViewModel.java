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
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;
import android.support.v17.leanback.supportleanbackshowcase.app.room.config.AppConfiguration;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.DatabaseHelper;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.CategoryEntity;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;

import java.util.List;

public class VideosViewModel extends AndroidViewModel {
    // live data connect to database
    private LiveData<List<CategoryEntity>> mAllCategories;
    private LiveData<List<VideoEntity>> mSearchResults;
    private LiveData<VideoEntity> mVideoById;
    private LiveData<List<VideoEntity>> mAllVideosByCategory;
    // mutable live data can be changed by ui controllers through setter
    private final MutableLiveData<String> mQuery = new MutableLiveData<>();
    private final MutableLiveData<Long> mVideoId = new MutableLiveData<>();
    private final MutableLiveData<String> mVideoCategory = new MutableLiveData<>();
    private DatabaseHelper mDatabaseHelper;

    public VideosViewModel(Application application) {
        super(application);

        mDatabaseHelper = DatabaseHelper.getInstance();

        // create database in main thread
        mDatabaseHelper.createDb(this.getApplication());

        mAllCategories =
                mDatabaseHelper
                        .getDatabase()
                        .categoryDao()
                        .loadAllCategories();

        if (AppConfiguration.IS_DATABASE_ACCESS_LATENCY_ENABLED) {

            /**
             * Emit the result using specified delay
             */
            mAllCategories = sendThroughMediatorLiveData(mAllCategories, 1000L);
        }

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
                        final LiveData<List<VideoEntity>> source =
                                mDatabaseHelper
                                        .getDatabase()
                                        .videoDao()
                                        .loadVideoInSameCateogry(
                                                category);
                        if (AppConfiguration.IS_DATABASE_ACCESS_LATENCY_ENABLED) {

                            /**
                             * Emit the result using specified delay
                             */
                            return sendThroughMediatorLiveData(source, 2000L);

                        }
                        return source;
                    }
                });

        mSearchResults = Transformations.switchMap(
                mQuery, new Function<String, LiveData<List<VideoEntity>>>() {
                    @Override
                    public LiveData<List<VideoEntity>> apply(final String queryMessage) {

                        /**
                         * Fetching live data from database
                         */
                        final LiveData<List<VideoEntity>> source =
                                mDatabaseHelper
                                        .getDatabase()
                                        .videoDao()
                                        .searchVideos(queryMessage);
                        if (AppConfiguration.IS_DATABASE_ACCESS_LATENCY_ENABLED
                                || AppConfiguration.IS_SEARCH_LATENCY_ENABLED) {

                            /**
                             * Emit the result using specified delay
                             */
                            return sendThroughMediatorLiveData(source, 3000L);
                        } else {
                            return source;
                        }
                    }
                });


        mVideoById = Transformations.switchMap(
                mVideoId, new Function<Long, LiveData<VideoEntity>>() {
                    @Override
                    public LiveData<VideoEntity> apply(final Long videoId) {

                        /**
                         * Fetching live data from database
                         */
                        final LiveData<VideoEntity> source =
                                mDatabaseHelper
                                        .getDatabase()
                                        .videoDao()
                                        .loadVideoById(videoId);

                        /**
                         * If latency is enabled, we will return the data using the mediator
                         * live data
                         */
                        if (AppConfiguration.IS_DATABASE_ACCESS_LATENCY_ENABLED) {
                            return sendThroughMediatorLiveData(source, 2000L);
                        }
                        return source;
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

    /**
     * Helper function to use mediator live data to emit the live data using specified delay
     *
     * @param source source live data
     * @param ms     for delay
     * @param <T>    The type of data you want to emit in the live data
     * @return The mediator live data
     */
    private <T> MediatorLiveData<T> sendThroughMediatorLiveData(LiveData<T> source, final Long ms) {
        final MediatorLiveData<T> mediator =
                new MediatorLiveData<>();

        mediator.addSource(source, new Observer<T>() {
            @Override
            public void onChanged(@Nullable final T sourceEntity) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(ms);
                            mediator.postValue(sourceEntity);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        return mediator;
    }
}
