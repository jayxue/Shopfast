/*
 * Copyright 2015 Waterloo Mobile Studio
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

package com.wms.opensource.shopfast.util;

import com.wms.opensource.shopfast.type.NetworkStatus;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

	public static NetworkStatus getNetworkConnection(Activity activity) {

		boolean wifiConnecting = false;
		boolean mobileConnecting = false;
		boolean mobileConnected = false;

		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();

		if (netInfo == null || netInfo.length == 0) {
			return NetworkStatus.NONE;
		}
		else {
			for (NetworkInfo ni : netInfo) {
				if (ni.getType() == ConnectivityManager.TYPE_WIFI) {
					if (ni.isConnected()) {
						return NetworkStatus.WIFI_CONNECTED;
					}
					else if (ni.isConnectedOrConnecting()) {
						wifiConnecting = true;
					}
				}
				if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
					if (ni.isConnected()) {
						mobileConnected = true;
					}
					else if (ni.isConnectedOrConnecting()) {
						mobileConnecting = true;
					}
				}
			}
		}

		if (mobileConnected == true) {
			return NetworkStatus.MOBILE_CONNECTED;
		}

		if (wifiConnecting || mobileConnecting) {
			return NetworkStatus.CONNECTING;
		}

		return NetworkStatus.NONE;
	}

}
