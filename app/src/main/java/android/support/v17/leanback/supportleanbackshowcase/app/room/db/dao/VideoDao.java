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

package android.support.v17.leanback.supportleanbackshowcase.app.room.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.constant.DatabaseContract;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;

import java.util.List;

@Dao
public interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllVideos(List<VideoEntity> videos);

    @Query("SELECT * FROM " + DatabaseContract.VideoEntry.TABLE_NAME
            + " WHERE " + DatabaseContract.VideoEntry.COLUMN_AUTO_GENERATE_ID
            + " = :videoId")
    LiveData<VideoEntity> loadVideoById(long videoId);


    @Query("SELECT * FROM " + DatabaseContract.VideoEntry.TABLE_NAME
            + " WHERE " + DatabaseContract.VideoEntry.COLUMN_CATEGORY
            + " = :category")
    LiveData<List<VideoEntity>> loadVideoInSameCateogry(String category);

    @Query("SELECT * FROM " + DatabaseContract.VideoEntry.TABLE_NAME
            + " WHERE "+ DatabaseContract.VideoEntry.COLUMN_NAME
            + " LIKE " + ":queryMessage"
            + " OR " + DatabaseContract.VideoEntry.COLUMN_CATEGORY
            +" LIKE " + ":queryMessage")
    LiveData<List<VideoEntity>> searchVideos(String queryMessage);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateVideo(VideoEntity video);
}
