/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package android.support.v17.leanback.supportleanbackshowcase.app.room.db;

import android.support.v17.leanback.supportleanbackshowcase.app.room.db.constant.GsonContract;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.CategoryEntity;
import android.support.v17.leanback.supportleanbackshowcase.app.room.db.entity.VideoEntity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;

public class DatabaseInitUtil {
    // For debugging purpose
    private static final boolean DEBUG = false;
    private static final String TAG = "VideoDbBuilder";

    static void initializeDb(AppDatabase db, String url) throws IOException{
        String jsonContent = fetchJsonStringFromUrl(url);
        buildDatabase(jsonContent, db);
    }

    /**
     * Takes the contents of a JSON object and populates the database
     *
     * @param jsonString The JSON String of all videos' information
     */
    public static void buildDatabase(String jsonString, AppDatabase db) throws IOException {
        Gson gson = new Gson();
        VideosWithGoogleTag videosWithGoogleTag = gson.fromJson(jsonString,
                VideosWithGoogleTag.class);
        for (VideosGroupByCategory videosGroupByCategory: videosWithGoogleTag.getAllResources()) {

            // create category table
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setCategoryName(videosGroupByCategory.getCategory());
            db.categoryDao().insertCategory(categoryEntity);

            // create video table
            postProcessing(videosGroupByCategory);
            db.videoDao().insertAllVideos(videosGroupByCategory.getVideos());
        }
    }

    /**
     * Helper function to make some customization on raw data
     */
    private static void postProcessing(VideosGroupByCategory videosGroupByCategory) {
        for (VideoEntity each: videosGroupByCategory.getVideos()) {
            each.setCategory(videosGroupByCategory.getCategory());
            each.setVideoLocalStorageUrl("");
            each.setVideoBgImageLocalStorageUrl("");
            each.setVideoCardImageLocalStorageUrl("");
            each.setVideoUrl(each.getVideoUrls().get(0));
            each.setRented(false);
            each.setStatus("");
            each.setTrailerVideoUrl("https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Say%20more%20with%20Hangouts.mp4");
        }
    }

    /**
     * Fetch JSON string from a given URL.
     *
     * @return the JSONObject representation of the response
     * @throws IOException The BufferedReader operation may throw an IOException.
     */
    private static String fetchJsonStringFromUrl(String urlString) throws IOException {
        BufferedReader reader = null;
        java.net.URL url = new java.net.URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),
                    "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            urlConnection.disconnect();
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "JSON feed closed", e);
                }
            }
        }
    }

    /**
     * The structure of the json file is
     * {
     *   "googlevideos": [{
     *     "category": "Google+",
     *     "videos": [{
     *       "description": "",
     *          ...
     *     }]
     *   }]
     * }
     *
     * So this class is used as a helper class for Gson library to reconstruct the object.
     */
    private static class VideosWithGoogleTag {
        @SerializedName(GsonContract.GOOGLE_VIDEO_TAG)
        private List<VideosGroupByCategory> mAllResources;

        public List<VideosGroupByCategory> getAllResources() {
            return mAllResources;
        }
    }

    /**
     * The structure of the json file is
     * {
     *   "googlevideos": [{
     *     "category": "Google+",
     *     "videos": [{
     *       "description": "",
     *          ...
     *     }]
     *   }]
     * }
     *
     * So this class is another helper class for Gson library to reconstruct the object.
     */
    private static class VideosGroupByCategory {
        @SerializedName("category")
        private String mCategory;

        @SerializedName("videos")
        private List<VideoEntity> mVideos;

        public String getCategory() {
            return mCategory;
        }

        public List<VideoEntity> getVideos() {
            return mVideos;
        }
    }
}
