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

package android.support.v17.leanback.supportleanbackshowcase.app.room.network;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.support.annotation.MainThread;
import android.util.Log;

public class NetworkLiveData extends LiveData<Boolean> {

    // For debugging purpose
    private static final boolean DEBUG = false;
    private static final String TAG = "NetworkLiveData";

    private static NetworkLiveData sNetworLivekData;

    private ConnectivityManager connectivityManager;

    @MainThread
    public static NetworkLiveData get(Context context) {
        if (sNetworLivekData == null) {
            sNetworLivekData = new NetworkLiveData(context.getApplicationContext());
        }
        return sNetworLivekData;
    }

    private NetworkLiveData(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
    }

    private ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {

        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            postValue(true);
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            postValue(false);
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            postValue(false);
        }

        @Override
        public void onLosing(Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
        }

        @Override
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
        }
    };

    /**
     * When there is an active observer observing the network live data, our network live data
     * will register a callback to the ConnectivityManager to listen to the change of network
     *
     * Also at first time, it will fetch the connectivity information through network info
     */
    @Override
    protected void onActive() {
        super.onActive();
        if (DEBUG) {
            Log.e(TAG, "onActive: ", new Exception());
        }
        connectivityManager.registerDefaultNetworkCallback(callback);
        if (connectivityManager.getActiveNetworkInfo() != null) {
            setValue(connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting());
        } else {
            setValue(false);
        }
    }

    /**
     * When there is no active observer observing our network live data. Our network live data
     * will unregister the callback to make sure there is no memory leakage.
     */
    @Override
    protected void onInactive() {
        if (DEBUG) {
            Log.e(TAG, "onActive: ", new Exception());
        }
        super.onInactive();
        connectivityManager.unregisterNetworkCallback(callback);
    }
}
