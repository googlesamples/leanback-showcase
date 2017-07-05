/*
 * Copyright 2015 The Android Open Source Project
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

package android.support.v17.leanback.supportleanbackshowcase.input.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.v17.leanback.supportleanbackshowcase.R;
import android.util.Log;

import com.google.android.media.tv.companionlibrary.XmlTvParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Static helper methods for fetching the channel feed.
 */
public class ChannelFeedUtil {
    private static final String TAG = "ChannelFeedUtil";

    // A key for the channel display number used in the app link intent from the xmltv_feed.
    public static final String EXTRA_DISPLAY_NUMBER = "display-number";

    private static XmlTvParser.TvListing sSampleTvListing;

    // For this sample we will use the local XML TV feed. In your real app, you will want to use a
    // remote feed to provide your users with up to date channel listings.
    private static final boolean USE_LOCAL_XML_FEED = true;

    private static final int URLCONNECTION_CONNECTION_TIMEOUT_MS = 3000;  // 3 sec
    private static final int URLCONNECTION_READ_TIMEOUT_MS = 10000;  // 10 sec

    private ChannelFeedUtil() {
    }

    public static XmlTvParser.TvListing getTvListings(Context context) {
        Uri catalogUri = USE_LOCAL_XML_FEED
                ? Uri.parse("android.resource://" + context.getPackageName() + "/"
                + R.raw.input_xmltv_feed)
                : Uri.parse(context.getResources().getString(R.string.input_feed_url))
                .normalizeScheme();
        if (sSampleTvListing != null) {
            return sSampleTvListing;
        }

        try (InputStream inputStream = getInputStream(context, catalogUri)) {
            sSampleTvListing = XmlTvParser.parse(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error in fetching " + catalogUri, e);
        } catch (XmlTvParser.XmlTvParseException e) {
            Log.e(TAG, "Error in parsing " + catalogUri, e);
        }
        return sSampleTvListing;
    }

    public static InputStream getInputStream(Context context, Uri uri) throws IOException {
        InputStream inputStream;
        if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())
                || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())
                || ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            inputStream = context.getContentResolver().openInputStream(uri);
        } else {
            URLConnection urlConnection = new URL(uri.toString()).openConnection();
            urlConnection.setConnectTimeout(URLCONNECTION_CONNECTION_TIMEOUT_MS);
            urlConnection.setReadTimeout(URLCONNECTION_READ_TIMEOUT_MS);
            inputStream = urlConnection.getInputStream();
        }

        return inputStream == null ? null : new BufferedInputStream(inputStream);
    }
}
