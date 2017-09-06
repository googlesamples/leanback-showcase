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

package android.support.v17.leanback.supportleanbackshowcase.app.room.db;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;

import java.io.IOException;

import static android.support.v17.leanback.supportleanbackshowcase.app.room.db.AppDatabase.DATABASE_NAME;

/**
 * Helper class to create/ update the database
 */
public class DatabaseHelper {

    private static final String RENTED = "rented";
    private static final String STATUS = "status";
    private static final String CARD = "card";
    private static final String BACKGROUND = "background";
    private static final String VIDEO = "video";

    private static DatabaseHelper sInstance;

    private AppDatabase mDb;

    public synchronized static DatabaseHelper getInstance() {
        if (sInstance == null) {
            sInstance = new DatabaseHelper();
        }
        return sInstance;
    }

    @Nullable
    public AppDatabase getDatabase(Context context) {
        if (mDb == null) {
            createAndPopulateDatabase(context);
        }
        return mDb;
    }

    private void createAndPopulateDatabase(Context context) {
        mDb = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, DATABASE_NAME).build();
        // insert contents into database
        try {
            String url =
                    "https://storage.googleapis.com/android-tv/";
            DatabaseInitUtil init = new DatabaseInitUtil();
            init.initializeDb(mDb, url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper function to access the database and update the video information in the database.
     *
     * @param video    video entity
     * @param category which fields to update
     * @param value    updated value
     */
    @WorkerThread
    public synchronized void updateDatabase(VideoEntity video, String category, String value) {
        try {
            mDb.beginTransaction();
            switch (category) {
                case VIDEO:
                    video.setVideoLocalStorageUrl(value);
                    break;
                case BACKGROUND:
                    video.setVideoBgImageLocalStorageUrl(value);
                    break;
                case CARD:
                    video.setVideoCardImageLocalStorageUrl(value);
                    break;
                case STATUS:
                    video.setStatus(value);
                    break;
                case RENTED:
                    video.setRented(true);
                    break;
            }
            mDb.videoDao().updateVideo(video);
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
    }
}
