/*
 * Copyright (c) 2016 The Android Open Source Project
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

package android.support.v17.leanback.supportleanbackshowcase.input.player;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.util.Util;
import com.google.android.media.tv.companionlibrary.utils.TvContractUtils;

public class RendererBuilderFactory {

    public static LeanbackPlayer.RendererBuilder createRendererBuilder(
            Context context, int contentType, Uri contentUri) {
        String userAgent = Util.getUserAgent(context, "ExoVideoPlayer");

        switch (contentType) {
            case TvContractUtils.SOURCE_TYPE_HTTP_PROGRESSIVE: {
                return new ExtractorRendererBuilder(context, userAgent, contentUri);
            }
            default: {
                throw new IllegalStateException("Unsupported type: " + contentType);
            }
        }
    }
}
