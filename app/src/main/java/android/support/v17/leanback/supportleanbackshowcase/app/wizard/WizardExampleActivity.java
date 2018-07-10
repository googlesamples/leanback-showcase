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

package android.support.v17.leanback.supportleanbackshowcase.app.wizard;

import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepSupportFragment;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.support.v4.app.FragmentActivity;

/**
 * An Activity displaying a wizard for renting a movie.
 */
public class WizardExampleActivity extends FragmentActivity {

    public static final String WATCH_NOW = "watch_movie_now";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.wizard_background_blackned);

        GuidedStepSupportFragment fragment = new WizardExample1stStepFragment();
        fragment.setArguments(getIntent().getExtras()); // Delegate Movie to first step.
        GuidedStepSupportFragment.addAsRoot(this, fragment, android.R.id.content);
    }

    @Override
    public void onBackPressed() {
        if (GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(getSupportFragmentManager())
                instanceof WizardExample4thStepFragment) {
            // The user 'bought' the product. When he presses 'Back' the Wizard will be closed and
            // he will not be send back to 'Processing Payment...'-Screen.
            finish();
        } else super.onBackPressed();
    }

}
