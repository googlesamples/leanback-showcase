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


package android.support.v17.leanback.supportleanbackshowcase.app.room.db.repo;

import android.arch.lifecycle.LiveData;
import android.support.v17.leanback.supportleanbackshowcase.app.room.SampleApplication;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.AppDatabase;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.DatabaseHelper;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.dao.CategoryDao;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.dao.VideoDao;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.CategoryEntity;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;

import java.util.HashMap;
import java.util.List;

public class VideosRepository {
    private static VideosRepository sVideosRepository;

    private VideoDao mVideoDao;
    private CategoryDao mCategoryDao;

    // maintain the local cache so the live data can be shared among different components
    private HashMap<String, LiveData<List<VideoEntity>>> mRepositoryCache;

    public static VideosRepository getVideosRepositoryInstance() {
        if (sVideosRepository == null) {
            sVideosRepository = new VideosRepository();
        }
        return sVideosRepository;
    }

    private VideosRepository() {
        AppDatabase appDatabase = DatabaseHelper.getInstance().getDatabase(SampleApplication.getInstance());
        mVideoDao = appDatabase.videoDao();
        mCategoryDao = appDatabase.categoryDao();
        mRepositoryCache = new HashMap<>();
    }

    public LiveData<List<VideoEntity>> getVideosInSameCategoryLiveData(String category) {

        // always try to retrive from local cache firstly
        if (mRepositoryCache.containsKey(category)) {
            return mRepositoryCache.get(category);
        }
        return mVideoDao.loadVideoInSameCateogry(category);
    }

    public LiveData<List<CategoryEntity>> getAllCategories() {
        return mCategoryDao.loadAllCategories();
    }

    public LiveData<List<VideoEntity>> getSearchResult(String query) {
        return mVideoDao.searchVideos(query);
    }

    public LiveData<VideoEntity> getVideoById(Long id) {
        return mVideoDao.loadVideoById(id);
    }
}
