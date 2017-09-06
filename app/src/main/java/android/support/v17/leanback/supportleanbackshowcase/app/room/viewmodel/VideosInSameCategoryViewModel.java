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
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.supportleanbackshowcase.app.room.config.AppConfiguration;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.DatabaseHelper;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;

import java.util.List;

public class VideosInSameCategoryViewModel extends AndroidViewModel {

    // The parameter used to create view model
    private final String mCategory;

    private DatabaseHelper mDatabaseHelper;

    /**
     * List of VideoEntities in same category
     */
    private LiveData<List<VideoEntity>> mVideosInSameCategory;


    public VideosInSameCategoryViewModel(@NonNull Application application, final String category) {
        super(application);
        this.mCategory = category;

        mDatabaseHelper = DatabaseHelper.getInstance();

        // create database in main thread
        mDatabaseHelper.createDb(this.getApplication());

        mVideosInSameCategory = mDatabaseHelper
                .getDatabase()
                .videoDao()
                .loadVideoInSameCateogry(mCategory);

        if (AppConfiguration.IS_DATABASE_ACCESS_LATENCY_ENABLED) {

            /**
             * Emit the result with specified delay using mediator live data
             */
            mVideosInSameCategory = sendThroughMediatorLiveData(mVideosInSameCategory, 2000L);
        }
    }

    /**
     * Return the video entity list in same category using live data
     *
     * @return live data
     */
    public LiveData<List<VideoEntity>> getVideosInSameCategory() {
        return mVideosInSameCategory;
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

    /**
     * The factory can take category as the parameter to create according view model
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Application mApplication;

        private final String mCategory;

        public Factory(@NonNull Application application, String category) {
            mApplication = application;
            mCategory = category;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new VideosInSameCategoryViewModel(mApplication, mCategory);
        }
    }
}
