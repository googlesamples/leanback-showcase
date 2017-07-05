/*
 * Copyright 2016 The Android Open Source Project
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
package android.support.v17.leanback.supportleanbackshowcase.input.service;

import android.net.Uri;
import android.support.v17.leanback.supportleanbackshowcase.input.widget.ChannelFeedUtil;


import com.google.android.media.tv.companionlibrary.model.Advertisement;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.google.android.media.tv.companionlibrary.EpgSyncJobService;
import com.google.android.media.tv.companionlibrary.XmlTvParser;

import java.util.ArrayList;
import java.util.List;

/**
 * EpgSyncJobService that periodically runs to update channels and programs.
 */
public class LeanbackTvProgramSync extends EpgSyncJobService {
    private String MPEG_DASH_CHANNEL_NAME = "MPEG_DASH";
    /**
     * Test <a href="http://www.iab.com/guidelines/digital-video-ad-serving-template-vast-3-0/">
     * VAST</a> URL from <a href="https://www.google.com/dfp">DoubleClick for Publishers (DFP)</a>.
     * More sample VAST tags can be found on
     * <a href="https://developers.google.com/interactive-media-ads/docs/sdks/android/tags">DFP
     * website</a>. You should replace it with the vast tag that you applied from your
     * advertisement provider. To verify whether your video ad response is VAST compliant, try<a
     * href="https://developers.google.com/interactive-media-ads/docs/sdks/android/vastinspector">
     * Google Ads Mobile Video Suite Inspector</a>
     */
    private static String TEST_AD_REQUEST_URL =
            "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/" +
                    "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast" +
                    "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct" +
                    "%3Dlinear&correlator=";

    @Override
    public List<Channel> getChannels() {
        // Add channels through an XMLTV file
        XmlTvParser.TvListing listings = ChannelFeedUtil.getTvListings(this);
        List<Channel> channelList = new ArrayList<>(listings.getChannels());

        // Build advertisement list for the channel.
        Advertisement channelAd = new Advertisement.Builder()
                .setType(Advertisement.TYPE_VAST)
                .setRequestUrl(TEST_AD_REQUEST_URL)
                .build();
        List<Advertisement> channelAdList = new ArrayList<>();
        channelAdList.add(channelAd);
        return channelList;
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channel, long startMs,
                                               long endMs) {
        if (!channel.getDisplayName().equals(MPEG_DASH_CHANNEL_NAME)) {
            // Is an XMLTV Channel
            XmlTvParser.TvListing listings = ChannelFeedUtil.getTvListings(getApplicationContext());
            return listings.getPrograms(channel);
        } else {
            return null;
        }
    }
}

