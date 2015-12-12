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

package com.wms.opensource.shopfast.task;

import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.shopify.HTMLPageMaker;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebView;

public class LoadCartTask extends AsyncTask<Void, Void, String> {

	private Context context = null;
	WebView webView = null;
	
	public LoadCartTask(Context context, WebView webView) {
		this.context = context;
		this.webView = webView;
	}
	
	protected void onPreExecute() {    	

	}
	
	protected String doInBackground(Void... params) {
		String pageTitle = context.getString(R.string.cartActivityTitle);
		String cartBody = HTMLPageMaker.getCartSection(context);
		String htmlString = HTMLPageMaker.getFullHTML(context, pageTitle, cartBody, true, context.getString(R.string.cartBackgroundColor));			
		return htmlString;
	}
	
    protected void onPostExecute(String result) {
    	webView.loadDataWithBaseURL("example-app://example.com/", result, "text/html", "utf-8", "");
    }
        
}
