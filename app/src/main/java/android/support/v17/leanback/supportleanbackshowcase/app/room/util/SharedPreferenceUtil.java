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

package android.support.v17.leanback.supportleanbackshowcase.app.room.util;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.SharedPreferences;
import android.support.v17.leanback.supportleanbackshowcase.app.room.SampleApplication;

/**
 * Utility function to simplify the access and modification of SharedPreference
 */
public class SharedPreferenceUtil {
    public static final String PREF_NAME = "preference_name";

    /**
     * Update the writing permission to be true in shared preference.
     */
    public static void updateWritingExternalPermission() {
        SharedPreferences settings = SampleApplication.getInstance().getSharedPreferences(PREF_NAME,
                MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settings.edit();

        prefEditor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
        prefEditor.commit();
    }

    /**
     * Judge if current application has been authorized with writing permission on device's external
     * storage
     *
     * @return If current application has the runtime permission to write to external storage.
     */
    public static boolean isPermitted() {
        // open shared preferences using perf name
        SharedPreferences sharedPreferences = SampleApplication.getInstance().getSharedPreferences(
                PREF_NAME, MODE_PRIVATE);

        boolean isPermitted = sharedPreferences.getBoolean(
                Manifest.permission.WRITE_EXTERNAL_STORAGE, false);
        return isPermitted;
    }
}
