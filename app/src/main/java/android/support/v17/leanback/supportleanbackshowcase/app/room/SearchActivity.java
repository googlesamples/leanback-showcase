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

package android.support.v17.leanback.supportleanbackshowcase.app.room;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.SearchFragment;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class SearchActivity extends FragmentActivity implements LifecycleRegistryOwner {
    private static final String TAG = "SearchActivity";
    private static boolean DEBUG = true;

    /**
     * If using internal speech recognizer, you must have RECORD_AUDIO permission
     */
    private static final boolean USE_INTERNAL_SPEECH_RECOGNIZER = true;
    private static final int REQUEST_SPEECH = 1;
    private SearchFragment mFragment;
    private SpeechRecognitionCallback mSpeechRecognitionCallback;

    // set up lifecycle registry
    private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        mFragment = (SearchFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
        if (!USE_INTERNAL_SPEECH_RECOGNIZER) {
            mSpeechRecognitionCallback = new SpeechRecognitionCallback() {
                @Override
                public void recognizeSpeech() {
                    if (DEBUG) {
                        Log.v(TAG, "recognizeSpeech");
                    }
                    startActivityForResult(mFragment.getRecognizerIntent(), REQUEST_SPEECH);
                }
            };
            mFragment.setSpeechRecognitionCallback(mSpeechRecognitionCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG){
            Log.v(TAG, "onActivityResult requestCode="
                    + requestCode +
                    " resultCode="
                    + resultCode +
                    " data="
                    + data);
        }
        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK) {
            mFragment.setSearchQuery(data, true);
        }
    }

}
