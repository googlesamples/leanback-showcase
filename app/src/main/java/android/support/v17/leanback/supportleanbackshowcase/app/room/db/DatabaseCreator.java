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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.v17.leanback.supportleanbackshowcase.app.room.db.AppDatabase.DATABASE_NAME;

public class DatabaseCreator {
    private static DatabaseCreator sInstance;

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    private AppDatabase mDb;

    /**
     * use atomic boolean for synchronization
     */
    private final AtomicBoolean mInitializing = new AtomicBoolean(false);


    public synchronized static DatabaseCreator getInstance() {
        if (sInstance == null) {
            sInstance = new DatabaseCreator();
        }
        return sInstance;
    }

    public LiveData<Boolean> isDatabaseCreated() {
        return mIsDatabaseCreated;
    }

    @Nullable
    public AppDatabase getDatabase() {
        return mDb;
    }

    public void createDb(Context context) {

        /**
         * When we create a new view model, it will try to create a new database. This is why
         * we use compareAndSet for synchronization
         */
        if (!mInitializing.compareAndSet(false, true)) {
            // Already initializing
            return;
        }

        // Trigger an update to show a loading screen.
        // It can be used as an independent signal to represent the beginning for fetching data
        // from internet
        mIsDatabaseCreated.setValue(false);

        new AsyncTask<Context, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Context... params) {

                Context context = params[0].getApplicationContext();

                AppDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, DATABASE_NAME).build();

                /**
                 * If database existed,
                 */
                if (!context.getDatabasePath(DATABASE_NAME).exists()) {
                    try {
                        String url =
                                "https://storage.googleapis.com/android-tv/android_tv_videos_new.json";
                        DatabaseInitUtil.initializeDb( db, url);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                mDb = db;
                return true;
            }

            @Override
            protected void onPostExecute(Boolean res) {
                /**
                 * If database is created unsuccessfully, database will be reinitialized
                 * next time
                 */
                if (!res) {
                    mInitializing.compareAndSet(true, false);
                }
                mIsDatabaseCreated.setValue(res);
            }
        }.execute(context.getApplicationContext());
    }
}
