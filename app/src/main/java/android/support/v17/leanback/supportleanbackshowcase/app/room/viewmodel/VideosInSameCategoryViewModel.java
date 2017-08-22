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
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.DatabaseCreator;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;

import java.util.List;

public class VideosInSameCategoryViewModel extends AndroidViewModel{

    private static final MutableLiveData ABSENT = new MutableLiveData();
    {
        ABSENT.setValue(null);
    }

    private final String mCategory;

    /**
     * List of VideoEntities in same category
     */
    private final LiveData<List<VideoEntity>> mVideosInSameCategory;

    public VideosInSameCategoryViewModel(@NonNull Application application, final String category) {
        super(application);
        this.mCategory = category;

        final DatabaseCreator databaseCreator = DatabaseCreator.getInstance();

        mVideosInSameCategory = Transformations.switchMap(databaseCreator.isDatabaseCreated(),
                new Function<Boolean, LiveData<List<VideoEntity>>>() {
            @Override
            public LiveData<List<VideoEntity>> apply(Boolean input) {
                if (!input){
                    return ABSENT;
                }else {
                    LiveData<List<VideoEntity>> source =
                            databaseCreator.getDatabase().videoDao().loadVideoInSameCateogry(
                                    mCategory);

                    final MediatorLiveData<List<VideoEntity>> mediator = new MediatorLiveData<>();

                    /**
                     * Emit the result with specified delay using mediator livedata
                     */
                    mediator.addSource(source, new Observer<List<VideoEntity>>() {
                        @Override
                        public void onChanged(@Nullable final List<VideoEntity> videoEntities) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        Thread.sleep(2000);
                                        mediator.postValue(videoEntities);
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
        });

        databaseCreator.createDb(this.getApplication());
    }

    public LiveData<List<VideoEntity>> getVideosInSameCategory() {
        return mVideosInSameCategory;
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
            return (T)new VideosInSameCategoryViewModel(mApplication, mCategory);
        }
    }
}
