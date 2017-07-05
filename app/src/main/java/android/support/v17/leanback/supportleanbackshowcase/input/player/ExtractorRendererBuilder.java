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

import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

public class ExtractorRendererBuilder implements LeanbackPlayer.RendererBuilder {

    private final Context context;
    private final String userAgent;
    private final Uri uri;

    public ExtractorRendererBuilder(Context context, String userAgent, Uri uri) {
        this.context = context;
        this.userAgent = userAgent;
        this.uri = uri;

    }

    @Override
    public void buildRenderers(LeanbackPlayer player) {
        MediaSource mMediaSource = new ExtractorMediaSource(uri,
                new DefaultDataSourceFactory(context, userAgent), new DefaultExtractorsFactory()
                , null, null);

        MediaCodecAudioRenderer audioRenderer = new MediaCodecAudioRenderer(
                MediaCodecSelector.DEFAULT,
                null,
                true,
                player.getMainHandler(),
                player,
                AudioCapabilities.getCapabilities(context));

        player.onRenderers(audioRenderer, mMediaSource);
    }

    @Override
    public void cancel() {
        // Do nothing.
    }
}
