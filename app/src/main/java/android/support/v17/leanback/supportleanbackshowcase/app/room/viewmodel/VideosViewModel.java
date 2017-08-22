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
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.DatabaseCreator;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.CategoryEntity;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;

import java.util.List;

public class VideosViewModel extends AndroidViewModel {

    private static final MutableLiveData ABSENT = new MutableLiveData();
    {
        ABSENT.setValue(null);
    }

    private DatabaseCreator mDatabaseCreator;

    // live data connect to database
    private final LiveData<List<CategoryEntity>> mAllCategories;
    private final LiveData<List<VideoEntity>> mSearchResults;
    private final LiveData<VideoEntity> mVideoById;
    private final LiveData<List<VideoEntity>> mAllVideosByCategory;

    // mutable live data can be changed by ui controllers through setter
    private final MutableLiveData<String> mQuery = new MutableLiveData<>();
    private final MutableLiveData<Long> mVideoId = new MutableLiveData<>();
    private final MutableLiveData<String> mVideoCategory = new MutableLiveData<>();


    public VideosViewModel(Application application) {
        super(application);

        mDatabaseCreator = DatabaseCreator.getInstance();

        final LiveData<Boolean> databaseCreated = mDatabaseCreator.isDatabaseCreated();

        /**
         * Always check if database is created firstly
         */
        mAllCategories = Transformations.switchMap(databaseCreated,
                new Function<Boolean, LiveData<List<CategoryEntity>>>() {
                    @Override
                    public LiveData<List<CategoryEntity>> apply(Boolean isDatabaseCreated) {
                        if (!Boolean.TRUE.equals(isDatabaseCreated)) {
                            return ABSENT;
                        } else {
                            LiveData<List<CategoryEntity>> source =
                                    mDatabaseCreator.getDatabase().categoryDao().loadAllCategories();

                            final MediatorLiveData<List<CategoryEntity>> mediator =
                                    new MediatorLiveData<>();

                            mediator.addSource(source, new Observer<List<CategoryEntity>>() {
                                @Override
                                public void onChanged(@Nullable final List<CategoryEntity> categoryEntities) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            try {
                                                Thread.sleep(1000);
                                                mediator.postValue(categoryEntities);
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

        mAllVideosByCategory = Transformations.switchMap(databaseCreated,
                new Function<Boolean, LiveData<List<VideoEntity>>>() {
                    @Override
                    public LiveData<List<VideoEntity>> apply(Boolean isDatabaseCreated) {
                        if (!Boolean.TRUE.equals(isDatabaseCreated)) {
                            return ABSENT;
                        } else {
                            return Transformations.switchMap(mVideoCategory,
                                    new Function<String, LiveData<List<VideoEntity>>>() {
                                        @Override
                                        public LiveData<List<VideoEntity>> apply(String category) {
                                            final LiveData<List<VideoEntity>> source =
                                                    mDatabaseCreator.getDatabase().videoDao()
                                                            .loadVideoInSameCateogry(category);
                                            final MediatorLiveData<List<VideoEntity>> mediator =
                                                    new MediatorLiveData<>();

                                            /**
                                             * Emit the result using specified delay
                                             */
                                            mediator.addSource(source, new Observer<List<VideoEntity>>() {
                                                @Override
                                                public void onChanged(
                                                        @Nullable final List<VideoEntity> videoEntities) {
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            try {
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
                                    });
                        }
                    }
                });

        mSearchResults = Transformations.switchMap(databaseCreated, new Function<Boolean,
                LiveData<List<VideoEntity>>>() {
            @Override
            public LiveData<List<VideoEntity>> apply(Boolean isDatabaseCreated) {
                if (!Boolean.TRUE.equals(isDatabaseCreated)) {
                    return ABSENT;
                } else {
                    return Transformations.switchMap(mQuery, new Function<String, LiveData<List<VideoEntity>>>() {
                        @Override
                        public LiveData<List<VideoEntity>> apply(String queryMessage) {
                            final LiveData<List<VideoEntity>> source =
                                    mDatabaseCreator.getDatabase().videoDao().searchVideos(queryMessage);
                            final MediatorLiveData<List<VideoEntity>> mediator = new MediatorLiveData<>();

                            mediator.addSource(source, new Observer<List<VideoEntity>>() {
                                @Override
                                public void onChanged(@Nullable final List<VideoEntity> videoEntities) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(3000);
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
                    });
                }
            }
        });

        mVideoById = Transformations.switchMap(databaseCreated, new Function<Boolean, LiveData<VideoEntity>>() {
            @Override
            public LiveData<VideoEntity> apply(Boolean isDatabaseCreated) {
                if (!Boolean.TRUE.equals(isDatabaseCreated)) {
                    return ABSENT;
                } else {
                    return Transformations.switchMap(mVideoId, new Function<Long, LiveData<VideoEntity>>() {
                        @Override
                        public LiveData<VideoEntity> apply(Long input) {
                            final LiveData<VideoEntity> source =
                                    mDatabaseCreator.getDatabase().videoDao().loadVideoById(input);
                            final MediatorLiveData<VideoEntity> mediator = new MediatorLiveData<>();

                            mediator.addSource(source, new Observer<VideoEntity>() {
                                @Override
                                public void onChanged(@Nullable final VideoEntity videoEntity) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            try {
                                                Thread.sleep(2000);
                                                mediator.postValue(videoEntity);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                }
                            });
                            return mediator;
                        }
                    });
                }
            }
        });

        // try to create database
        mDatabaseCreator.createDb(this.getApplication());
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
